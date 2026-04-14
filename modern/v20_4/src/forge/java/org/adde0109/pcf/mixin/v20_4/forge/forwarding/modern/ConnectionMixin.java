package org.adde0109.pcf.mixin.v20_4.forge.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.ModernForwarding.injectIntoPipeline;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;

import org.adde0109.pcf.forwarding.modern.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    @Shadow public abstract Channel shadow$channel();
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
        return this.shadow$channel();
    }

    @Override
    public @Nullable Object bridge$getPacketListener() {
        return this.shadow$getPacketListener();
    }

    @Inject(method = "channelActive", at = @At("TAIL"), remap = false)
    private void onChannelActive(
            final @NonNull ChannelHandlerContext ctx, final @NonNull CallbackInfo ci) {
        injectIntoPipeline(this, ctx);
    }
}
