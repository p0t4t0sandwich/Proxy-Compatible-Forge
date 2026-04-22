package org.adde0109.pcf.mixin.v26_1.forwarding.legacy;

import com.mojang.authlib.properties.Property;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.network.Protocol;

import io.netty.channel.Channel;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;

import org.adde0109.pcf.forwarding.legacy.ConnectionBridgeLegacy;
import org.adde0109.pcf.forwarding.modern.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

@AConstraint(mappings = Mappings.MOJANG)
@Mixin(Connection.class)
public abstract class ConnectionLegacyMixin implements ConnectionBridge, ConnectionBridgeLegacy {
    // spotless:off
    @Shadow private SocketAddress address;
    @Shadow public abstract PacketListener shadow$getPacketListener();
    @Shadow public abstract Channel channel();
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
    public @NonNull Channel bridge$channel() {
        return this.channel();
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

    // spotless:off
    @Unique private UUID pcf$spoofedUUID = null;
    @Unique private Property[] pcf$spoofedProfile = null;
    // spotless:on

    @Override
    public UUID bridge$spoofedUUID() {
        return this.pcf$spoofedUUID;
    }

    @Override
    public void bridge$spoofedUUID(final @NonNull UUID uuid) {
        this.pcf$spoofedUUID = uuid;
    }

    @Override
    public Property[] bridge$spoofedProfile() {
        return this.pcf$spoofedProfile;
    }

    @Override
    public void bridge$spoofedProfile(final @NonNull Property[] properties) {
        this.pcf$spoofedProfile = properties;
    }
}
