package org.adde0109.pcf.mixin.v1_14_4.forge.forwarding.modern;

import com.bawnorton.mixinsquared.TargetHandler;

import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;
import dev.neuralnexus.taterapi.muxins.annotations.ReqMCVersion;
import dev.neuralnexus.taterapi.muxins.annotations.ReqPlatform;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ReqPlatform(Platform.SPONGE)
@ReqMCVersion(MinecraftVersion.V16_5)
@Mixin(value = ServerLoginPacketListenerImpl.class, priority = 1500)
public class SpongeFixMixin {
    @TargetHandler(
            mixin =
                    " org.spongepowered.common.mixin.core.server.network.ServerLoginPacketListenerImplMixin",
            name = " impl$fireAuthEventOffline",
            prefix = "handler")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void cancelFireAuthEventOffline(CallbackInfo originalCi, CallbackInfo ci) {
        ci.cancel();
    }
}
