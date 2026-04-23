package org.adde0109.pcf.mixin.v13_2.forge.forwarding;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.network.Protocol;

import io.netty.util.AttributeKey;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.SPacketDisconnectLogin;
import net.minecraft.util.text.ITextComponent;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V13_2))
@Mixin(NetworkManager.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow @Final public static AttributeKey<EnumConnectionState> PROTOCOL_ATTRIBUTE_KEY;
    // spotless:on

    @Override
    public Protocol bridge$protocol() {
        final Object listener = this.bridge$getPacketListener();
        if (listener == null) {
            return null;
        }
        return Protocol.fromLegacyId(
                this.bridge$channel().attr(PROTOCOL_ATTRIBUTE_KEY).get().getId());
    }

    @AConstraint(
            mappings = Mappings.SEARGE,
            version = @Versions(min = MinecraftVersion.V13, max = MinecraftVersion.V13_2))
    @Override
    public @NonNull Object bridge$disconnectPacket(final @NonNull Object reason) {
        return new SPacketDisconnectLogin((ITextComponent) reason);
    }
}
