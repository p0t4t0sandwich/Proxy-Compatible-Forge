package org.adde0109.pcf.forwarding.legacy;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;

import static org.adde0109.pcf.forwarding.Forwarding.HOST_PATTERN;
import static org.adde0109.pcf.forwarding.Forwarding.PLAYER_INFO_ERR;
import static org.adde0109.pcf.forwarding.Forwarding.rewriteClientIntention;
import static org.adde0109.pcf.forwarding.ReflectionUtils.attributeKeyValueOf;
import static org.adde0109.pcf.forwarding.ReflectionUtils.getName;
import static org.adde0109.pcf.forwarding.ReflectionUtils.getProperties;
import static org.adde0109.pcf.forwarding.ReflectionUtils.getValue;
import static org.adde0109.pcf.forwarding.bungeeguard.BungeeGuard.BUNGEE_GUARD_TOKEN;
import static org.adde0109.pcf.forwarding.bungeeguard.BungeeGuard.BUNGEE_GUARD_TOKEN_PROPERTY_NAME;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.net.InetAddresses;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import dev.neuralnexus.taterapi.meta.Constraint;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;
import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;
import dev.neuralnexus.taterapi.network.protocol.handshake.ClientIntent;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.Forwarding;
import org.adde0109.pcf.forwarding.Mode;
import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Adapted from <a
 * href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0024-BungeeCord-Support.patch">Spigot</a>
 * and <a
 * href="https://github.com/caunt/BungeeForge/blob/1.20.2/src/main/java/ua/caunt/bungeeforge/mixin/network/protocol/handshake/ClientIntentionPacket.java">BungeeForge</a>.
 * Additional information sourced from <a
 * href="https://github.com/PaperMC/Velocity/blob/dev/3.0.0/proxy/src/main/java/com/velocitypowered/proxy/connection/forge/legacy/LegacyForgeConnectionType.java">Velocity</a>
 * and <a
 * href="https://github.com/PaperMC/Waterfall/blob/master/BungeeCord-Patches/0011-Add-support-for-FML-with-IP-Forwarding-enabled.patch">Waterfall</a>
 */
public final class LegacyForwarding {
    public static final AttributeKey<InetAddress> FORWARDED_ADDRESS =
            attributeKeyValueOf("pcf-forwarded-address");
    public static final AttributeKey<String> PLAYER_NAME = attributeKeyValueOf("pcf-player-name");
    public static final AttributeKey<UUID> SPOOFED_UUID = attributeKeyValueOf("pcf-spoofed-uuid");
    public static final AttributeKey<Collection<Property>> SPOOFED_PROFILE =
            attributeKeyValueOf("pcf-spoofed-profile");

    private static final Object LEGACY_DIRECT_CONNECT_ERR =
            literal("This server requires you to connect with Velocity or BungeeCord.");
    private static final Object BG_CONFIG_ERR =
            literal("This server requires the proxy to be configured for BungeeGuard forwarding.");

    private static final Gson GSON = new GsonBuilder().create();
    // Use Type b/c GSON shipped with MC 1.19.2 doesn't have Gson#fromJson(String, TypeToken<T>)
    private static final Type profileTypeToken = new TypeToken<List<Property>>() {}.getType();

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    private static final String LEGACY_FORGE_MARKER = "\0FML\0";
    private static final String EXTRA_DATA_PROPERTY = "extraData";
    private static final String LEGACY_FORGE_CLIENT_PROPERTY = "forgeClient";
    private static final String MODERN_FORGE_CLIENT_PROPERTY = "modernForgeClient";
    private static final String FORGE_CLIENT_TRUE = "true";

