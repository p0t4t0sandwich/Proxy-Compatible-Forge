package org.adde0109.pcf.v19_2.forge.forwarding.modern;

import dev.neuralnexus.taterapi.mc.world.entity.player.ProfilePublicKey;
import dev.neuralnexus.taterapi.serialization.Result;
import dev.neuralnexus.taterapi.serialization.codecs.ReversibleCodec;

public final class ProfilePublicKeyDataAdapter
        implements ReversibleCodec<
                net.minecraft.world.entity.player.ProfilePublicKey.Data, ProfilePublicKey.Data> {
    public static final ProfilePublicKeyDataAdapter INSTANCE = new ProfilePublicKeyDataAdapter();

    @Override
    public Result<ProfilePublicKey.Data> encode(
            final net.minecraft.world.entity.player.ProfilePublicKey.Data object) {
        return Result.success(
                new ProfilePublicKey.Data(object.expiresAt(), object.key(), object.keySignature()));
    }

    @Override
    public Result<net.minecraft.world.entity.player.ProfilePublicKey.Data> decode(
            final ProfilePublicKey.Data object) {
        return Result.success(
                new net.minecraft.world.entity.player.ProfilePublicKey.Data(
                        object.expiresAt(), object.key(), object.keySignature()));
    }
}
