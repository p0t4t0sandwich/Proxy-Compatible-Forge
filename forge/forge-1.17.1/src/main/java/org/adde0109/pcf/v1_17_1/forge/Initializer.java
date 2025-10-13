package org.adde0109.pcf.v1_17_1.forge;

import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;

import org.adde0109.pcf.PCFInitializer;
import org.adde0109.pcf.v1_14_4.forge.Config;
import org.adde0109.pcf.v1_14_4.forge.forwarding.FWDBootstrap;

@AConstraint(
        platform = Platform.FORGE,
        version = @Versions(min = MinecraftVersion.V17, max = MinecraftVersion.V17_1))
public final class Initializer implements PCFInitializer {
    @Override
    public void onInit() {
        FWDBootstrap.RESOURCE_LOCATION = ResourceLocation::new;
        FWDBootstrap.COMPONENT = Component::nullToEmpty;
        FWDBootstrap.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);

        ModLoadingContext.get()
                .registerExtensionPoint(
                        IExtensionPoint.DisplayTest.class,
                        () ->
                                new IExtensionPoint.DisplayTest(
                                        () -> FMLNetworkConstants.IGNORESERVERONLY,
                                        (a, b) -> true));

        MinecraftForge.EVENT_BUS.addListener(
                (FMLServerAboutToStartEvent event) -> {
                    Config.setupForwarding();
                    Config.setupModdedArgumentTypes();
                });
    }
}
