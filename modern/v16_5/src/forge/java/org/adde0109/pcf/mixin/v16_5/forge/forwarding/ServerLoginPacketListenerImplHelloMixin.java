package org.adde0109.pcf.mixin.v16_5.forge.forwarding;

import static org.adde0109.pcf.forwarding.Forwarding.handleHello;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V14, max = MinecraftVersion.V16_5))
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplHelloMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow private ServerLoginPacketListenerImpl.State state;

    @AConstraint(
            platform = {Platform.ARCLIGHT, Platform.CATSERVER, Platform.MAGMA, Platform.MOHIST}, invert = true)
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;state:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"))
    // spotless:on
    private void onHandleHello(final @NonNull CallbackInfo ci) {
        handleHello(this, ci);
    }

    /**
     * Arclight - Overwrites the method <br>
     * CatServer, Magma, Mohist - Patches alter the method in an incompatible manner
     */
    @AConstraint(
            platform = {Platform.ARCLIGHT, Platform.CATSERVER, Platform.MAGMA, Platform.MOHIST})
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "HEAD"))
    private void onHandleHelloHybrid(final @NonNull CallbackInfo ci) {
        Validate.validState(
                this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
        handleHello(this, ci);
    }
}
