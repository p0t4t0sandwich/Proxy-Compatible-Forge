package org.adde0109.pcf.forwarding.modern;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.mc.world.entity.player.ProfilePublicKey;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface ServerLoginPacketListenerBridge {
    int bridge$velocityLoginMessageId();

    void bridge$setVelocityLoginMessageId(final int id);

    @NonNull ConnectionBridge bridge$connection();

    void bridge$disconnect(final @NonNull Object reason);

    void bridge$setGameProfile(final @NonNull GameProfile profile);

    void bridge$startClientVerification(final @NonNull GameProfile profile);

    void bridge$logger_info(final @NonNull String text, final Object... params);

    void bridge$logger_error(final @NonNull String text, final Object... params);

    interface KeyV1 {
        void bridge$setPlayerProfilePublicKey(ProfilePublicKey.@Nullable Data publicKeyData);
    }

    interface KeyV2 {
        ProfilePublicKey.@Nullable Data bridge$profilePublicKeyData();

        void bridge$setProfilePublicKeyData(final ProfilePublicKey.@Nullable Data publicKeyData);

        void bridge$validatePublicKey(
                final ProfilePublicKey.@Nullable Data keyData, final @Nullable UUID signer)
                throws Exception;
    }
}
