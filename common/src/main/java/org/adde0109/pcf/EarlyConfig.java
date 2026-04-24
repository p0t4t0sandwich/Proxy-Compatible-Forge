package org.adde0109.pcf;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import org.adde0109.pcf.forwarding.Mode;
import org.adde0109.pcf.forwarding.modern.VelocityProxy;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads PCF config directly from the TOML file during normal mod initialization. This keeps the
 * mixin plugin from touching ForgeConfigSpec/ModConfigSpec before other mods can transform those
 * classes.
 */
@ApiStatus.Internal
public final class EarlyConfig {
    private EarlyConfig() {}

    @SuppressWarnings("unchecked")
    public static void load() {
        final Path configPath = resolveConfigDir().resolve(PCF.CONFIG_FILE_NAME);

        if (!Files.exists(configPath)) {
            applyDefaults();
            return;
        }

        try (final CommentedFileConfig config =
                CommentedFileConfig.builder(configPath).sync().preserveInsertionOrder().build()) {
            config.load();

            // Forwarding
            final boolean enableForwarding = config.getOrElse("forwarding.enabled", true);
            final String modeStr = config.getOrElse("forwarding.mode", Mode.MODERN.name());
            Mode forwardingMode;
            try {
                forwardingMode = Mode.valueOf(modeStr.toUpperCase());
            } catch (final IllegalArgumentException e) {
                forwardingMode = Mode.MODERN;
            }
            final String secret = config.getOrElse("forwarding.secret", "");
            final List<String> approvedProxyHosts =
                    (List<String>)
                            config.getOrElse("forwarding.approvedProxyHosts", List.<String>of());

            PCF.instance()
                    .setForwarding(
                            new PCF.Forwarding(
                                    enableForwarding, forwardingMode, secret, approvedProxyHosts));

            // CrossStitch
            final boolean enableCrossStitch = config.getOrElse("crossStitch.enabled", true);
            final List<String> forceWrappedArguments =
                    (List<String>)
                            config.getOrElse(
                                    "crossStitch.forceWrappedArguments", List.<String>of());
            final boolean forceWrapVanilla =
                    config.getOrElse("crossStitch.forceWrapVanillaArguments", false);

            PCF.instance()
                    .setCrossStitch(
                            new PCF.CrossStitch(
                                    enableCrossStitch, forceWrappedArguments, forceWrapVanilla));

            // Debug
            final boolean enableDebug = config.getOrElse("debug.enabled", false);
            final List<String> disabledMixins =
                    (List<String>) config.getOrElse("debug.disabledMixins", List.<String>of());

            PCF.instance().setDebug(new PCF.Debug(enableDebug, disabledMixins));

            // Advanced
            final String modernForwardingVersionStr =
                    config.getOrElse(
                            "advanced.modernForwardingVersion",
                            VelocityProxy.Version.NO_OVERRIDE.name());
            VelocityProxy.Version modernForwardingVersion;
            try {
                modernForwardingVersion =
                        VelocityProxy.Version.valueOf(modernForwardingVersionStr.toUpperCase());
            } catch (final IllegalArgumentException e) {
                modernForwardingVersion = VelocityProxy.Version.NO_OVERRIDE;
            }

            PCF.instance().setAdvanced(new PCF.Advanced(modernForwardingVersion));
        } catch (final Exception e) {
            PCF.logger.error("Failed to read early config, using defaults", e);
            applyDefaults();
        }
    }

    private static void applyDefaults() {
        PCF.instance().resetConfigToDefaults();
    }

    private static Path resolveConfigDir() {
        // Try Forge's FMLPaths
        try {
            final Class<?> fmlPaths = Class.forName("net.minecraftforge.fml.loading.FMLPaths");
            final Object configDir = fmlPaths.getField("CONFIGDIR").get(null);
            final Method getMethod = configDir.getClass().getMethod("get");
            return (Path) getMethod.invoke(configDir);
        } catch (final Exception ignored) {
        }
        // Try NeoForge's FMLPaths
        try {
            final Class<?> fmlPaths = Class.forName("net.neoforged.fml.loading.FMLPaths");
            final Object configDir = fmlPaths.getField("CONFIGDIR").get(null);
            final Method getMethod = configDir.getClass().getMethod("get");
            return (Path) getMethod.invoke(configDir);
        } catch (final Exception ignored) {
        }
        // Fallback
        return Path.of("config");
    }
}
