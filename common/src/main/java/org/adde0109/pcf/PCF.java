package org.adde0109.pcf;

import dev.neuralnexus.taterapi.loader.EntrypointLoader;
import dev.neuralnexus.taterapi.logger.Logger;
import dev.neuralnexus.taterapi.meta.Constraint;
import dev.neuralnexus.taterapi.meta.Constraints;
import dev.neuralnexus.taterapi.meta.MetaAPI;
import dev.neuralnexus.taterapi.meta.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;
import dev.neuralnexus.taterapi.meta.ModContainer;
import dev.neuralnexus.taterapi.meta.ModResource;
import dev.neuralnexus.taterapi.meta.Platform;
import dev.neuralnexus.taterapi.meta.Platforms;
import dev.neuralnexus.taterapi.network.NetworkRegistry;
import dev.neuralnexus.taterapi.registries.AdapterRegistry;

import org.adde0109.pcf.forwarding.Mode;
import org.adde0109.pcf.forwarding.compat.ArclightPreLogin;
import org.adde0109.pcf.forwarding.compat.MohistBridge;
import org.adde0109.pcf.forwarding.compat.SpigotLoginHandler;
import org.adde0109.pcf.forwarding.compat.SpongePreLogin;
import org.adde0109.pcf.forwarding.modern.ModernForwarding;
import org.adde0109.pcf.forwarding.modern.PlayerInfoQueryPayload;
import org.adde0109.pcf.forwarding.modern.VelocityProxy;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

public final class PCF {
    private PCF() {}

    public static final String MOD_ID = "pcf";
    public static final String MOD_NAME = "Proxy Compatible Forge";
    public static final String CONFIG_FILE_NAME = "proxy-compatible-forge.toml";

    private static final PCF INSTANCE = new PCF();
    public static final Logger logger = Logger.create(MOD_ID);

    public static PCF instance() {
        return INSTANCE;
    }

    private static final @NonNull String SERVICE_PATH =
            "META-INF/services/org.adde0109.pcf.PCFInitializer";
    private static EntrypointLoader<PCFInitializer> loader;

