package org.adde0109.pcf.mixin.v13_2.forge.forwarding.modern;

import static org.adde0109.pcf.forwarding.modern.ModernForwarding.handleHello;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.NetHandlerLoginServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.ITextComponent;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
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

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V13, max = MinecraftVersion.V13_2))
@Mixin(NetHandlerLoginServer.class)
public abstract class ServerLoginPacketListenerImplMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final public NetworkManager networkManager;
    @Shadow @Nullable private GameProfile loginGameProfile;
    @Shadow private NetHandlerLoginServer.LoginState currentLoginState;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract void shadow$disconnect(ITextComponent reason);
    @Unique private int pcf$velocityLoginMessageId = -1;
    // spotless:on

    // spotless:off
    @Inject(method = "processLoginStart", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 1,
            target = "Lnet/minecraft/network/NetHandlerLoginServer;currentLoginState:Lnet/minecraft/network/NetHandlerLoginServer$LoginState;"))
    // spotless:on
    private void onHandleHello(final @NonNull CallbackInfo ci) {
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
        return (ConnectionBridge) this.networkManager;
    }

    @Override
    public void bridge$disconnect(final @NonNull Object reason) {
        this.shadow$disconnect((ITextComponent) reason);
    }

    @Override
    public void bridge$setGameProfile(final @NonNull GameProfile profile) {
        this.loginGameProfile = profile;
    }

    @Override
    public void bridge$startClientVerification(final @NonNull GameProfile profile) {
        this.loginGameProfile = profile;
        this.currentLoginState = NetHandlerLoginServer.LoginState.NEGOTIATING;
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
