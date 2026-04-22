package org.adde0109.pcf.mixin.v26_1.forwarding.modern;

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
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@AConstraint(mappings = Mappings.MOJANG, version = @Versions(min = MinecraftVersion.V20_2))
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final private Connection connection;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract void shadow$disconnect(Component details);
    @Shadow protected abstract void shadow$startClientVerification(GameProfile profile);
    @Shadow private GameProfile authenticatedProfile;
    // spotless:on

    @Override
    public @NonNull ConnectionBridge bridge$connection() {
        return (ConnectionBridge) this.connection;
    }

    @Override
    public void bridge$disconnect(final @NonNull Object reason) {
        this.shadow$disconnect((Component) reason);
    }

    @Override
    public void bridge$setGameProfile(final @NonNull GameProfile profile) {
        this.authenticatedProfile = profile;
    }

    @Override
    public void bridge$startClientVerification(final @NonNull GameProfile profile) {
        this.shadow$startClientVerification(profile);
    }

    @Override
    public void bridge$logger_info(final @NonNull String text, final Object... params) {
        LOGGER.info(text, params);
    }

    @Override
    public void bridge$logger_error(final @NonNull String text, final Object... params) {
        LOGGER.error(text, params);
    }
}
