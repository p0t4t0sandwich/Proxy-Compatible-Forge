package org.adde0109.pcf.forwarding.modern;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;
import static dev.neuralnexus.taterapi.network.chat.Component.translatable;

import static org.adde0109.pcf.forwarding.ReflectionUtils.enforceSecureProfile;
import static org.adde0109.pcf.forwarding.modern.VelocityProxy.MODERN_MAX_VERSION;
import static org.adde0109.pcf.forwarding.modern.VelocityProxy.PLAYER_INFO_PAYLOAD;
import static org.adde0109.pcf.forwarding.modern.VelocityProxy.checkIntegrity;

import dev.neuralnexus.taterapi.event.Cancellable;
import dev.neuralnexus.taterapi.mc.server.players.NameAndId;
import dev.neuralnexus.taterapi.meta.Constraint;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;
import dev.neuralnexus.taterapi.network.protocol.login.ClientboundCustomQueryPacket;
import dev.neuralnexus.taterapi.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import dev.neuralnexus.taterapi.network.protocol.login.custom.CustomQueryAnswerPayload;

import io.netty.handler.codec.DecoderException;
import io.netty.util.AttributeKey;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.Mode;
import org.adde0109.pcf.forwarding.PreLoginHandler;
import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for modern forwarding handling. <br>
 * Adapted from: <br>
 * <a
 * href="https://github.com/PaperMC/Paper-archive/blob/bef2c9d005bdd039f188ee53094a928e76bd8e59/patches/server/0273-Add-Velocity-IP-Forwarding-Support.patch">Paper
 * 1.19.2</a> <br>
 * <a
 * href="https://github.com/PaperMC/Paper-archive/blob/4074d4ee99a75ad005b05bfba8257e55beeb335f/patches/server/0884-Add-Velocity-IP-Forwarding-Support.patch">Paper
 * 1.19.3</a> <br>
 * <a
 * href="https://github.com/PaperMC/Paper-archive/blob/ver/1.19.4/patches/server/0874-Add-Velocity-IP-Forwarding-Support.patch">Paper
 * 1.19.4</a> <br>
 * <a
 * href="https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Paper
 * 1.20.x</a>
 */
public final class ModernForwarding {
    public static final AttributeKey<Integer> LOGIN_MESSAGE_ID =
            AttributeKey.valueOf("pcf-login-message-id");

    private static final Object REJECTED_PROXY_ERR = literal("Unapproved proxy host.");

    /**
     * Abstract implementation of the hello packet handler
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param ci The callback info
     */
    public static void handleHello(
            final @NonNull ServerLoginPacketListenerBridge slpl, final @NonNull CallbackInfo ci) {
        if (!PCF.instance().forwarding().enabled()
                || !PCF.instance().forwarding().mode().equals(Mode.MODERN)) {
            return;
        }
        final ConnectionBridge connection = slpl.bridge$connection();

        final List<String> approved = PCF.instance().forwarding().approvedProxyHosts();
        if (!approved.isEmpty()) {
            final InetSocketAddress address = connection.bridge$address();
            final String host = address.getHostString();
            final String ip = address.getAddress().getHostAddress();
            if (!approved.contains(host) && !approved.contains(ip)) {
                PCF.logger.warn(
                        "Rejected connection from unapproved proxy host: "
                                + host
                                + " (IP: "
                                + ip
                                + ")");
                slpl.bridge$disconnect(REJECTED_PROXY_ERR);
                ci.cancel();
                return;
            }
        }

        final int messageId = ThreadLocalRandom.current().nextInt();
        connection.bridge$channel().attr(LOGIN_MESSAGE_ID).set(messageId);
        connection.bridge$send(new ClientboundCustomQueryPacket(messageId, PLAYER_INFO_PAYLOAD));
        PCF.logger.debug("Sent Forward Request");
        ci.cancel();
    }

    private static final Object DIRECT_CONNECT_ERR =
            literal("This server requires you to connect with Velocity.");
    private static final Object EMPTY_PAYLOAD_ERR =
            literal("Received empty player info payload from the proxy.");
    private static final Object PLAYER_INFO_ERR = literal("Unable to verify player details.");
    private static final Object FAILED_TO_VERIFY =
            translatable("multiplayer.disconnect.unverified_username");
    private static final Object MISSING_PROFILE_PUBLIC_KEY =
            translatable("multiplayer.disconnect.missing_public_key");
    private static final Object INVALID_SIGNATURE =
            translatable("multiplayer.disconnect.invalid_public_key_signature");

