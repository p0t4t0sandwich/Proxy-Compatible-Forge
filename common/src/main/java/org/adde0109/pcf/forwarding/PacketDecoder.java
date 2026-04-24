package org.adde0109.pcf.forwarding;

import static dev.neuralnexus.taterapi.network.protocol.login.ServerboundHelloPacket.MAX_NAME_LENGTH;

import static org.adde0109.pcf.forwarding.ConnectionBridge.HANDLER_PACKET;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.PLAYER_NAME;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.handleClientIntention;
import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleCustomQueryAnswer;

import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.Protocol;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;

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

        if (connection.bridge$protocol() != Protocol.HANDSHAKING
                && connection.bridge$protocol() != Protocol.LOGIN) {
            out.add(msg.retain());
            return;
        }

        final int readerIndex = msg.readerIndex();
        final FriendlyByteBuf data = new FriendlyByteBuf(msg);
        final int id = data.readVarInt();
        PCF.logger.debug(
                "Received "
                        + connection.bridge$protocol()
                        + " packet with ID 0x"
                        + Integer.toHexString(id)
                        + " from "
                        + ctx.channel().remoteAddress());

        switch (connection.bridge$protocol()) {
            case HANDSHAKING -> {
                if (!PCF.instance().forwarding().mode().isLegacy()) {
                    msg.readerIndex(readerIndex);
                    break;
                }

                //noinspection SwitchStatementWithTooFewBranches
                switch (id) {
                    case 0x00 -> {
                        PCF.logger.debug(
                                "Handling ClientIntentionPacket from "
                                        + ctx.channel().remoteAddress());

                        // Rewrite the packet
                        handleClientIntention(connection, data);
                        msg.readerIndex(readerIndex);
                    }
                    default -> msg.readerIndex(readerIndex);
                }
            }
            case LOGIN -> {
                if (!(connection.bridge$getPacketListener()
                        instanceof ServerLoginPacketListenerBridge slpl)) {
                    msg.readerIndex(readerIndex);
                    break;
                }

                switch (id) {
                    case 0x00 -> {
                        if (!PCF.instance().forwarding().mode().isLegacy()) {
                            msg.readerIndex(readerIndex);
                            break;
                        }
                        PCF.logger.debug(
                                "Handling ServerBoundHelloPacket from "
                                        + ctx.channel().remoteAddress());

                        // Save player name
                        final String name = data.readUtf(MAX_NAME_LENGTH);
                        ctx.channel().attr(PLAYER_NAME).set(name);
                        msg.readerIndex(readerIndex);
                    }
                    case 0x02 -> {
                        if (!PCF.instance().forwarding().mode().isModern()) {
                            msg.readerIndex(readerIndex);
                            break;
                        }
                        PCF.logger.debug(
                                "Handling ServerboundCustomQueryAnswerPacket from "
                                        + ctx.channel().remoteAddress());

                        boolean handled = false;
                        try {
                            handled = handleCustomQueryAnswer(slpl, data);
                        } catch (final ThrowingComponent e) {
                            handled = true;
                            slpl.bridge$disconnect(e.getComponent());
                        } finally {
                            if (handled) msg.clear();
                        }
                    }
                    default -> msg.readerIndex(readerIndex);
                }
            }
            case null, default -> msg.readerIndex(readerIndex);
        }

        if (msg.isReadable()) {
            out.add(msg.retain());
        }
    }
}
