package org.adde0109.pcf.forwarding.modern;

import dev.neuralnexus.taterapi.network.Protocol;

import io.netty.channel.Channel;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;

public interface ConnectionBridge {
    String PACKET_HANDLER = "packet_handler";

    @NonNull InetSocketAddress bridge$address();

    void bridge$address(final @NonNull InetSocketAddress address);

    @NonNull Channel bridge$channel();

    @Nullable Object bridge$getPacketListener();

    @Nullable Protocol bridge$protocol();

    default void bridge$send(final @NonNull Object packet) {
        this.bridge$channel().writeAndFlush(packet).addListener(ModernForwarding::errorListener);
    }
}
