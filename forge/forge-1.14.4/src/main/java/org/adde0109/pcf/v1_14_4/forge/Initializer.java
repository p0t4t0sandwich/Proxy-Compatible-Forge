package org.adde0109.pcf.v1_14_4.forge;

import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;

import org.adde0109.pcf.PCFInitializer;
import org.adde0109.pcf.v1_14_4.forge.forwarding.FWDBootstrap;
import org.apache.commons.lang3.tuple.Pair;

@AConstraint(
        platform = Platform.FORGE,
        version = @Versions(min = MinecraftVersion.V14, max = MinecraftVersion.V16_5))
public final class Initializer implements PCFInitializer {
    @Override
    public void onInit() {
        FWDBootstrap.RESOURCE_LOCATION = ResourceLocation::new;
        FWDBootstrap.COMPONENT = TextComponent::new;
        FWDBootstrap.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);

        ModLoadingContext.get()
                .registerExtensionPoint(
                        ExtensionPoint.DISPLAYTEST,
                        () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        MinecraftForge.EVENT_BUS.addListener(
                (FMLServerAboutToStartEvent event) -> {
                    Config.setupForwarding();
                    Config.setupModdedArgumentTypes();
                });
    }
}
