package org.adde0109.pcf;

import static dev.neuralnexus.taterapi.network.Protocol.map;

import dev.neuralnexus.taterapi.loader.EntrypointLoader;
import dev.neuralnexus.taterapi.meta.Constraint;
import dev.neuralnexus.taterapi.meta.Constraints;
import dev.neuralnexus.taterapi.meta.MetaAPI;
import dev.neuralnexus.taterapi.meta.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;
import dev.neuralnexus.taterapi.meta.ModContainer;
import dev.neuralnexus.taterapi.meta.ModResource;
import dev.neuralnexus.taterapi.meta.Platform;
import dev.neuralnexus.taterapi.meta.Platforms;
import dev.neuralnexus.taterapi.network.PayloadRegistry;

import org.adde0109.pcf.forwarding.Mode;
import org.adde0109.pcf.forwarding.compatibility.prelogin.ArclightPreLogin;
import org.adde0109.pcf.forwarding.compatibility.prelogin.MohistPreLogin;
import org.adde0109.pcf.forwarding.compatibility.prelogin.SpigotPreLogin;
import org.adde0109.pcf.forwarding.compatibility.prelogin.SpongePreLogin;
import org.adde0109.pcf.forwarding.modern.ModernForwarding;
import org.adde0109.pcf.forwarding.modern.PlayerInfoQueryPayload;
import org.adde0109.pcf.forwarding.modern.VelocityProxy;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

public final class PCF extends Constants {
    private PCF() {}

    private static final PCF INSTANCE = new PCF();

    public static PCF instance() {
        return INSTANCE;
    }

    private static final @NonNull String SERVICE_PATH =
            "META-INF/services/org.adde0109.pcf.PCFInitializer";

    @SuppressWarnings("FieldCanBeLocal")
    private static EntrypointLoader<PCFInitializer> loader;

    @ApiStatus.Internal
    void onInit() {
        final MetaAPI api = MetaAPI.instance();
        final MinecraftVersion mcv = api.version();
        final Platform platform = api.platform();

        // spotless:off
        PCF.logger.info("Initializing " + MOD_NAME + " on "
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
            throw new RuntimeException(
                    "Failed to access " + MOD_NAME + " Mod Resources: " + e.getClass(), e);
        }
        loader.onInit();

        // Modern forwarding init
        if (this.forwarding().enabled() && this.forwarding().mode().equals(Mode.MODERN)) {
            PayloadRegistry.register(
                    PlayerInfoQueryPayload.TYPE,
                    map(PlayerInfoQueryPayload.IDENTIFIER, MinecraftVersions.V7_2));

            if (Constraint.builder().platform(Platforms.ARCLIGHT).result()) {
                logger.debug("Arclight detected, applying pre-login post processor");
                if (Constraint.range(MinecraftVersions.V14, MinecraftVersions.V20_1).result()) {
                    ModernForwarding.postProcessors.removeFirst();
                    ModernForwarding.postProcessors.add(
                            (slpl, profile, c) -> {
                                slpl.bridge$setGameProfile(profile);
                                ArclightPreLogin.V14.preLogin(slpl);
                            });
                } else if (Constraint.builder().version(MinecraftVersions.V20_2).result()) {
                    ModernForwarding.postProcessors.removeFirst();
                    ModernForwarding.postProcessors.add(
                            (slpl, profile, c) -> ArclightPreLogin.V20_2.preLogin(slpl, profile));
                } else if (Constraint.noLessThan(MinecraftVersions.V20_3).result()) {
                    ModernForwarding.postProcessors.removeFirst();
                    ModernForwarding.postProcessors.add(
                            (slpl, profile, c) -> ArclightPreLogin.V20_4.preLogin(slpl, profile));
                }
            } else if (Constraint.builder()
                    .platform(Platforms.MOHIST)
                    .version(MinecraftVersions.V20_1)
                    .result()) {
                logger.debug("Mohist detected, applying pre-login post processor");
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile, c) -> {
                            slpl.bridge$setGameProfile(profile);
                            MohistPreLogin.V20_1.fireEvents(slpl);
                        });
            } else if (Constraint.builder()
                    .platform(Platforms.YOUER)
                    .version(MinecraftVersions.V21_1)
                    .result()) {
                logger.debug("Youer detected, applying pre-login post processor");
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile, c) -> {
                            MohistPreLogin.Youer.fireEvents(slpl, profile);
                            slpl.bridge$startClientVerification(profile);
                        });
            } else if (Constraints.builder()
                    .or(
                            Constraint.range(MinecraftVersions.V12_2, MinecraftVersions.V19_4)
                                    .platform(Platforms.CATSERVER, Platforms.MOHIST),
                            Constraint.range(MinecraftVersions.V12_2, MinecraftVersions.V18_2)
                                    .platform(
                                            Platforms.MAGMA), // TODO: Magma 1.19.3 is having issues
                            Constraint.builder()
                                    .platform(Platforms.MAGMA, Platforms.KETTING)
                                    .version(MinecraftVersions.V20_1))
                    .result()) {
                logger.debug("Forge+Bukkit hybrid detected, applying pre-login post processor");
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile, c) -> {
                            slpl.bridge$setGameProfile(profile);
                            SpigotPreLogin.Legacy.fireEvents(slpl);
                        });
            } else if (Constraints.builder()
                    .or(
                            Constraint.range(MinecraftVersions.V20_2, MinecraftVersions.V20_4)
                                    .platform(Platforms.KETTING),
                            Constraint.builder()
                                    .platform(Platforms.MOHIST)
                                    .version(MinecraftVersions.V20_2))
                    .result()) {
                logger.debug(
                        "[Neo]Forge+Bukkit hybrid detected, applying pre-login post processor");
                ModernForwarding.postProcessors.removeFirst();
                ModernForwarding.postProcessors.add(
                        (slpl, profile, c) -> SpigotPreLogin.V20_2.fireEvents(slpl, profile));
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
                logger.debug(
                        "[Neo]Forge+Bukkit hybrid detected, applying pre-login post processor");
                ModernForwarding.postProcessors.addFirst(
                        (slpl, profile, c) ->
                                SpigotPreLogin.V20_5.callPlayerPreLoginEvents(slpl, profile));
            }

            if (Constraint.range(MinecraftVersions.V16, MinecraftVersions.V18_2)
                    .platform(Platforms.SPONGE)
                    .result()) {
                logger.debug("SpongeAPI 8 or 9 detected, applying pre-login post processor");
                ModernForwarding.postProcessors.addFirst(
                        (slpl, profile, c) -> {
                            slpl.bridge$setGameProfile(profile);
                            c.setCancelled(SpongePreLogin.API8.fireAuthEvent(slpl));
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
                    Class.forName("org.adde0109.pcf.v26_1.forge.Config")
                            .getMethod("reload")
                            .invoke(null);
                }
            } else if (Constraint.builder().platform(Platforms.NEOFORGE).result()) {
                Class.forName("org.adde0109.pcf.v26_1.neoforge.Config")
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
