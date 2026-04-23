package org.adde0109.pcf.mixin.v26_1.forwarding.legacy;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.adde0109.pcf.forwarding.legacy.LegacyForwarding;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0024-BungeeCord-Support.patch
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplHelloMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @AConstraint(mappings = Mappings.MOJANG, version = @Versions(min = MinecraftVersion.V20_2))
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V"))
    // spotless:on
    private void onHandleHello_20_M(final @NonNull CallbackInfo ci) {
        try {
            LegacyForwarding.handleHello(this, ci);
        } catch (final ThrowingComponent e) {
            this.bridge$disconnect(e.getComponent());
        }
    }
}
