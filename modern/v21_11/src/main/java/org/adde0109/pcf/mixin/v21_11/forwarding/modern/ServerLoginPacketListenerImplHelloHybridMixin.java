package org.adde0109.pcf.mixin.v21_11.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleHello;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// spotless:off
@AConstraint(
        platform = {Platform.ARCLIGHT, Platform.CATSERVER, Platform.MAGMA, Platform.MOHIST},
        version = @Versions(min = MinecraftVersion.V17))
@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplHelloHybridMixin {
    @AConstraint(mappings = Mappings.SEARGE)
    @Shadow ServerLoginPacketListenerImpl.State f_10019_;

    @AConstraint(mappings = Mappings.SEARGE)
    @Inject(method = "m_5990_", require = 0, cancellable = true, at = @At(value = "HEAD"))
    private void onHandleHelloS(final @NonNull CallbackInfo ci) {
        Validate.validState(
                this.f_10019_ == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
        handleHello((ServerLoginPacketListenerBridge) this, ci);
    }

    @AConstraint(mappings = Mappings.MOJANG)
    @Shadow private ServerLoginPacketListenerImpl.State state;

    @AConstraint(mappings = Mappings.MOJANG)
    @Inject(method = "handleHello", require = 0, cancellable = true, at = @At(value = "HEAD"))
    private void onHandleHelloM(final @NonNull CallbackInfo ci) {
        Validate.validState(
                this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
        handleHello((ServerLoginPacketListenerBridge) this, ci);
    }
}
// spotless:on
