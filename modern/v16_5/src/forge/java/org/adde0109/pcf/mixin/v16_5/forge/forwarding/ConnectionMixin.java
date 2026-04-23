package org.adde0109.pcf.mixin.v16_5.forge.forwarding;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.network.Protocol;

import io.netty.util.AttributeKey;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V16_5))
@Mixin(Connection.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow private SocketAddress address;
    @Shadow public abstract PacketListener shadow$getPacketListener();

    @AConstraint(version = @Versions(min = MinecraftVersion.V14, max = MinecraftVersion.V16_5))
    @Shadow @Final public static AttributeKey<ConnectionProtocol> ATTRIBUTE_PROTOCOL;
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

    @AConstraint(version = @Versions(min = MinecraftVersion.V14, max = MinecraftVersion.V16_5))
    @Override
    public Protocol bridge$protocol() {
        final Object listener = this.bridge$getPacketListener();
        if (listener == null) {
            return null;
        }
        return Protocol.fromLegacyId(this.bridge$channel().attr(ATTRIBUTE_PROTOCOL).get().getId());
    }

    @AConstraint(version = @Versions(min = MinecraftVersion.V14, max = MinecraftVersion.V16_5))
    @Override
    public @NonNull Object bridge$disconnectPacket(final @NonNull Object reason) {
        return new ClientboundLoginDisconnectPacket((Component) reason);
    }
}
