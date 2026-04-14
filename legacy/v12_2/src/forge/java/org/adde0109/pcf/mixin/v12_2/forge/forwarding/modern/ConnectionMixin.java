package org.adde0109.pcf.mixin.v12_2.forge.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.ModernForwarding.injectIntoPipeline;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;

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
        mappings = Mappings.LEGACY_SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V12_2))
@Mixin(NetworkManager.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow private SocketAddress socketAddress;
    @Shadow public abstract INetHandler shadow$getNetHandler();
    @Shadow public abstract Channel shadow$channel();
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
    public @NonNull Channel bridge$channel() {
        return this.shadow$channel();
    }

    @Override
    public @Nullable Object bridge$getPacketListener() {
        return this.shadow$getNetHandler();
    }

    @Inject(method = "channelActive", at = @At("TAIL"), remap = false)
    private void onChannelActive(
            final @NonNull ChannelHandlerContext ctx, final @NonNull CallbackInfo ci) {
        injectIntoPipeline(this, ctx);
    }
}
