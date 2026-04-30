package org.adde0109.pcf.forwarding;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;
import static dev.neuralnexus.taterapi.network.chat.Component.translatable;

import static org.adde0109.pcf.forwarding.ReflectionUtils.attributeKeyValueOf;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.event.Cancellable;
import dev.neuralnexus.taterapi.mc.server.players.NameAndId;
import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;
import dev.neuralnexus.taterapi.network.protocol.handshake.ClientIntent;
import dev.neuralnexus.taterapi.network.protocol.handshake.ClientIntentionPacket;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.legacy.LegacyForwarding;
import org.adde0109.pcf.forwarding.modern.ModernForwarding;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.regex.Pattern;

public final class Forwarding {
    public static final AttributeKey<Object> DEFERRED_DISCONNECT =
            attributeKeyValueOf("pcf-deferred-disconnect");

    public static final Object PLAYER_INFO_ERR = literal("Unable to verify player details.");

    private static final Object FAILED_TO_VERIFY =
            translatable("multiplayer.disconnect.unverified_username");
    private static final Object REJECTED_PROXY_ERR = literal("Unapproved proxy host.");

    public static final Pattern HOST_PATTERN = Pattern.compile("[0-9a-f.:]{0,45}");

    /**
     * Handle the client intention packet and extract player info
     *
     * @param connection The connection
     * @param data The packet buffer
     */
    public static void handleClientIntention(
            final @NonNull ConnectionBridge connection, final @NonNull FriendlyByteBuf data) {
        final Channel channel = connection.bridge$channel();
        try {
            switch (PCF.instance().forwarding().mode()) {
                case LEGACY, BUNGEEGUARD ->
                        LegacyForwarding.handleClientIntention(connection, data);
                case MODERN -> ModernForwarding.handleClientIntention(connection, data);
            }
        } catch (final ThrowingComponent e) {
            channel.attr(DEFERRED_DISCONNECT).set(e.getComponent());
        } catch (final Exception e) {
            e.printStackTrace();
            channel.attr(DEFERRED_DISCONNECT).set(PLAYER_INFO_ERR);
        }
    }

    /**
     * Abstract implementation of the hello packet handler
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param ci The callback info
     */
    public static void handleHello(
            final @NonNull ServerLoginPacketListenerBridge slpl, final @NonNull CallbackInfo ci) {
        try {
            final ConnectionBridge connection = slpl.bridge$connection();
            final Channel channel = connection.bridge$channel();

            // Handle any deferred disconnects from the handshake phase
            final Object deferredDisconnect = channel.attr(DEFERRED_DISCONNECT).getAndSet(null);
            if (deferredDisconnect != null) {
                throw new ThrowingComponent(deferredDisconnect);
            }

            switch (PCF.instance().forwarding().mode()) {
                case LEGACY, BUNGEEGUARD -> LegacyForwarding.handleHello(slpl, ci);
                case MODERN -> ModernForwarding.handleHello(slpl, ci);
            }
        } catch (final ThrowingComponent e) {
            slpl.bridge$disconnect(e.getComponent());
        } catch (final Exception e) {
            e.printStackTrace();
            slpl.bridge$disconnect(FAILED_TO_VERIFY);
        } finally {
            ci.cancel();
        }
    }

    /**
     * Rewrite ClientIntention packet so the player can enter the login phase.
     *
     * @param channel The connection's Netty channel
     * @param protocolVersion The protocol version from the original packet
     * @param hostName The hostname from the original packet
     * @param hostPort The port from the original packet
     * @param intention The client intention from the original packet
     * @param data The packet buffer to write the new packet into
     */
    public static void rewriteClientIntention(
            final @NonNull Channel channel,
            final int protocolVersion,
            final String hostName,
            final int hostPort,
            final ClientIntent intention,
            final @NonNull FriendlyByteBuf data) {
        final ClientIntentionPacket newPacket =
                new ClientIntentionPacket(protocolVersion, hostName, hostPort, intention);
        data.clear();
        data.writeVarInt(0x00);
        ClientIntentionPacket.STREAM_CODEC.encode(data, newPacket);
        PCF.logger.debug("Rewrote ClientIntentionPacket for " + channel.remoteAddress());
    }

    /**
     * Checks if the connection is coming from an approved proxy host
     *
     * @param connection The connection
     */
    public static void checkProxy(final @NonNull ConnectionBridge connection) {
        final Collection<String> approved = PCF.instance().forwarding().approvedProxyHosts();
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
                throw new ThrowingComponent(REJECTED_PROXY_ERR);
            }
        }
    }

    /**
     * Set the connection's address to the forwarded address from the proxy.
     *
     * @param connection The connection
     * @param forwardedAddress The forwarded address
     */
    public static void ipForwarding(
            final @NonNull ConnectionBridge connection,
            final @NonNull InetAddress forwardedAddress) {
        final int port = connection.bridge$address().getPort();
        final InetSocketAddress address = new InetSocketAddress(forwardedAddress, port);
        connection.bridge$address(address);
    }

    /**
     * Pre-login handler that invokes registered {@link PreLoginHandler}s
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param profile The player's GameProfile
     */
    public static void preLogin(
            final @NonNull ServerLoginPacketListenerBridge slpl,
            final @NonNull GameProfile profile) {
        final Cancellable c = Cancellable.simple();
        try {
            for (final PreLoginHandler processor : PreLoginHandler.HANDLERS) {
                processor.process(slpl, profile, c);
                if (c.cancelled()) {
                    break;
                }
            }
        } catch (final ThrowingComponent e) {
            throw e;
        } catch (final Exception e) {
            final NameAndId nameAndId = new NameAndId(profile);
            PCF.logger.warn("Exception while forwarding user " + nameAndId.name());
            e.printStackTrace();
            throw new ThrowingComponent(FAILED_TO_VERIFY, e);
        } finally {
            c.cancel();
        }
    }
}
