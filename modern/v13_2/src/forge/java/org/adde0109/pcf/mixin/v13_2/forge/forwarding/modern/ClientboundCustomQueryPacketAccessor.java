package org.adde0109.pcf.mixin.v13_2.forge.forwarding.modern;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.server.SPacketCustomPayloadLogin;
import net.minecraft.util.ResourceLocation;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// TODO: Create some way of replacing a mixin's target, then merge with v16_5
@AConstraint(
        mappings = Mappings.LEGACY_SEARGE,
        version = @Versions(min = MinecraftVersion.V13, max = MinecraftVersion.V13_2))
@Mixin(SPacketCustomPayloadLogin.class)
public interface ClientboundCustomQueryPacketAccessor {
    @Accessor("transaction")
    int pcf$getTransactionId();

    @Accessor("transaction")
    void pcf$setTransactionId(final int transactionId);

    @Accessor("channel")
    @NonNull ResourceLocation pcf$getIdentifier();

    @Accessor("channel")
    void pcf$setIdentifier(final @NonNull ResourceLocation identifier);

    @Accessor("payload")
    @NonNull PacketBuffer pcf$getData();

    @Accessor("payload")
    void pcf$setData(final @NonNull PacketBuffer data);
}
