package org.adde0109.pcf.mixin.v16_5.forge.forwarding;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V14, max = MinecraftVersion.V16_5))
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final public Connection connection;
    @Shadow @Nullable private GameProfile gameProfile;
    @Shadow private ServerLoginPacketListenerImpl.State state;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract void shadow$disconnect(Component reason);
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
        this.gameProfile = profile;
    }

    @Override
    public void bridge$startClientVerification(final @NonNull GameProfile profile) {
        this.gameProfile = profile;
        this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING;
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
