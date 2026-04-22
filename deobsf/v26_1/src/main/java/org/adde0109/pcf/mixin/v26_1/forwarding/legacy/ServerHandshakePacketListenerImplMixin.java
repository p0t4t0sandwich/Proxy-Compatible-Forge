package org.adde0109.pcf.mixin.v26_1.forwarding.legacy;

import static dev.neuralnexus.taterapi.network.chat.Component.literal;

import com.google.common.net.InetAddresses;
import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.Mode;
import org.adde0109.pcf.forwarding.legacy.ConnectionBridgeLegacy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

// https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0024-BungeeCord-Support.patch
@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin {
    // spotless:off
    @Shadow @Final private Connection connection;

    @Unique private static final char pcf$LEGACY_SEPARATOR = '\0';
    @Unique private static final String pcf$BUNGEE_GUARD_TOKEN_PROPERTY_NAME = "bungeeguard-token";

    @Unique private static final Gson pcf$gson = new Gson();
    @Unique private static final Pattern pcf$HOST_PATTERN = Pattern.compile("[0-9a-f.:]{0,45}");

    @Unique private static final Object pcf$DIRECT_CONNECT_ERR = // TODO: Consider two different messages
            literal("This server requires you to connect with Velocity or BungeeCord.");
    @Unique private static final Object pcf$BG_CONFIG_ERR =
            literal("This server requires the proxy to be configured for BungeeGuard forwarding.");

    @Inject(method = "beginLogin", cancellable = true, at = @At(value = "RETURN",
            target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    // spotless:on
    private void onBeginLogin(
            final ClientIntentionPacket packet, final boolean transfer, final CallbackInfo ci) {
        if (PCF.instance().forwarding().mode() != Mode.LEGACY
                && PCF.instance().forwarding().mode() != Mode.BUNGEEGUARD) return;

        // Spigot Start
        // TODO: Stick this in a packet listener and extract it there and add to the
        //  ConnectionBridgeLegacy
        //  Then replace the original serverAddress used for the regular login flow
        //  Do not forget Forge markers if present
        final String[] split = packet.hostName().split("\00");
        PCF.logger.info(
                "Received handshake with "
                        + split.length
                        + " parts: "
                        + String.join("\n    ", split));
        if ((split.length < 3) || !(pcf$HOST_PATTERN.matcher(split[1]).matches())) {
            this.pcf$disconnect(pcf$DIRECT_CONNECT_ERR);
            return;
        }
        if ((split.length < 4) && PCF.instance().forwarding().mode() != Mode.BUNGEEGUARD) {
            this.pcf$disconnect(pcf$BG_CONFIG_ERR);
            return;
        }

        final String originalAddress = split[1];
        final UUID uuid = pcf$fromStringLenient(split[2]);
        PCF.logger.info(
                "Player " + uuid + " is connecting with original address " + originalAddress);

        // Update the proxied address
        final int port = ((ConnectionBridge) this.connection).bridge$address().getPort();
        final InetSocketAddress address =
                new InetSocketAddress(InetAddresses.forString(originalAddress), port);
        ((ConnectionBridge) this.connection).bridge$address(address);

        // Save the UUID
        ((ConnectionBridgeLegacy) this.connection).bridge$spoofedUUID(uuid);

        // Save the properties if present
        if (split.length == 4) { // TODO: Enforce if BungeeGuard forwarding
            final String profileJSON = split[3];
            ((ConnectionBridgeLegacy) this.connection)
                    .bridge$spoofedProfile(pcf$gson.fromJson(profileJSON, Property[].class));
        }
        // Spigot End
    }

    @Unique public void pcf$disconnect(Object component) {
        this.connection.send(new ClientboundLoginDisconnectPacket((Component) component));
        this.connection.disconnect((Component) component);
    }

    @Unique private static UUID pcf$fromStringLenient(final String string) {
        return UUID.fromString(
                string.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    // https://github.com/caunt/BungeeForge/blob/1.20.2/src/main/java/ua/caunt/bungeeforge/mixin/network/protocol/handshake/ClientIntentionPacket.java#L51-L54
    @Unique private static boolean pcf$isFmlMarker(Property property) {
        return Objects.equals(property.name(), "extraData")
                && property.value().startsWith("\u0001FORGE");
    }
}
