package org.adde0109.pcf.forwarding.modern;

import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.protocol.login.ClientboundCustomQueryPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.adde0109.pcf.PCF;
import org.jspecify.annotations.NonNull;

public final class PacketEncoder extends MessageToByteEncoder<ClientboundCustomQueryPacket> {
    public static final String NAME = "pcf-encoder";

    @SuppressWarnings("RedundantThrows")
    @Override
    protected void encode(
            final @NonNull ChannelHandlerContext ctx,
            final @NonNull ClientboundCustomQueryPacket msg,
            final @NonNull ByteBuf buf)
            throws Exception {
        try {
            PCF.logger.debug(
                    "Encoding "
                            + msg.getClass().getSimpleName()
                            + " to "
                            + ctx.channel().remoteAddress());
            final FriendlyByteBuf data = new FriendlyByteBuf(buf);
            data.writeVarInt(0x04);
            ClientboundCustomQueryPacket.STREAM_CODEC.encode(data, msg);
        } catch (final Exception e) {
            PCF.logger.error("Failed to encode packet " + msg.getClass().getName(), e);
            throw e;
        }
    }
}
