package org.adde0109.pcf.mixin.v16_5.forge.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleHello;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.modern.ConnectionBridge;
import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@AConstraint(mappings = Mappings.LEGACY_SEARGE, version = @Versions(min = MinecraftVersion.V14))
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final public Connection connection;
    @Shadow @Nullable private GameProfile gameProfile;
    @Shadow private ServerLoginPacketListenerImpl.State state;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract void shadow$disconnect(Component reason);
    @Unique private int pcf$velocityLoginMessageId = -1;
    // spotless:on

    // spotless:off
    @AConstraint(
            platform = {Platform.ARCLIGHT, Platform.CATSERVER, Platform.MAGMA, Platform.MOHIST}, invert = true)
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;state:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"))
    // spotless:on
    private void onHandleHello(final @NonNull CallbackInfo ci) {
        handleHello(this, ci);
    }

    /**
     * Arclight - Overwrites the method <br>
     * CatServer, Magma, Mohist - Patches alter the method in an incompatible manner
     */
    @AConstraint(
            platform = {Platform.ARCLIGHT, Platform.CATSERVER, Platform.MAGMA, Platform.MOHIST})
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "HEAD"))
    private void onHandleHelloHybrid(final @NonNull CallbackInfo ci) {
        Validate.validState(
                this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
        handleHello(this, ci);
    }

    @Override
    public int bridge$velocityLoginMessageId() {
        return this.pcf$velocityLoginMessageId;
    }

    @Override
    public void bridge$setVelocityLoginMessageId(final int id) {
        this.pcf$velocityLoginMessageId = id;
    }

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
