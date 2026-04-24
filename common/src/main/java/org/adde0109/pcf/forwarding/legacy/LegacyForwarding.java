package org.adde0109.pcf.forwarding.legacy;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;

import static org.adde0109.pcf.forwarding.Forwarding.PLAYER_INFO_ERR;
import static org.adde0109.pcf.forwarding.ReflectionUtils.attributeKeyValueOf;
import static org.adde0109.pcf.forwarding.ReflectionUtils.getName;
import static org.adde0109.pcf.forwarding.ReflectionUtils.getProperties;
import static org.adde0109.pcf.forwarding.ReflectionUtils.getValue;
import static org.adde0109.pcf.forwarding.bungeeguard.BungeeGuard.BUNGEE_GUARD_TOKEN;
import static org.adde0109.pcf.forwarding.bungeeguard.BungeeGuard.BUNGEE_GUARD_TOKEN_PROPERTY_NAME;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.net.InetAddresses;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import dev.neuralnexus.taterapi.meta.Constraint;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;
import dev.neuralnexus.taterapi.network.FriendlyByteBuf;
import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;
import dev.neuralnexus.taterapi.network.protocol.handshake.ClientIntent;
import dev.neuralnexus.taterapi.network.protocol.handshake.ClientIntentionPacket;

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

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Adapted from <a
 * href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0024-BungeeCord-Support.patch">Spigot</a>
 * and <a
 * href="https://github.com/caunt/BungeeForge/blob/1.20.2/src/main/java/ua/caunt/bungeeforge/mixin/network/protocol/handshake/ClientIntentionPacket.java">BungeeForge</a>
 */
public final class LegacyForwarding {
    public static final AttributeKey<Object> DEFERRED_DISCONNECT =
            attributeKeyValueOf("pcf-deferred-disconnect");
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
    private static final TypeToken<List<Property>> profileTypeToken = new TypeToken<>() {};

    private static final Pattern HOST_PATTERN = Pattern.compile("[0-9a-f.:]{0,45}");
    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");
    private static final char LEGACY_SEPARATOR = '\0';

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
        final String[] split = hostName.split("\00");
        if (split.length < 3 || !(HOST_PATTERN.matcher(split[1]).matches())) {
            channel.attr(DEFERRED_DISCONNECT).set(LEGACY_DIRECT_CONNECT_ERR);
            return;
        }
        if (PCF.instance().forwarding().mode() == Mode.BUNGEEGUARD
                && (split.length < 4 || !split[3].contains(BUNGEE_GUARD_TOKEN_PROPERTY_NAME))) {
            channel.attr(DEFERRED_DISCONNECT).set(BG_CONFIG_ERR);
        }

        final String originalHost = split[0];
        final String forwardedAddress = split[1];
        final UUID uuid = fromStringLenient(split[2]);

        // Save forwarded data
        channel.attr(FORWARDED_ADDRESS).set(InetAddresses.forString(forwardedAddress));
        channel.attr(SPOOFED_UUID).set(uuid);

        final Optional<Property> fmlMarker;
        if (split.length >= 4) {
            final String profileJSON = split[3];
            final List<Property> properties = GSON.fromJson(profileJSON, profileTypeToken);

            // Pop out the FML marker
            fmlMarker = properties.stream().filter(LegacyForwarding::isFmlMarker).findFirst();
            properties.removeIf(LegacyForwarding::isFmlMarker);
            channel.attr(SPOOFED_PROFILE).set(properties);
        } else {
            fmlMarker = Optional.empty();
        }

        // spotless:off
        final String host = fmlMarker.map(property -> originalHost
                + LEGACY_SEPARATOR
                + property.value().split("\u0001")[1]
                + LEGACY_SEPARATOR).orElse(originalHost);
        PCF.logger.debug("Parsed forwarded data - Host: " + host + ", UUID: " + uuid);
        // spotless:on

        // Write the original address (and Forge marker) back into packet
        final ClientIntentionPacket newPacket =
                new ClientIntentionPacket(protocolVersion, host, hostPort, intention);
        data.clear();
        data.writeVarInt(0x00);
        ClientIntentionPacket.STREAM_CODEC.encode(data, newPacket);
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

        // Handle any deferred disconnects from the handshake phase
        final Object deferredDisconnect = channel.attr(DEFERRED_DISCONNECT).getAndSet(null);
        if (deferredDisconnect != null) {
            throw new ThrowingComponent(deferredDisconnect);
        }

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
     * Check if a property is the FML marker used by Forge to indicate a modded client.
     *
     * @param property The property to check
     * @return True if the property is the FML marker, false otherwise
     */
    private static boolean isFmlMarker(final @NonNull Property property) {
        return Objects.equals(property.name(), "extraData")
                && property.value().startsWith("\u0001FORGE");
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
