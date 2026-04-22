package org.adde0109.pcf.mixin.v26_1.forwarding.legacy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// https://github.com/caunt/BungeeForge/blob/1.20.2/src/main/java/ua/caunt/bungeeforge/mixin/network/protocol/handshake/ClientIntentionPacket.java#L28
@Mixin(ClientIntentionPacket.class)
public final class ClientIntentionPacketMixin {
    // spotless:off
    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/FriendlyByteBuf;readUtf(I)Ljava/lang/String;"))
    // spotless:on
    private static String onConstruct(final FriendlyByteBuf input, int length) {
        return input.readUtf(Short.MAX_VALUE);
    }
}
