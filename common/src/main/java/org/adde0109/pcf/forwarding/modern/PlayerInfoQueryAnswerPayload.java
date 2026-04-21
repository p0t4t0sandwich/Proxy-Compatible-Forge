package org.adde0109.pcf.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.VelocityProxy.createProfile;
import static org.adde0109.pcf.forwarding.modern.VelocityProxy.readSignerUuidOrElse;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.mc.world.entity.player.ProfilePublicKey;
import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.codec.StreamCodec;
import dev.neuralnexus.taterapi.network.protocol.PayloadType;
import dev.neuralnexus.taterapi.network.protocol.login.custom.CustomQueryAnswerPayload;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Payload response for the player info query.
 *
 * @param version the forwarding version used
 * @param address the forwarded client IP
 * @param profile the forwarded game profile
 * @param key the forwarded profile public key data
 * @param signer the signer's UUID
 */
public record PlayerInfoQueryAnswerPayload(
        VelocityProxy.Version version,
        @NonNull InetAddress address,
        @NonNull GameProfile profile,
        ProfilePublicKey.@Nullable Data key,
        @Nullable UUID signer)
        implements CustomQueryAnswerPayload {
    public static final StreamCodec<@NonNull FriendlyByteBuf, @NonNull PlayerInfoQueryAnswerPayload>
            STREAM_CODEC =
                    CustomQueryAnswerPayload.codec(
                            PlayerInfoQueryAnswerPayload::encode,
                            PlayerInfoQueryAnswerPayload::decode);
    public static final Type<PlayerInfoQueryAnswerPayload> TYPE =
            PayloadType.answer(PlayerInfoQueryAnswerPayload.class, STREAM_CODEC);

    private static @NonNull PlayerInfoQueryAnswerPayload decode(
            final @NonNull FriendlyByteBuf input) {
        final FriendlyByteBuf data = input.readPayload(); // TODO: See if this is finally removable
        final VelocityProxy.Version version = VelocityProxy.Version.from(data.readVarInt());
        final InetAddress address = data.readInetAddress();
        final UUID playerId = data.readUUID();
        final String playerName = data.readUtf(16);
        final GameProfile profile = createProfile(playerId, playerName, data);
        ProfilePublicKey.Data key = null;
        UUID signer = null;
        switch (version) {
            case MODERN_FORWARDING_WITH_KEY -> key = new ProfilePublicKey.Data(data);
            case MODERN_FORWARDING_WITH_KEY_V2 -> {
                key = new ProfilePublicKey.Data(data);
                signer = readSignerUuidOrElse(data, playerId);
            }
        }
        return new PlayerInfoQueryAnswerPayload(version, address, profile, key, signer);
    }

    private void encode(final @NonNull FriendlyByteBuf output) {
        throw new UnsupportedOperationException(
                this.getClass().getName() + " does not support serialization.");
    }

    @Override
    public @NonNull Type<PlayerInfoQueryAnswerPayload> type() {
        return TYPE;
    }
}
