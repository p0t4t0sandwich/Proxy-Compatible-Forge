package org.adde0109.pcf.mixin.v19_2.forge.forwarding;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.network.Protocol;

import io.netty.util.AttributeKey;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V17, max = MinecraftVersion.V20_1))
@Mixin(Connection.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    // spotless:off
    @Shadow @Final public static AttributeKey<ConnectionProtocol> ATTRIBUTE_PROTOCOL;
    @Shadow public abstract PacketListener shadow$getPacketListener();
    // spotless:on

    @Override
    public Protocol bridge$protocol() {
        final PacketListener listener = this.shadow$getPacketListener();
        if (listener == null) {
            return null;
        }
        return Protocol.fromLegacyId(this.bridge$channel().attr(ATTRIBUTE_PROTOCOL).get().getId());
    }
}
