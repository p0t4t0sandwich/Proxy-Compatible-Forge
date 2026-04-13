package org.adde0109.pcf.forwarding.modern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;

public interface ConnectionBridge {
    @NonNull InetSocketAddress bridge$address();

    void bridge$address(final @NonNull InetSocketAddress address);

    void bridge$send(final @NonNull Object packet);

    @Nullable Object bridge$getPacketListener();
}
