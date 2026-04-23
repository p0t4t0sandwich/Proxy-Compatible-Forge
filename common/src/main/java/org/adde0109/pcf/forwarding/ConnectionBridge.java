package org.adde0109.pcf.forwarding;

import dev.neuralnexus.taterapi.network.Protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

import org.adde0109.pcf.PCF;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;

public interface ConnectionBridge {
    String HANDLER_PACKET = "packet_handler";
    String HANDLER_SPLITTER = "splitter";
    String HANDLER_PREPENDER = "prepender";

    @NonNull InetSocketAddress bridge$address();

    void bridge$address(final @NonNull InetSocketAddress address);

    @NonNull Channel bridge$channel();

    @Nullable Object bridge$getPacketListener();

    @Nullable Protocol bridge$protocol();

    @NonNull Object bridge$disconnectPacket(final @NonNull Object reason);

    default void bridge$send(final @NonNull Object packet) {
        this.bridge$channel().writeAndFlush(packet).addListener(ConnectionBridge::errorListener);
    }

    default void bridge$disconnect(final @NonNull Object reason) {
        if (this.bridge$getPacketListener() instanceof ServerLoginPacketListenerBridge slpl) {
            slpl.bridge$disconnect(reason);
            return;
        }
        if (this.bridge$channel() != null && this.bridge$channel().isOpen()) {
            this.bridge$send(this.bridge$disconnectPacket(reason));
            this.bridge$channel().close().awaitUninterruptibly();
        }
    }

    /**
     * Injects the packet encoder and decoder into the pipeline to handle login query packets
     *
     * @param ctx the channel handler context
     */
    static void injectIntoPipeline(final @NonNull ChannelHandlerContext ctx) {
        if (ctx.pipeline().get(PacketDecoder.NAME) != null
                || ctx.pipeline().get(PacketEncoder.NAME) != null) {
            return;
        }
        PCF.logger.debug(
                "Injecting packet handlers into pipeline of " + ctx.channel().remoteAddress());
        ctx.channel()
                .pipeline()
                .addAfter(HANDLER_SPLITTER, PacketDecoder.NAME, new PacketDecoder())
                .addAfter(HANDLER_PREPENDER, PacketEncoder.NAME, new PacketEncoder());
    }

    /**
     * Listener for logging errors during packet handling
     *
     * @param future the future to check for success or failure
     */
    static void errorListener(Future<? super Void> future) {
        if (!future.isSuccess()) {
            PCF.logger.error("An error occurred during packet handling", future.cause());
        }
    }
}
