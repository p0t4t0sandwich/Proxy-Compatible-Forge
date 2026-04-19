package org.adde0109.pcf.forwarding.modern;

import static dev.neuralnexus.taterapi.network.FriendlyByteBuf.readVarInt;

import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleCustomQueryPacket;

import dev.neuralnexus.taterapi.event.Cancellable;
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

    private final ConnectionBridge connection;

    public PacketDecoder(final @NonNull ConnectionBridge connection) {
        this.connection = connection;
    }

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
        if (!(this.connection.bridge$getPacketListener()
                instanceof ServerLoginPacketListenerBridge slpl)) {
            out.add(msg.retain());
            return;
        }

        final int readerIndex = msg.readerIndex();
        final int id = readVarInt(msg);
        PCF.logger.debug(
                "Received packet with ID 0x"
                        + Integer.toHexString(id)
                        + " from "
                        + ctx.channel().remoteAddress());

        //noinspection SwitchStatementWithTooFewBranches
        switch (id) {
            case 0x02 -> {
                final ServerboundCustomQueryAnswerPacket packet =
                        ServerboundCustomQueryAnswerPacket.STREAM_CODEC.decode(msg);

                // Check if the packet should be handled
                if (packet.transactionId() != slpl.bridge$velocityLoginMessageId()) {
                    msg.readerIndex(readerIndex);
                    break;
                }
                PCF.logger.debug(
                        "Handling ServerboundCustomQueryAnswerPacket from "
                                + ctx.channel().remoteAddress());

                final CancellableCallback callback = new CancellableCallback(msg::clear);
                try {
                    handleCustomQueryPacket(slpl, packet, callback);
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

    private static class CancellableCallback implements Cancellable {
        private boolean cancelled = false;
        private final Runnable action;

        public CancellableCallback(final @NonNull Runnable action) {
            this.action = action;
        }

        @Override
        public boolean cancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(final boolean cancelled) {
            if (this.cancelled) return;
            this.cancelled = cancelled;
            if (cancelled) {
                this.action.run();
            }
        }
    }
}