    @ApiStatus.Internal
    void onInit() {
        MetaAPI api = MetaAPI.instance();
        MinecraftVersion mcv = api.version();
        Platform platform = api.platform();

        // spotless:off
        PCF.logger.info("Initializing Proxy Compatible Forge on "
                + "Minecraft " + mcv
                + " (" + platform + " " + api.meta().apiVersion() + ")");
        // spotless:on

        final boolean debug = Constraint.Evaluator.DEBUG;
        Constraint.Evaluator.DEBUG = this.debug().enabled();

        final ModContainer<?> container = MetaAPI.instance().mod(MOD_ID).orElseThrow();
        try (final ModResource resource = container.resource()) {
            final Path servicePath = resource.getResourceOrThrow(SERVICE_PATH);
            loader =
                    EntrypointLoader.builder()
                            .entrypointClass(PCFInitializer.class)
                            .logger(logger)
                            .servicePaths(servicePath)
                            .useServiceLoader(false)
                            .useOtherProviders(true)
                            .build();

            loader.load();
        } catch (final Exception e) {
            PCF.logger.error("Failed to access PCF Mod Resources: " + e.getClass(), e);
        }
        loader.onInit();

        // Modern forwarding init
        if (this.forwarding().enabled() && this.forwarding().mode().equals(Mode.MODERN)) {
            NetworkRegistry.registerQueryPayload(
                    PlayerInfoQueryPayload.IDENTIFIER, PlayerInfoQueryPayload.STREAM_CODEC);

            if (Constraint.builder().platform(Platforms.ARCLIGHT).result()) {
                if (Constraint.range(MinecraftVersions.V14, MinecraftVersions.V20_1).result()) {
                    ModernForwarding.postProcessors.removeFirst();
                    ModernForwarding.postProcessors.add(
                            (slpl, profile) -> {
                                slpl.bridge$setGameProfile(profile);
                                ArclightPreLogin.V14.preLogin(slpl);
                                return false;
                            });
                } else if (Constraint.builder().version(MinecraftVersions.V20_2).result()) {
                    ModernForwarding.postProcessors.removeFirst();
                    ModernForwarding.postProcessors.add(
                            (slpl, profile) -> {
                                ArclightPreLogin.V20_2.preLogin(slpl, profile);
                                return false;
                            });
                } else if (Constraint.noLessThan(MinecraftVersions.V20_3).result()) {
                    ModernForwarding.postProcessors.removeFirst();
                    ModernForwarding.postProcessors.add(
                            (slpl, profile) -> {
                                ((ArclightPreLogin.V20_4) slpl).bridge$preLogin(profile);
                                return false;
                            });
                }
            } else if (Constraint.builder()
                    .platform(Platforms.MOHIST)
                    .version(MinecraftVersions.V20_1)
                    .result()) {
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile) -> {
                            slpl.bridge$setGameProfile(profile);
                            MohistBridge.V20_1.fireEvents(slpl);
                            return false;
                        });
            } else if (Constraint.builder()
                    .platform(Platforms.YOUER)
                    .version(MinecraftVersions.V21_1)
                    .result()) {
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile) -> {
                            MohistBridge.Youer.fireEvents(slpl, profile);
                            slpl.bridge$startClientVerification(profile);
                            return false;
                        });
            } else if (Constraints.builder()
                    .or(
                            Constraint.builder()
                                    .platform(Platforms.KETTING)
                                    .version(MinecraftVersions.V20_1),
                            Constraint.range(MinecraftVersions.V12_2, MinecraftVersions.V20_1)
                                    .platform(Platforms.MAGMA),
                            Constraint.range(MinecraftVersions.V12_2, MinecraftVersions.V19_4)
                                    .platform(Platforms.MOHIST))
                    .result()) {
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile) -> {
                            slpl.bridge$setGameProfile(profile);
                            SpigotLoginHandler.Legacy.fireEvents(slpl);
                            return false;
                        });
            } else if (Constraints.builder()
                    .or(
                            Constraint.range(MinecraftVersions.V20_2, MinecraftVersions.V20_4)
                                    .platform(Platforms.KETTING),
                            Constraint.builder()
                                    .platform(Platforms.MOHIST)
                                    .version(MinecraftVersions.V20_2))
                    .result()) {
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile) -> {
                            SpigotLoginHandler.V20_2.fireEvents(slpl, profile);
                            return false;
                        });
            } else if (Constraints.builder()
                    .or(
                            Constraint.builder()
                                    .platform(Platforms.MAGMA)
                                    .version(MinecraftVersions.V21_1),
                            Constraint.builder()
                                    .platform(Platforms.MOHIST)
                                    .version(MinecraftVersions.V21_1, MinecraftVersions.V21_4),
                            Constraint.range(MinecraftVersions.V21_11, MinecraftVersions.V26_1)
                                    .platform(Platforms.YOUER),
                            Constraint.builder()
                                    .platform(Platforms.NEOTENET)
                                    .version(MinecraftVersions.V21_1, MinecraftVersions.V21_10))
                    .result()) {
                ModernForwarding.postProcessors.addFirst(
                        (slpl, profile) -> {
                            ((SpigotLoginHandler.V20_5) slpl).callPlayerPreLoginEvents(profile);
                            return false;
                        });
            }

            if (Constraint.range(MinecraftVersions.V16, MinecraftVersions.V18_2)
                    .platform(Platforms.SPONGE)
                    .result()) {
                ModernForwarding.postProcessors.addFirst(
                        (slpl, profile) -> {
                            slpl.bridge$setGameProfile(profile);
                            return SpongePreLogin.API8.fireAuthEvent(slpl);
                        });
            }
        }

        Constraint.Evaluator.DEBUG = debug;
    }

    @ApiStatus.Internal
    public static void forceLoadConfig() {
        try {
            if (Constraint.builder().platform(Platforms.FORGE).result()) {
                if (Constraint.lessThan(MinecraftVersions.V13).result()) {
                    Class.forName("org.adde0109.pcf.v12_2.forge.ModConfig")
                            .getMethod("reload")
                            .invoke(null);
                } else {
                    Class.forName("org.adde0109.pcf.v16_5.forge.Config")
                            .getMethod("reload")
                            .invoke(null);
                }
            } else if (Constraint.builder().platform(Platforms.NEOFORGE).result()) {
                Class.forName("org.adde0109.pcf.v20_2.neoforge.Config")
                        .getMethod("reload")
                        .invoke(null);
            }
        } catch (ClassNotFoundException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            logger.error("Failed to load Config class", e);
        }
    }

    private static final AdapterRegistry ADAPTER_REGISTRY = new AdapterRegistry();

    @ApiStatus.Internal
    public AdapterRegistry adapters() {
        return ADAPTER_REGISTRY;
    }

    private Forwarding forwarding;

    public Forwarding forwarding() {
        return this.forwarding;
    }

    @ApiStatus.Internal
    public void setForwarding(final @NonNull Forwarding forwarding) {
        this.forwarding = forwarding;
    }

    private CrossStitch crossStitch;

    public CrossStitch crossStitch() {
        return this.crossStitch;
    }

    @ApiStatus.Internal
    public void setCrossStitch(final @NonNull CrossStitch crossStitch) {
        this.crossStitch = crossStitch;
    }

    private Debug debug;

    public Debug debug() {
        return this.debug;
    }

    @ApiStatus.Internal
    public void setDebug(final @NonNull Debug debug) {
        this.debug = debug;
    }

    private Advanced advanced;

    public Advanced advanced() {
        return this.advanced;
    }

    @ApiStatus.Internal
    public void setAdvanced(final @NonNull Advanced advanced) {
        this.advanced = advanced;
    }

    public record Forwarding(
            boolean enabled,
            @NonNull Mode mode,
            @NonNull String secret,
            List<@NonNull String> approvedProxyHosts) {}

    public record CrossStitch(
            boolean enabled,
            List<@NonNull String> forceWrappedArguments,
            boolean forceWrapVanillaArguments) {}

    public record Debug(boolean enabled, List<@NonNull String> disabledMixins) {}

    public record Advanced(VelocityProxy.@NonNull Version modernForwardingVersion) {}
}
