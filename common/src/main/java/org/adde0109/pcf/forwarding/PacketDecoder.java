package org.adde0109.pcf.forwarding;

import static dev.neuralnexus.taterapi.network.protocol.login.ServerboundHelloPacket.MAX_NAME_LENGTH;

import static org.adde0109.pcf.forwarding.ConnectionBridge.HANDLER_PACKET;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.PLAYER_NAME;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.handleClientIntention;
import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleCustomQueryAnswer;

import dev.neuralnexus.taterapi.meta.Constraint;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;
import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.Protocol;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import org.adde0109.pcf.PCF;
import org.jspecify.annotations.NonNull;

import java.nio.channels.ClosedChannelException;
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

        // Hexdump the packet for debugging
        if (PCF.instance().debug().enabled()) {
            final int id = data.readVarInt();
            msg.readerIndex(readerIndex);
            final String hexDump = ByteBufUtil.prettyHexDump(data);

            final StringBuilder sb = new StringBuilder();
            if (connection.bridge$getPacketListener() != null) {
                //noinspection DataFlowIssue
                sb.append("\nPacket listener: ")
                        .append(connection.bridge$getPacketListener().getClass().getName());
            } else {
                sb.append("\nPacket listener: NONE");
            }
            sb.append("\nPacket length: ").append(data.readableBytes());

            // Don't want to leak the secret nor the encrypted payload in the debug log
            if (!(connection.bridge$protocol() == Protocol.LOGIN && id == 0x02)
                    && !(connection.bridge$protocol() == Protocol.HANDSHAKING
                            && PCF.instance().forwarding().mode().isLegacy())) {
                sb.append("\nPacket data:\n").append(hexDump);
            }
            PCF.logger.debug(sb.toString());
        }

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

                        // Used to avoid a second 0x0 packet with 2 bytes consisting of [0x00, 0x03]
                        // Not entirely sure of the cause
                        if (data.readableBytes() == 1
                                && Constraint.range(
                                                MinecraftVersions.V20_2, MinecraftVersions.V20_4)
                                        .result()) {
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
                            if (handled) {
                                msg.clear();
                            } else {
                                msg.readerIndex(readerIndex);
                            }
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

    @Override
    public void exceptionCaught(
            final @NonNull ChannelHandlerContext ctx, final @NonNull Throwable cause)
            throws Exception {
        if (cause instanceof ClosedChannelException) {
            super.exceptionCaught(ctx, cause);
            return;
        }
        PCF.logger.error(
                "Exception in PacketDecoder for "
                        + ctx.channel().remoteAddress()
                        + ": "
                        + cause.getMessage(),
                cause);
        super.exceptionCaught(ctx, cause);
    }
}
