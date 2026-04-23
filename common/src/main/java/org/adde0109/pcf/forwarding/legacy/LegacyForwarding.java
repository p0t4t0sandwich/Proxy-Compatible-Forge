package org.adde0109.pcf.forwarding.legacy;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;
import static dev.neuralnexus.taterapi.network.chat.Component.translatable;

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

import dev.neuralnexus.taterapi.event.Cancellable;
import dev.neuralnexus.taterapi.mc.server.players.NameAndId;
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
import org.adde0109.pcf.forwarding.Mode;
import org.adde0109.pcf.forwarding.PreLoginHandler;
import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class LegacyForwarding {
    private static final Object REJECTED_PROXY_ERR = literal("Unapproved proxy host.");

    public static final AttributeKey<Object> DEFERRED_DISCONNECT =
            AttributeKey.valueOf("pcf-deferred-disconnect");
    public static final AttributeKey<InetAddress> FORWARDED_ADDRESS =
            AttributeKey.valueOf("pcf-forwarded-address");
    public static final AttributeKey<String> PLAYER_NAME = AttributeKey.valueOf("pcf-player-name");
    public static final AttributeKey<UUID> SPOOFED_UUID = AttributeKey.valueOf("pcf-spoofed-uuid");
    public static final AttributeKey<Collection<Property>> SPOOFED_PROFILE =
            AttributeKey.valueOf("pcf-spoofed-profile");

    private static final char LEGACY_SEPARATOR = '\0';

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
        final int hostPort = data.readUnsignedShort();
        final ClientIntent intention = ClientIntent.byId(data.readVarInt());
        if (intention != ClientIntent.LOGIN) {
            return;
        }
        final Channel channel = connection.bridge$channel();

        final String[] split = hostName.split("\00");
        if (split.length < 3 || !(HOST_PATTERN.matcher(split[1]).matches())) {
            channel.attr(DEFERRED_DISCONNECT).set(DIRECT_CONNECT_ERR);
            return;
        }
        if (PCF.instance().forwarding().mode() == Mode.BUNGEEGUARD
                && (split.length < 4 || !split[3].contains(BUNGEE_GUARD_TOKEN_PROPERTY_NAME))) {
            channel.attr(DEFERRED_DISCONNECT).set(BG_CONFIG_ERR);
        }

        final String originalHost = split[0];
        final String forwardedAddress = split[1];
        final UUID uuid = fromStringLenient(split[2]);

        // Save the forwarded address
        channel.attr(FORWARDED_ADDRESS).set(InetAddresses.forString(forwardedAddress));

        // Save the UUID
        channel.attr(SPOOFED_UUID).set(uuid);

        // Save the properties if present
        final Optional<Property> fmlMarker;
        if (split.length >= 4) {
            final String profileJSON = split[3];
            final List<Property> properties = GSON.fromJson(profileJSON, profileTypeToken);

            // Pop out the FML marker if present
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
        // spotless:on

        // Write the original address back into packet
        final ClientIntentionPacket newPacket =
                new ClientIntentionPacket(protocolVersion, host, hostPort, intention);
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

    private static final Object FAILED_TO_VERIFY =
            translatable("multiplayer.disconnect.unverified_username");

    public static void handleHello(
            final @NonNull ServerLoginPacketListenerBridge slpl, final @NonNull CallbackInfo ci) {
        final ConnectionBridge connection = slpl.bridge$connection();
        final Channel channel = connection.bridge$channel();

        // Handle any deferred disconnects from the handshake phase
        final Object deferredDisconnect = channel.attr(DEFERRED_DISCONNECT).getAndSet(null);
        if (deferredDisconnect != null) {
            slpl.bridge$disconnect(deferredDisconnect);
            return;
        }

        // Check if the connection is from an approved proxy
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
                throw new ThrowingComponent(REJECTED_PROXY_ERR);
            }
        }

        // Update the address to the forwarded address
        final InetAddress address = connection.bridge$channel().attr(FORWARDED_ADDRESS).get();
        final int port = connection.bridge$address().getPort();
        final InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        connection.bridge$address(socketAddress);

        // Create the profile
        final GameProfile profile = createProfile(slpl.bridge$connection().bridge$channel());

        // Proceed with login
        try {
            final Cancellable cancellable = Cancellable.simple();
            for (final PreLoginHandler processor : PreLoginHandler.HANDLERS) {
                processor.process(slpl, profile, cancellable);
                if (cancellable.cancelled()) {
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
            ci.cancel();
        }
    }

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    public static @NonNull GameProfile createProfile(final @NonNull Channel channel) {
        final String name = channel.attr(PLAYER_NAME).getAndSet(null);
        final UUID uuid = channel.attr(SPOOFED_UUID).getAndSet(null);
        if (name == null || uuid == null) {
            throw new ThrowingComponent(PLAYER_INFO_ERR);
        }

        final Collection<Property> properties = channel.attr(SPOOFED_PROFILE).getAndSet(null);

        // Check for BungeeGuard tokens
        if (PCF.instance().forwarding().mode() == Mode.BUNGEEGUARD
                && properties != null
                && !properties.isEmpty()) {
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
        if (properties == null || properties.isEmpty()) {
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
                if (!PROP_PATTERN.matcher(getName(property)).matches()) continue;
                propertiesMap.put(getName(property), property);
            }
            return profile;
        }
    }
}
