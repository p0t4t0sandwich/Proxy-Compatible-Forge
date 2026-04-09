package org.adde0109.pcf.mixin.v13_2.forge.forwarding.modern;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

import org.adde0109.pcf.forwarding.modern.ConnectionBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@AConstraint(
        mappings = Mappings.LEGACY_SEARGE,
        version = @Versions(min = MinecraftVersion.V13, max = MinecraftVersion.V13_2))
@Mixin(NetworkManager.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow private SocketAddress socketAddress;

    // TODO: Find a way to swap just this method and merge with v16_5
    @Shadow public abstract void shadow$sendPacket(Packet<?> packet);
    // spotless:on

    @Override
    public InetSocketAddress bridge$address() {
        return (InetSocketAddress) this.socketAddress;
    }

    @Override
    public void bridge$address(InetSocketAddress address) {
        this.socketAddress = address;
    }

    @Override
    public void bridge$send(Object packet) {
        this.shadow$sendPacket((Packet<?>) packet);
    }
}
