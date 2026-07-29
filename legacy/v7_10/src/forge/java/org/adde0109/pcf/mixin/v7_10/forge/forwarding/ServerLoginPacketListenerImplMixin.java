package org.adde0109.pcf.mixin.v7_10.forge.forwarding;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.IChatComponent;

import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V8_9))
@Mixin(NetHandlerLoginServer.class)
public abstract class ServerLoginPacketListenerImplMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final private static Logger logger;
    @Shadow public abstract void shadow$closeConnection(String reason);
    // spotless:on

    @Override
    public void bridge$disconnect(final @NonNull Object reason) {
        // Note: IChatComponent.getFormattedText() is client-only
        this.shadow$closeConnection(((IChatComponent) reason).getUnformattedTextForChat());
    }

    @Override
    public void bridge$logger_info(final @NonNull String text, final Object... params) {
        logger.info(text, params);
    }

    @Override
    public void bridge$logger_error(final @NonNull String text, final Object... params) {
        logger.error(text, params);
    }
}
