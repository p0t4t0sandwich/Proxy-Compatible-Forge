package org.adde0109.pcf.mixin.v12_2.forge.forwarding.modern;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;

import org.adde0109.pcf.forwarding.modern.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@AConstraint(
        mappings = Mappings.LEGACY_SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V12_2))
@Mixin(NetworkManager.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow private SocketAddress socketAddress;
    @Shadow public abstract INetHandler shadow$getNetHandler();
    // spotless:on

    @Override
    public @NonNull InetSocketAddress bridge$address() {
        return (InetSocketAddress) this.socketAddress;
    }

    @Override
    public void bridge$address(final @NonNull InetSocketAddress address) {
        this.socketAddress = address;
    }

    @Override
    public @Nullable Object bridge$getPacketListener() {
        return this.shadow$getNetHandler();
    }
}
