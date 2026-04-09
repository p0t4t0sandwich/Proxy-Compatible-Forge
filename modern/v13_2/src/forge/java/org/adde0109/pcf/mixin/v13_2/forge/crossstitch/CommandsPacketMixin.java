package org.adde0109.pcf.mixin.v13_2.forge.crossstitch;

import com.mojang.brigadier.arguments.ArgumentType;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.play.server.SPacketCommandList;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.crossstitch.CrossStitch;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: Create some way of replacing a mixin's target, then merge with v16_5
/**
 * Adapted from <a
 * href="https://github.com/VelocityPowered/CrossStitch/blob/fe3f3be49c52dc9c1b6b7cd3cafefb953adf4486/src/main/java/com/velocitypowered/crossstitch/mixin/command/CommandTreeSerializationMixin.java">CrossStitch</a>
 */
@AConstraint(
        mappings = Mappings.LEGACY_SEARGE,
        version = @Versions(min = MinecraftVersion.V13, max = MinecraftVersion.V13_2))
@Mixin(SPacketCommandList.class)
public abstract class CommandsPacketMixin {
    // spotless:off
    @Redirect(method = "func_197696_a",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/arguments/ArgumentTypes;serialize(Lnet/minecraft/network/PacketBuffer;Lcom/mojang/brigadier/arguments/ArgumentType;)V"))
    // spotless:on
    private void writeNode$wrapInVelocityModArgument(
            final @NonNull @Coerce ByteBuf buf, final @NonNull ArgumentType<?> argumentType) {
        try {
            CrossStitch.writeNode$wrapInVelocityModArgument(buf, argumentType);
        } catch (Exception e) {
            PCF.logger.error(
                    "Failed to serialize command argument type: "
                            + argumentType.getClass().getName(),
                    e);
        }
    }
}
