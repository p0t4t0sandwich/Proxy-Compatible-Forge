package org.adde0109.pcf.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.ConnectionBridge.PACKET_HANDLER;
import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleCustomQueryPacket;

import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.Protocol;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;
import dev.neuralnexus.taterapi.network.protocol.login.ServerboundCustomQueryAnswerPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import org.adde0109.pcf.PCF;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {
    public static final String NAME = "pcf-decoder";

    @SuppressWarnings("RedundantThrows")
    @Override
    protected void decode(
            final @NonNull ChannelHandlerContext ctx,
            final @NonNull ByteBuf msg,
            final List<Object> out)
            throws Exception {
        if (!msg.isReadable()) {
            return;
        }
        final ConnectionBridge connection =
                ((ConnectionBridge) ctx.channel().pipeline().get(PACKET_HANDLER));
        if (!(connection.bridge$protocol() == Protocol.LOGIN)) {
            out.add(msg.retain());
            return;
        }
        if (!(connection.bridge$getPacketListener()
                instanceof ServerLoginPacketListenerBridge slpl)) {
            out.add(msg.retain());
            return;
        }

        final int readerIndex = msg.readerIndex();
        final FriendlyByteBuf data = new FriendlyByteBuf(msg);
        final int id = data.readVarInt();
        PCF.logger.debug(
                "Received packet with ID 0x"
                        + Integer.toHexString(id)
                        + " from "
                        + ctx.channel().remoteAddress());

        //noinspection SwitchStatementWithTooFewBranches
        switch (id) {
            case 0x02 -> {
                final ServerboundCustomQueryAnswerPacket packet =
                        ServerboundCustomQueryAnswerPacket.STREAM_CODEC.decode(data);

                // Check if the packet should be handled
                if (packet.transactionId() != slpl.bridge$velocityLoginMessageId()) {
                    msg.readerIndex(readerIndex);
                    break;
                }
                PCF.logger.debug(
                        "Handling ServerboundCustomQueryAnswerPacket from "
                                + ctx.channel().remoteAddress());

                try {
                    handleCustomQueryPacket(slpl, packet);
                } catch (final ThrowingComponent e) {
                    slpl.bridge$disconnect(e.getComponent());
                } finally {
                    msg.clear();
                }
            }
            // Reset reader index for unhandled packets
            default -> msg.readerIndex(readerIndex);
        }

        if (msg.isReadable()) {
            out.add(msg.retain());
        }
    }
}