    /**
     * Handle the client intention packet and extract player info
     *
     * @param connection The connection
     * @param data The packet buffer
     */
    public static void handleClientIntention(
            final @NonNull ConnectionBridge connection, final @NonNull FriendlyByteBuf data) {
        // Read the original packet
        final int protocolVersion = data.readVarInt();
        final String hostName = data.readUtf(Short.MAX_VALUE);
        final int hostPort = data.readUnsignedShort();
        final ClientIntent intention = ClientIntent.byId(data.readVarInt());
        if (intention != ClientIntent.LOGIN) {
            return;
        }
        final Channel channel = connection.bridge$channel();

        // Parse the host name for forwarded data
        final String[] split = hostName.split("\0");
        if (split.length < 3 || !(HOST_PATTERN.matcher(split[1]).matches())) {
            throw new ThrowingComponent(LEGACY_DIRECT_CONNECT_ERR);
        }
        if (PCF.instance().forwarding().mode() == Mode.BUNGEEGUARD
                && (split.length < 4 || !split[3].contains(BUNGEE_GUARD_TOKEN_PROPERTY_NAME))) {
            // Rewrite the packet before throwing
            rewriteClientIntention(channel, protocolVersion, split[0], hostPort, intention, data);
            throw new ThrowingComponent(BG_CONFIG_ERR);
        }

        final String originalHost = split[0];
        final String forwardedAddress = split[1];
        final UUID uuid = fromStringLenient(split[2]);

        // Save forwarded data
        channel.attr(FORWARDED_ADDRESS).set(InetAddresses.forString(forwardedAddress));
        channel.attr(SPOOFED_UUID).set(uuid);

        // spotless:off
        final boolean legacyForgeClient;
        final boolean modernForgeClient;
        final Optional<Property> extraData;
        if (split.length >= 4) {
            final String profileJSON = split[3];
            final List<Property> properties = GSON.fromJson(profileJSON, profileTypeToken);

            // Pop out the Forge properties
            legacyForgeClient = properties.stream().anyMatch(p ->
                    getName(p).equals(LEGACY_FORGE_CLIENT_PROPERTY) && getValue(p).equals(FORGE_CLIENT_TRUE));
            modernForgeClient = properties.stream().anyMatch(p ->
                    getName(p).equals(MODERN_FORGE_CLIENT_PROPERTY) && getValue(p).equals(FORGE_CLIENT_TRUE));
            extraData = properties.stream()
                    .filter(p -> getName(p).equals(EXTRA_DATA_PROPERTY)).findFirst();
            properties.removeIf(p -> getName(p).equals(LEGACY_FORGE_CLIENT_PROPERTY)
                            || getName(p).equals(MODERN_FORGE_CLIENT_PROPERTY)
                            || getName(p).equals(EXTRA_DATA_PROPERTY));
            channel.attr(SPOOFED_PROFILE).set(properties);
        } else {
            legacyForgeClient = false;
            modernForgeClient = false;
            extraData = Optional.empty();
        }

        final String host;
        if (extraData.isPresent()) {
            final String value = getValue(extraData.get());
            if (PCF.instance().debug().enabled()) {
                if (legacyForgeClient) {
                    PCF.logger.debug("Received extraData with forgeClient=true from "
                            + channel.remoteAddress() + " - value: " + value);
                } else if (modernForgeClient) {
                    PCF.logger.debug("Received extraData with modernForgeClient=true from "
                            + channel.remoteAddress() + " - value: " + value);
                } else { // Some implementations do this
                    PCF.logger.debug("Received extraData without (modernF|f)orgeClient=true from "
                            + channel.remoteAddress() + " - value: " + value);
                }
            }
            if (value.startsWith("\1")) { // Restore extra hostname data
                host = originalHost + value.replace("\1", "\0");
            } else { // Avoid propagating bad data
                PCF.logger.warn("Received misformatted extraData from "
                        + channel.remoteAddress() + " - value: " + value);
                host = originalHost;
            }
        } else if (legacyForgeClient) { // Assume Forge 1.8 - 1.12.2
            if (PCF.instance().debug().enabled()) {
                PCF.logger.debug("Identified legacy Forge client from " + channel.remoteAddress()
                        + " - appending legacy Forge marker to hostname");
            }
            host = originalHost + LEGACY_FORGE_MARKER;
        } else {
            host = originalHost;
        }
        PCF.logger.debug("Parsed forwarded data - Host: " + host + ", UUID: " + uuid);
        // spotless:on

        // Write the original address (and Forge marker) back into packet
        rewriteClientIntention(channel, protocolVersion, host, hostPort, intention, data);
    }

