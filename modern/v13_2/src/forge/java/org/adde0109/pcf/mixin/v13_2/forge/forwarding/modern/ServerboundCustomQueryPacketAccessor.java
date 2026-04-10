package org.adde0109.pcf.mixin.v13_2.forge.forwarding.modern;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.client.CPacketCustomPayloadLogin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// TODO: Create some way of replacing a mixin's target, then merge with v16_5
@AConstraint(
        mappings = Mappings.LEGACY_SEARGE,
        version = @Versions(min = MinecraftVersion.V13, max = MinecraftVersion.V13_2))
@Mixin(CPacketCustomPayloadLogin.class)
public interface ServerboundCustomQueryPacketAccessor {
    @Accessor("transaction")
    int pcf$getTransactionId();

    @Accessor("payload")
    @Nullable PacketBuffer pcf$getData();
}
