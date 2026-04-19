package org.adde0109.pcf.mixin.v19_2.forge.forwarding.modern;

import static org.adde0109.pcf.v19_2.forge.forwarding.modern.ProfilePublicKeyDataAdapter.INSTANCE;

import dev.neuralnexus.taterapi.mc.world.entity.player.ProfilePublicKey;
import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.MetaAPI;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.SignatureValidator;

import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V19_1, max = MinecraftVersion.V19_2))
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplKeyV2Mixin
        implements ServerLoginPacketListenerBridge.KeyV2 {
    // spotless:off
    @Shadow private net.minecraft.world.entity.player.ProfilePublicKey.Data profilePublicKeyData;

    // ProfilePublicKey.ValidationException Doesn't exist on 1.19.1
    @SuppressWarnings("RedundantThrows")
    @Shadow private static net.minecraft.world.entity.player.ProfilePublicKey validatePublicKey(
            net.minecraft.world.entity.player.ProfilePublicKey.Data keyData,
            UUID signer,
            SignatureValidator validator,
            boolean enforceSecureProfile)
            throws Exception {
        throw new UnsupportedOperationException();
    }
    // spotless:on

    @Override
    public ProfilePublicKey.@Nullable Data bridge$profilePublicKeyData() {
        if (this.profilePublicKeyData == null) {
            return null;
        }
        return INSTANCE.encode(this.profilePublicKeyData).unwrap();
    }

    @Override
    public void bridge$setProfilePublicKeyData(
            final ProfilePublicKey.@Nullable Data publicKeyData) {
        if (publicKeyData == null) {
            this.profilePublicKeyData = null;
        } else {
            this.profilePublicKeyData = INSTANCE.decode(publicKeyData).unwrap();
        }
    }

    @Override
    public void bridge$validatePublicKey(
            final ProfilePublicKey.@Nullable Data keyData, final @Nullable UUID signer)
            throws Exception {
        MinecraftServer server = (MinecraftServer) MetaAPI.instance().server();
        validatePublicKey(
                keyData != null ? INSTANCE.decode(keyData).unwrap() : null,
                signer != null ? signer : UUID.randomUUID(),
                server.getServiceSignatureValidator(),
                server.enforceSecureProfile());
    }
}
