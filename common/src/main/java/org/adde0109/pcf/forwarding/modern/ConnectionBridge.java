package org.adde0109.pcf.forwarding.modern;

import io.netty.channel.Channel;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;

public interface ConnectionBridge {
    @NonNull InetSocketAddress bridge$address();

    void bridge$address(final @NonNull InetSocketAddress address);

    @NonNull Channel bridge$channel();

    @Nullable Object bridge$getPacketListener();

    default void bridge$send(final @NonNull Object packet) {
        this.bridge$channel().writeAndFlush(packet).addListener(ModernForwarding::errorListener);
    }
}
