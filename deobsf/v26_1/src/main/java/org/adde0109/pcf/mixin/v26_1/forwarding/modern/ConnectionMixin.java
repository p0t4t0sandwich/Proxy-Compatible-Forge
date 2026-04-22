package org.adde0109.pcf.mixin.v26_1.forwarding.modern;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.network.Protocol;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;

import org.adde0109.pcf.forwarding.modern.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@AConstraint(mappings = Mappings.MOJANG)
@Mixin(Connection.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow private SocketAddress address;
    @Shadow public abstract PacketListener shadow$getPacketListener();
    // spotless:on

    @Override
    public @NonNull InetSocketAddress bridge$address() {
        return (InetSocketAddress) this.address;
    }

    @Override
    public void bridge$address(final @NonNull InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public @Nullable Object bridge$getPacketListener() {
        return this.shadow$getPacketListener();
    }

    @Override
    public Protocol bridge$protocol() {
        final PacketListener listener = this.shadow$getPacketListener();
        if (listener == null) {
            return null;
        }
        return Protocol.fromId(listener.protocol().id());
    }
}
