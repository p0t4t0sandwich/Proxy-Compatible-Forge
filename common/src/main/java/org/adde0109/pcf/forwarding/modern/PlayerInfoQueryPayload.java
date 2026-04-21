package org.adde0109.pcf.forwarding.modern;

import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.codec.StreamCodec;
import dev.neuralnexus.taterapi.network.protocol.PayloadType;
import dev.neuralnexus.taterapi.network.protocol.login.custom.CustomQueryPayload;

import io.netty.buffer.ByteBuf;

import org.jspecify.annotations.NonNull;

/**
 * Payload for the player info query <br>
 * Adapted from <a
 * href="https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/network/protocol/login/ClientboundCustomQueryPacket.java.patch#L8-L15">PaperMC</a>
 *
 * @param data the buffer
 */
public record PlayerInfoQueryPayload(@NonNull ByteBuf data) implements CustomQueryPayload {
    public static final String IDENTIFIER = VelocityProxy.PLAYER_INFO_CHANNEL.toString();
    public static final StreamCodec<FriendlyByteBuf, PlayerInfoQueryPayload> STREAM_CODEC =
            CustomQueryPayload.codec(
                    PlayerInfoQueryPayload::encode, PlayerInfoQueryPayload::decode);
    public static final Type<PlayerInfoQueryPayload> TYPE =
            PayloadType.query(PlayerInfoQueryPayload.class, IDENTIFIER).codec(STREAM_CODEC).build();

    private static @NonNull PlayerInfoQueryPayload decode(final @NonNull FriendlyByteBuf buf) {
        return new PlayerInfoQueryPayload(buf.readPayload());
    }

    private void encode(final @NonNull FriendlyByteBuf buf) {
        buf.writePayload(this.data);
    }

    @Override
    public @NonNull Type<PlayerInfoQueryPayload> type() {
        return TYPE;
    }
}
