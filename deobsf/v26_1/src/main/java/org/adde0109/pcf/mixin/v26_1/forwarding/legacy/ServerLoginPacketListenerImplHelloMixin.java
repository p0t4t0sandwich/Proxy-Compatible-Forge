package org.adde0109.pcf.mixin.v26_1.forwarding.legacy;

import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.SPOOFED_PROFILE;
import static org.adde0109.pcf.forwarding.legacy.LegacyForwarding.SPOOFED_UUID;
import static org.adde0109.pcf.forwarding.modern.ReflectionUtils.getProperties;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.adde0109.pcf.forwarding.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

// https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0024-BungeeCord-Support.patch
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplHelloMixin
        implements ServerLoginPacketListenerBridge {
    // spotless:off
    @Shadow @Final private Connection connection;

    @Shadow protected abstract void startClientVerification(GameProfile profile);

    @Unique private static final Pattern pcf$PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @AConstraint(mappings = Mappings.MOJANG, version = @Versions(min = MinecraftVersion.V20_2))
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V"))
    // spotless:on
    private void onHandleHello_20_M(
            final ServerboundHelloPacket packet, final @NonNull CallbackInfo ci) {
        final ConnectionBridge conn = (ConnectionBridge) this.connection;

        if (conn.bridge$channel().attr(SPOOFED_UUID).get() == null) {
            return; // TODO: Provide client-bound disconnect error
        }

        // TODO: PostProcessors
        this.startClientVerification(this.pcf$createOfflineProfile(packet.name()));
        ci.cancel();
    }

    // Spigot start
    @Unique protected GameProfile pcf$createOfflineProfile(String name) {
        final ConnectionBridge conn = (ConnectionBridge) this.connection;

        final UUID uuid;
        if (conn.bridge$channel().attr(SPOOFED_UUID).get() != null) {
            uuid = conn.bridge$channel().attr(SPOOFED_UUID).get();
        } else {
            uuid =
                    UUID.nameUUIDFromBytes(
                            ("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }

        // TODO: Add 1.21.9 support
        final GameProfile profile = new GameProfile(uuid, name);
        final PropertyMap propertiesMap = getProperties(profile);
        if (conn.bridge$channel().attr(SPOOFED_PROFILE).get() != null) {
            for (final Property property : conn.bridge$channel().attr(SPOOFED_PROFILE).get()) {
                if (!pcf$PROP_PATTERN.matcher(property.name()).matches()) continue;
                propertiesMap.put(property.name(), property);
            }
        }
        return profile;
    }
    // Spigot end
}
