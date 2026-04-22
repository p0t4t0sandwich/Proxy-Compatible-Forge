package org.adde0109.pcf.forwarding.legacy;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;

import static org.adde0109.pcf.forwarding.ReflectionUtils.getProperties;

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

import io.netty.util.AttributeKey;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.Mode;
import org.jspecify.annotations.NonNull;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class LegacyForwarding {
    public static final AttributeKey<UUID> SPOOFED_UUID = AttributeKey.valueOf("pcf-spoofed-uuid");
    public static final AttributeKey<Collection<Property>> SPOOFED_PROFILE =
            AttributeKey.valueOf("pcf-spoofed-profile");

    private static final char LEGACY_SEPARATOR = '\0';
    private static final String BUNGEE_GUARD_TOKEN_PROPERTY_NAME = "bungeeguard-token";

    private static final Gson GSON = new GsonBuilder().create();
    private static final TypeToken<List<Property>> profileTypeToken = new TypeToken<>() {};

    private static final Pattern HOST_PATTERN = Pattern.compile("[0-9a-f.:]{0,45}");

    private static final Object DIRECT_CONNECT_ERR = // TODO: Consider two different messages
            literal("This server requires you to connect with Velocity or BungeeCord.");
    private static final Object BG_CONFIG_ERR =
            literal("This server requires the proxy to be configured for BungeeGuard forwarding.");
    private static final Object PLAYER_INFO_ERR = literal("Unable to verify player details.");

    // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0024-BungeeCord-Support.patch
    // https://github.com/caunt/BungeeForge/blob/1.20.2/src/main/java/ua/caunt/bungeeforge/mixin/network/protocol/handshake/ClientIntentionPacket.java#L51-L54
    public static void handleClientIntentionPacket(
            final @NonNull ConnectionBridge connection, final @NonNull FriendlyByteBuf data) {
        final int protocolVersion = data.readVarInt();
        final String hostName = data.readUtf(Short.MAX_VALUE);
        final int _ = data.readUnsignedShort();
        final ClientIntent intention = ClientIntent.byId(data.readVarInt());
        if (intention != ClientIntent.LOGIN) {
            return;
        }

        final String[] split = hostName.split("\00");
        if ((split.length < 3) || !(HOST_PATTERN.matcher(split[1]).matches())) {
            throw new ThrowingComponent(DIRECT_CONNECT_ERR);
        }
        if (PCF.instance().forwarding().mode() != Mode.BUNGEEGUARD && (split.length < 4)) {
            throw new ThrowingComponent(BG_CONFIG_ERR);
        }

        final String originalHost = split[0];
        final String forwardedAddress = split[1];
        final UUID uuid = fromStringLenient(split[2]);
        PCF.logger.debug(
                "Player " + uuid + " is connecting with forwarded address " + forwardedAddress);

        // Update the proxied address
        final int port = connection.bridge$address().getPort();
        final InetSocketAddress address =
                new InetSocketAddress(InetAddresses.forString(forwardedAddress), port);
        connection.bridge$address(address);

        // Save the UUID
        connection.bridge$channel().attr(SPOOFED_UUID).set(uuid);

        // Save the properties if present
        final Optional<Property> fmlMarker;
        if (split.length == 4) {
            final String profileJSON = split[3];
            final List<Property> properties = GSON.fromJson(profileJSON, profileTypeToken);

            // Pop out the FML marker if present
            fmlMarker = properties.stream().filter(LegacyForwarding::isFmlMarker).findFirst();
            properties.removeIf(LegacyForwarding::isFmlMarker);
            connection.bridge$channel().attr(SPOOFED_PROFILE).set(properties);
        } else {
            fmlMarker = Optional.empty();
        }

        // spotless:off
        final String host = fmlMarker.map(property -> originalHost
                + LEGACY_SEPARATOR
                + property.value().split("\u0001")[1]
                + LEGACY_SEPARATOR).orElse(originalHost);
        // spotless:on

        // Write the original address back into packet
        final ClientIntentionPacket newPacket =
                new ClientIntentionPacket(protocolVersion, host, port, intention);
        data.clear();
        data.writeVarInt(0x00);
        ClientIntentionPacket.STREAM_CODEC.encode(data, newPacket);
    }

    private static @NonNull UUID fromStringLenient(final @NonNull String string) {
        return UUID.fromString(
                string.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    // https://github.com/caunt/BungeeForge/blob/1.20.2/src/main/java/ua/caunt/bungeeforge/mixin/network/protocol/handshake/ClientIntentionPacket.java#L51-L54
    private static boolean isFmlMarker(final @NonNull Property property) {
        return Objects.equals(property.name(), "extraData")
                && property.value().startsWith("\u0001FORGE");
    }

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    public static GameProfile createProfile(
            final @NonNull ConnectionBridge conn, final @NonNull String name) {
        final UUID uuid;
        if (conn.bridge$channel().attr(SPOOFED_UUID).get() != null) {
            uuid = conn.bridge$channel().attr(SPOOFED_UUID).get();
        } else {
            throw new ThrowingComponent(PLAYER_INFO_ERR);
        }

        final Collection<Property> properties = conn.bridge$channel().attr(SPOOFED_PROFILE).get();
        if (properties == null) {
            return new GameProfile(uuid, name);
            // com.mojang:authlib:7.0.0 or newer
        } else if (Constraint.noLessThan(MinecraftVersions.V21_9).result()) {
            final ImmutableMultimap.Builder<String, Property> propertiesBuilder =
                    ImmutableMultimap.builder();
            for (final Property property : properties) {
                if (!PROP_PATTERN.matcher(property.name()).matches()) continue;
                propertiesBuilder.put(property.name(), property);
            }
            return new GameProfile(uuid, name, new PropertyMap(propertiesBuilder.build()));
        } else {
            final GameProfile profile = new GameProfile(uuid, name);
            final PropertyMap propertiesMap = getProperties(profile);
            for (final Property property : properties) {
                if (!PROP_PATTERN.matcher(property.name()).matches()) continue;
                propertiesMap.put(property.name(), property);
            }
            return profile;
        }
    }
}
