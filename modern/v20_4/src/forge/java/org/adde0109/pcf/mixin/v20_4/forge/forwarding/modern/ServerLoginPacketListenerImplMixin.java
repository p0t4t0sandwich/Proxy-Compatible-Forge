package org.adde0109.pcf.mixin.v20_4.forge.forwarding.modern;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@AConstraint(mappings = Mappings.SEARGE, version = @Versions(min = MinecraftVersion.V17))
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final Connection connection;
    @Shadow public abstract void shadow$disconnect(Component reason);

    @AConstraint(version = @Versions(min = MinecraftVersion.V20_2))
    @Shadow abstract void shadow$startClientVerification(GameProfile profile);

    @AConstraint(version = @Versions(min = MinecraftVersion.V20_2))
    @Shadow private @Nullable GameProfile authenticatedProfile;
    // spotless:on

    @Override
    public @NonNull ConnectionBridge bridge$connection() {
        return (ConnectionBridge) this.connection;
    }

    @Override
    public void bridge$disconnect(final @NonNull Object reason) {
        this.shadow$disconnect((Component) reason);
    }

    @AConstraint(version = @Versions(min = MinecraftVersion.V20_2))
    @Override
    public void bridge$setGameProfile(final @NonNull GameProfile profile) {
        this.authenticatedProfile = profile;
    }

    @AConstraint(version = @Versions(min = MinecraftVersion.V20_2))
    @Override
    public void bridge$startClientVerification(final @NonNull GameProfile profile) {
        this.shadow$startClientVerification(profile);
    }
}
