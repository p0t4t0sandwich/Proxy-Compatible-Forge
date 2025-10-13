package org.adde0109.pcf.v1_20_2.neoforge;

import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import org.adde0109.pcf.PCFInitializer;
import org.adde0109.pcf.v1_20_2.neoforge.crossstitch.CSBootstrap;
import org.adde0109.pcf.v1_20_2.neoforge.forwarding.FWDBootstrap;

import java.util.Optional;

@AConstraint(
        platform = Platform.NEOFORGE,
        version = @Versions(min = MinecraftVersion.V20_2, max = MinecraftVersion.V20_6))
public final class Initializer implements PCFInitializer {
    @Override
    public void onInit() {
        FWDBootstrap.RESOURCE_LOCATION = ResourceLocation::new;
        FWDBootstrap.COMPONENT = Component::nullToEmpty;
        FWDBootstrap.init();
        CSBootstrap.ARGUMENT_TYPES_REGISTRY =
                () -> Optional.of(BuiltInRegistries.COMMAND_ARGUMENT_TYPE);
        CSBootstrap.COMMAND_ARGUMENT_TYPE_KEY =
                (type) ->
                        CSBootstrap.ARGUMENT_TYPES_REGISTRY
                                .get()
                                .flatMap(reg -> reg.getResourceKey(type));
        CSBootstrap.COMMAND_ARGUMENT_TYPE_ID =
                (type) ->
                        CSBootstrap.ARGUMENT_TYPES_REGISTRY
                                .get()
                                .map(reg -> reg.getId(type))
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "Could not find ID for argument type: "
                                                                + type.getClass().getName()));

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);

        NeoForge.EVENT_BUS.addListener(
                (ServerAboutToStartEvent event) -> {
                    Config.setupForwarding();
                    Config.setupModdedArgumentTypes();
                });
    }
}
