package org.adde0109.pcf.v1_19.forge;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.v1_14_4.forge.Config;

@SuppressWarnings("unused")
public class Initializer {
    public static void init() {
        PCF.resourceLocation = ResourceLocation::new;
        PCF.component = Component::nullToEmpty;
        PCF.COMMAND_ARGUMENT_TYPE_KEY =
                (type) -> Registry.COMMAND_ARGUMENT_TYPE.getKey((ArgumentTypeInfo<?, ?>) type);
        PCF.COMMAND_ARGUMENT_TYPE_ID =
                (type) -> Registry.COMMAND_ARGUMENT_TYPE.getId((ArgumentTypeInfo<?, ?>) type);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);

        ModLoadingContext.get()
                .registerExtensionPoint(
                        IExtensionPoint.DisplayTest.class,
                        () ->
                                new IExtensionPoint.DisplayTest(
                                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                                        (a, b) -> true));

        MinecraftForge.EVENT_BUS.addListener(
                (ServerAboutToStartEvent event) -> Config.setupForwarding());
    }
}