    /**
     * Abstract implementation of the custom query packet handler
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param packet The Minecraft packet
     */
    public static void handleCustomQueryPacket(
            final @NonNull ServerLoginPacketListenerBridge slpl,
            final @NonNull ServerboundCustomQueryAnswerPacket packet) {
        final CustomQueryAnswerPayload.Raw rawPayload =
                packet.payload() instanceof CustomQueryAnswerPayload.Raw raw ? raw : null;

        // Validate payload presence
        if (rawPayload == null) {
            throw new ThrowingComponent(DIRECT_CONNECT_ERR);
        } else if (rawPayload.data().readableBytes() == 0) {
            PCF.logger.error(
                    "Received empty forwarding payload. Has Velocity been configured to use modern forwarding?");
            throw new ThrowingComponent(EMPTY_PAYLOAD_ERR);
        }
        PCF.logger.debug("Received Forward Response");

        // Validate data
        try {
            if (!checkIntegrity(rawPayload.data())) {
                if (Constraint.noGreaterThan(MinecraftVersions.V12_2).result()) {
                    PCF.logger.error(
                            "Ensure the `forwarding.secret` setting's value in PCF's config file doesn't have quotes around it!");
                }
                throw new ThrowingComponent(PLAYER_INFO_ERR);
            }
        } catch (final AssertionError e) {
            if ((e.getCause() instanceof InvalidKeyException
                            && PCF.instance().forwarding().secret().isBlank())
                    || (e.getCause() instanceof IllegalArgumentException
                            && e.getCause().getMessage().contains("Empty key"))) {
                PCF.logger.error(
                        "Please configure the `forwarding.secret` setting in PCF's config file!");
            } else {
                PCF.logger.error("An error occurred while validating player details: ", e);
            }
            throw new ThrowingComponent(PLAYER_INFO_ERR, e);
        }
        PCF.logger.debug("Player-data validated!");

        // Decode payload
        final PlayerInfoQueryAnswerPayload payload =
                PlayerInfoQueryAnswerPayload.TYPE.codec().decode(rawPayload.data());

        // Validate version
        final VelocityProxy.Version version = payload.version();
        if (version.id() > MODERN_MAX_VERSION) {
            throw new IllegalStateException(
                    "Unsupported forwarding version "
                            + version
                            + ", wanted up to "
                            + MODERN_MAX_VERSION);
        }
        PCF.logger.debug("Using modern forwarding version: " + version);

        // Apply IP forwarding
        final int port = slpl.bridge$connection().bridge$address().getPort();
        final InetSocketAddress address = new InetSocketAddress(payload.address(), port);
        slpl.bridge$connection().bridge$address(address);

        // Handle profile key
        switch (version) {
            case MODERN_DEFAULT -> { // Clear key on 1.19.1 - 1.19.2 if using MODERN_DEFAULT
                if (Constraint.range(MinecraftVersions.V19_1, MinecraftVersions.V19_2).result()) {
                    ((ServerLoginPacketListenerBridge.KeyV2) slpl)
                            .bridge$setProfilePublicKeyData(null);
                }
            }
            case MODERN_FORWARDING_WITH_KEY -> { // 1.19 forwarding with key v1
                boolean enforceSecureProfile = enforceSecureProfile();
                try {
                    if (enforceSecureProfile && payload.key() == null) {
                        throw new ThrowingComponent(MISSING_PROFILE_PUBLIC_KEY);
                    }
                    ((ServerLoginPacketListenerBridge.KeyV1) slpl)
                            .bridge$setPlayerProfilePublicKey(payload.key());
                } catch (final DecoderException e) {
                    PCF.logger.error("Public key read failed.", e);
                    if (enforceSecureProfile) {
                        throw new ThrowingComponent(INVALID_SIGNATURE, e);
                    }
                }
            }
            case MODERN_FORWARDING_WITH_KEY_V2 -> { // 1.19.1 - 1.19.2 forwarding with key v2
                final ServerLoginPacketListenerBridge.KeyV2 keyV2 =
                        (ServerLoginPacketListenerBridge.KeyV2) slpl;
                if (keyV2.bridge$profilePublicKeyData() == null) {
                    try {
                        keyV2.bridge$validatePublicKey(payload.key(), payload.signer());
                        keyV2.bridge$setProfilePublicKeyData(payload.key());
                    } catch (final Exception e) {
                        slpl.bridge$logger_error(
                                "Failed to validate profile key: {}", e.getMessage());
                        throw new ThrowingComponent(INVALID_SIGNATURE, e);
                    }
                }
            }
        }

        // Proceed with login
        try {
            final Cancellable cancellable = Cancellable.simple();
            for (final PreLoginHandler processor : PreLoginHandler.HANDLERS) {
                processor.process(slpl, payload.profile(), cancellable);
                if (cancellable.cancelled()) {
                    break;
                }
            }
        } catch (final Exception e) {
            final NameAndId nameAndId = new NameAndId(payload.profile());
            PCF.logger.warn("Exception while forwarding user " + nameAndId.name());
            e.printStackTrace();
            throw new ThrowingComponent(FAILED_TO_VERIFY, e);
        }
    }
}
