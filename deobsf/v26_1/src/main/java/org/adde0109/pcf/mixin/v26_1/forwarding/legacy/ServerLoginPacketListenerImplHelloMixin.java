package org.adde0109.pcf.mixin.v26_1.forwarding.legacy;

import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.createProfile;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0024-BungeeCord-Support.patch
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplHelloMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final private Connection connection;
    @Shadow protected abstract void startClientVerification(GameProfile profile);

    @AConstraint(mappings = Mappings.MOJANG, version = @Versions(min = MinecraftVersion.V20_2))
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V"))
    // spotless:on
    private void onHandleHello_20_M(
            final ServerboundHelloPacket packet, final @NonNull CallbackInfo ci) {
        // TODO: PostProcessors
        this.startClientVerification(
                createProfile((ConnectionBridge) this.connection, packet.name()));
        ci.cancel();
    }
}
