package org.adde0109.pcf.mixin.v7_10.forge.forwarding;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import io.netty.handler.codec.MessageToByteEncoder;

import net.minecraft.network.Packet;
import net.minecraft.util.MessageSerializer;

import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("rawtypes")
@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V7_10))
@Mixin(MessageSerializer.class)
public abstract class MessageSerializerMixin extends MessageToByteEncoder {
    @SuppressWarnings("RedundantThrows")
    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return msg instanceof Packet;
    }
}
