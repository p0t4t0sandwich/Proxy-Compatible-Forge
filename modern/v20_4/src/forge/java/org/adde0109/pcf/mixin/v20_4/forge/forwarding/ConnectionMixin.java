package org.adde0109.pcf.mixin.v20_4.forge.forwarding;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.network.Protocol;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V17, max = MinecraftVersion.V20_4))
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

    @AConstraint(
            mappings = Mappings.SEARGE,
            version = @Versions(min = MinecraftVersion.V20_2, max = MinecraftVersion.V20_4))
    @Override
    public Protocol bridge$protocol() {
        final PacketListener listener = this.shadow$getPacketListener();
        if (listener == null) {
            return null;
        }
        return Protocol.fromId(listener.protocol().id());
    }

    @Override
    public @NonNull Object bridge$disconnectPacket(final @NonNull Object reason) {
        return new ClientboundLoginDisconnectPacket((Component) reason);
    }
}
