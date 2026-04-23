package org.adde0109.pcf.mixin.v7_10.forge.forwarding;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.util.IChatComponent;

import org.adde0109.pcf.forwarding.ConnectionBridge;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;

@AConstraint(
        mappings = Mappings.SEARGE,
        version = @Versions(min = MinecraftVersion.V7, max = MinecraftVersion.V7_10))
@Mixin(NetworkManager.class)
public abstract class ConnectionMixin implements ConnectionBridge {
    @Override
    public @NonNull Object bridge$disconnectPacket(final @NonNull Object reason) {
        return new S00PacketDisconnect((IChatComponent) reason);
    }
}
