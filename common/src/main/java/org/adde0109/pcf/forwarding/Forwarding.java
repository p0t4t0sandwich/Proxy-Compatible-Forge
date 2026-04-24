package org.adde0109.pcf.forwarding;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;
import static dev.neuralnexus.taterapi.network.chat.Component.translatable;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.event.Cancellable;
import dev.neuralnexus.taterapi.mc.server.players.NameAndId;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.legacy.LegacyForwarding;
import org.adde0109.pcf.forwarding.modern.ModernForwarding;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

public final class Forwarding {
    public static final Object PLAYER_INFO_ERR = literal("Unable to verify player details.");

    private static final Object FAILED_TO_VERIFY =
            translatable("multiplayer.disconnect.unverified_username");
    private static final Object REJECTED_PROXY_ERR = literal("Unapproved proxy host.");

    /**
     * Abstract implementation of the hello packet handler
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param ci The callback info
     */
    public static void handleHello(
            final @NonNull ServerLoginPacketListenerBridge slpl, final @NonNull CallbackInfo ci) {
        try {
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
