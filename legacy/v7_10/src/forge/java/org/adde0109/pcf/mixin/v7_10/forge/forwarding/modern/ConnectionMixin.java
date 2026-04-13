package org.adde0109.pcf.mixin.v7_10.forge.forwarding.modern;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

import org.adde0109.pcf.forwarding.modern.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@AConstraint(
        mappings = Mappings.LEGACY_SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V7_10))
@Mixin(NetworkManager.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow public abstract void shadow$scheduleOutboundPacket(Packet packet, GenericFutureListener<?>... futureListeners);
    @Shadow public abstract Channel shadow$channel();

    @Override
    public void bridge$send(final @NonNull Object packet) {
        if (packet instanceof Packet mcPacket) {
            this.shadow$scheduleOutboundPacket(mcPacket);
        } else {
            this.shadow$channel().writeAndFlush(packet);
        }
    }
    // spotless:on
}
