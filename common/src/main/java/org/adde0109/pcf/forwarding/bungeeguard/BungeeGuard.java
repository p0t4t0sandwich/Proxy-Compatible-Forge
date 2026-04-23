package org.adde0109.pcf.forwarding.bungeeguard;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.event.Cancellable;
import dev.neuralnexus.taterapi.mc.server.players.NameAndId;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;

import io.netty.util.AttributeKey;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

/** Adapted from <a href="https://github.com/lucko/BungeeGuard">BungeeGuard</a> */
public final class BungeeGuard {
    public static final String BUNGEE_GUARD_TOKEN_PROPERTY_NAME = "bungeeguard-token";
    public static final AttributeKey<Collection<String>> BUNGEE_GUARD_TOKEN =
            AttributeKey.valueOf("pcf-bungeeguard-token");

    private static final Object NO_DATA =
            literal("&cUnable to authenticate - no data was forwarded by the proxy.");
    private static final Object INVALID_TOKEN = literal("&cUnable to authenticate.");

    /**
     * Validates the BungeeGuard token for the given connection
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param profile The player's GameProfile
     * @param c Cancellable
     */
    public static void validateToken(
            final @NonNull ServerLoginPacketListenerBridge slpl,
            final @NonNull GameProfile profile,
            final @NonNull Cancellable c) {
        final NameAndId nameAndId = new NameAndId(profile);
        final String connectionDescription =
                nameAndId.id() + " @ " + slpl.bridge$connection().bridge$address().getHostString();
        final Collection<String> bungeeGuardTokens =
                slpl.bridge$connection().bridge$channel().attr(BUNGEE_GUARD_TOKEN).getAndSet(null);
        if (bungeeGuardTokens.size() > 1) {
            PCF.logger.warn(
                    "Denying connection from " + connectionDescription + " - more than one token");
            c.cancel();
            throw new ThrowingComponent(INVALID_TOKEN);
        }
        final String bungeeGuardToken = bungeeGuardTokens.stream().findFirst().orElse(null);

        // TODO: Consider supporting multiple in PCF's config
        if (!PCF.instance().forwarding().secret().equals(bungeeGuardToken)) {
            final String reason = bungeeGuardToken == null ? "No Token" : "Invalid token";
            PCF.logger.warn(
                    "Denying connection from " + connectionDescription + " - reason: " + reason);
            c.cancel();
            throw new ThrowingComponent(bungeeGuardToken == null ? NO_DATA : INVALID_TOKEN);
        }

        PCF.logger.debug("Successfully validated BungeeGuard token for " + connectionDescription);
    }
}
