package org.adde0109.pcf.forwarding;

import static org.adde0109.pcf.forwarding.ConnectionBridge.HANDLER_PACKET;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.PLAYER_NAME;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.handleClientIntentionPacket;
import static org.adde0109.pcf.forwarding.modern.ModernForwarding.LOGIN_MESSAGE_ID;
import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleCustomQueryPacket;

import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
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
                ((ConnectionBridge) ctx.channel().pipeline().get(HANDLER_PACKET));

        switch (connection.bridge$protocol()) {
            case null -> {}
            case HANDSHAKING -> {
                if (!PCF.instance().forwarding().mode().isLegacy()) {
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
                    case 0x00 -> {
                        PCF.logger.debug(
                                "Handling ClientIntentionPacket from "
                                        + ctx.channel().remoteAddress());

                        // Rewrite the packet
                        handleClientIntentionPacket(connection, data);

                        // Reset reader index and pass it along
                        msg.readerIndex(readerIndex);
                    }
                    // Reset reader index for unhandled packets
                    default -> msg.readerIndex(readerIndex);
                }
            }
            case LOGIN -> {
                // TODO: Clean this up
                if (PCF.instance().forwarding().mode().isLegacy()) {
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
                        case 0x00 -> {
                            PCF.logger.debug(
                                    "Handling ServerBoundHello from "
                                            + ctx.channel().remoteAddress());
                            final String name = data.readUtf(16);
                            ctx.channel().attr(PLAYER_NAME).set(name);
                            msg.readerIndex(readerIndex);
                        }
                        default -> msg.readerIndex(readerIndex);
                    }
                    break;
                }

                if (!PCF.instance().forwarding().mode().isModern()) {
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
                        if (packet.transactionId()
                                != connection.bridge$channel().attr(LOGIN_MESSAGE_ID).get()) {
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
            }
            default -> {
                out.add(msg.retain());
                return;
            }
        }

        if (msg.isReadable()) {
            out.add(msg.retain());
        }
    }
}
