package org.adde0109.pcf.forwarding;

import static dev.neuralnexus.taterapi.network.protocol.login.ServerboundHelloPacket.MAX_NAME_LENGTH;

import static org.adde0109.pcf.forwarding.ConnectionBridge.HANDLER_PACKET;
import static org.adde0109.pcf.forwarding.Forwarding.handleClientIntention;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.PLAYER_NAME;
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
        final int id = data.readVarInt();
        final StringBuilder debugInfo =
                new StringBuilder("Received ")
                        .append(connection.bridge$protocol())
                        .append(" packet with ID 0x")
                        .append(Integer.toHexString(id))
                        .append(" from ")
                        .append(ctx.channel().remoteAddress());

        switch (connection.bridge$protocol()) {
            case HANDSHAKING -> {
                //noinspection SwitchStatementWithTooFewBranches
                switch (id) {
                    case 0x00 -> {
                        debugInfo.append(", Handling ClientIntentionPacket");

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

                        // TODO: Resolve out of band PLAY accept teleportation packet
                        //  Effects: 1.20.2 - 1.20.4
                        if (data.readableBytes() == 1
                                && Constraint.range(
                                                MinecraftVersions.V20_2, MinecraftVersions.V20_4)
                                        .result()) {
                            msg.readerIndex(readerIndex);
                            debugInfo
                                    .append(
                                            ", Deferring out-of-band PLAY accept teleportation packet:")
                                    .append("\n - Packet Length: ")
                                    .append(data.readableBytes())
                                    .append("\n - Packet data: 0x")
                                    .append(ByteBufUtil.prettyHexDump(data));
                            break;
                        }
                        debugInfo.append(", Handling ServerBoundHelloPacket");

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
                        debugInfo.append(", Handling ServerboundCustomQueryAnswerPacket");

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
        if (PCF.instance().debug().enabled()) {
            PCF.logger.debug(debugInfo.toString());
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