    /**
     * Hello packet handler for legacy forwarding
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param ci The callback info
     */
    public static void handleHello(
            final @NonNull ServerLoginPacketListenerBridge slpl, final @NonNull CallbackInfo ci) {
        final ConnectionBridge connection = slpl.bridge$connection();
        final Channel channel = connection.bridge$channel();

        // Check if the connection is from an approved proxy
        Forwarding.checkProxy(connection);

        // Apply IP forwarding
        final InetAddress address = channel.attr(FORWARDED_ADDRESS).get();
        Forwarding.ipForwarding(connection, address);

        // Query player info from channel
        final String name = channel.attr(PLAYER_NAME).getAndSet(null);
        final UUID uuid = channel.attr(SPOOFED_UUID).getAndSet(null);
        if (name == null || uuid == null) {
            throw new ThrowingComponent(PLAYER_INFO_ERR);
        }
        final Collection<Property> properties = channel.attr(SPOOFED_PROFILE).getAndSet(null);

        // Check for BungeeGuard tokens
        if (PCF.instance().forwarding().mode() == Mode.BUNGEEGUARD && properties != null) {
            final Collection<String> bungeeGuardTokens = new HashSet<>();
            for (final Property property : properties) {
                if (getName(property).equals(BUNGEE_GUARD_TOKEN_PROPERTY_NAME)) {
                    bungeeGuardTokens.add(getValue(property));
                }
            }
            channel.attr(BUNGEE_GUARD_TOKEN).set(bungeeGuardTokens);

            // Remove BungeeGuard token(s) from properties.
            // They're filtered out by PROP_PATTERN, but might as well remove them now.
            properties.removeIf(
                    property -> getName(property).equals(BUNGEE_GUARD_TOKEN_PROPERTY_NAME));
        }

        // Create the profile
        final GameProfile profile = createProfile(name, uuid, properties);

        // Proceed with login
        ci.cancel();
        Forwarding.preLogin(slpl, profile);
    }

    /**
     * Parse a UUID from a string, leniently accepting both dashed and non-dashed formats
     *
     * @param string The string to parse
     * @return The parsed UUID
     */
    private static @NonNull UUID fromStringLenient(final @NonNull String string) {
        return UUID.fromString(
                string.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    /**
     * Creates a new GameProfile
     *
     * @param name The player's name
     * @param uuid The player's UUID
     * @param properties The player's properties, if any
     * @return The created GameProfile
     */
    public static @NonNull GameProfile createProfile(
            final @NonNull String name,
            final @NonNull UUID uuid,
            final @Nullable Collection<Property> properties) {
        // Exit early if there are no properties
        if (properties == null || properties.isEmpty()) {
            return new GameProfile(uuid, name);
        }

        // Filter out invalid properties
        properties.removeIf(property -> !PROP_PATTERN.matcher(getName(property)).matches());

        // Create the profile
        if (Constraint.noLessThan(MinecraftVersions.V21_9)
                .result()) { // com.mojang:authlib:7.0.0 or newer
            final ImmutableMultimap.Builder<String, Property> propertiesBuilder =
                    ImmutableMultimap.builder();
            for (final Property property : properties) {
                propertiesBuilder.put(property.name(), property);
            }
            return new GameProfile(uuid, name, new PropertyMap(propertiesBuilder.build()));
        } else {
            final GameProfile profile = new GameProfile(uuid, name);
            final PropertyMap propertiesMap = getProperties(profile);
            for (final Property property : properties) {
                propertiesMap.put(getName(property), property);
            }
            return profile;
        }
    }
}
