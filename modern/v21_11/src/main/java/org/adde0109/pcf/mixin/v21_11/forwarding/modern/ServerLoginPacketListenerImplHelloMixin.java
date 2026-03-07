package org.adde0109.pcf.mixin.v21_11.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleHello;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.AConstraints;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// spotless:off
@AConstraints({
    @AConstraint(platform = Platform.ARCLIGHT, invert = true),
    @AConstraint(version = @Versions(min = MinecraftVersion.V17))
})
@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(targets = "net.minecraft.server.network.ServerLoginPacketListenerImpl")
public abstract class ServerLoginPacketListenerImplHelloMixin
        implements ServerLoginPacketListenerBridge {
    @AConstraint(mappings = Mappings.SEARGE, version = @Versions(max = MinecraftVersion.V18_2))
    @Inject(method = "m_5990_", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;f_10019_:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"))
    private void onHandleHello_17(final @NonNull CallbackInfo ci) {
        handleHello(this, ci);
    }

    @AConstraint(mappings = Mappings.SEARGE, version = @Versions(min = MinecraftVersion.V19, max = MinecraftVersion.V20_1))
    @Inject(method = "m_5990_", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 2,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;f_10019_:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"))
    private void onHandleHello_19(final @NonNull CallbackInfo ci) {
        handleHello(this, ci);
    }

    @AConstraint(mappings = Mappings.SEARGE, version = @Versions(min = MinecraftVersion.V20_2, max = MinecraftVersion.V20_4))
    @Inject(method = "m_5990_", cancellable = true, at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;m_294008_(Lcom/mojang/authlib/GameProfile;)V"))
    private void onHandleHello_20_S(final @NonNull CallbackInfo ci) {
        handleHello(this, ci);
    }

    @AConstraint(mappings = Mappings.MOJANG, version = @Versions(min = MinecraftVersion.V20_2))
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V"))
    private void onHandleHello_20_M(final @NonNull CallbackInfo ci) {
        handleHello(this, ci);
    }

    @AConstraint(platform = Platform.ARCLIGHT, version = @Versions(min = MinecraftVersion.V17))
    @Mixin(ServerLoginPacketListenerImpl.class)
    public static class ArclightMixin {
        @AConstraint(mappings = Mappings.SEARGE)
        @Shadow ServerLoginPacketListenerImpl.State f_10019_;

        @AConstraint(mappings = Mappings.SEARGE)
        @Inject(method = "m_5990_", require = 0, cancellable = true, at = @At(value = "HEAD"))
        private void onHandleHelloArclightS(final @NonNull CallbackInfo ci) {
            Validate.validState(
                    this.f_10019_ == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
            handleHello((ServerLoginPacketListenerBridge) this, ci);
        }

        @AConstraint(mappings = Mappings.MOJANG)
        @Shadow private ServerLoginPacketListenerImpl.State state;

        @AConstraint(mappings = Mappings.MOJANG)
        @Inject(method = "handleHello", require = 0, cancellable = true, at = @At(value = "HEAD"))
        private void onHandleHelloArclightM(final @NonNull CallbackInfo ci) {
            Validate.validState(
                    this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
            handleHello((ServerLoginPacketListenerBridge) this, ci);
        }
    }
}
// spotless:on
