package org.adde0109.pcf.mixin.v1_20_2.forge.login;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.conditionalmixins.annotations.ReqMCVersion;
import dev.neuralnexus.conditionalmixins.annotations.ReqMappings;
import dev.neuralnexus.taterapi.Mappings;
import dev.neuralnexus.taterapi.MinecraftVersion;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.DiscardedQueryPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.adde0109.pcf.common.CommonInitializer;
import org.adde0109.pcf.common.StateUtil;
import org.adde0109.pcf.common.abstractions.Connection;
import org.adde0109.pcf.common.abstractions.Payload;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ReqMappings(Mappings.SEARGE)
@ReqMCVersion(MinecraftVersion.V1_20_2)
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ModernForwardingMixin {
    @Shadow @Final net.minecraft.network.Connection connection;

    @Shadow @Nullable private GameProfile authenticatedProfile;

    @Shadow public abstract void shadow$disconnect(Component reason);

    @Unique private boolean pcf$listen = false;

    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void onHandleHello(CallbackInfo ci) {
        Validate.validState(StateUtil.stateEquals(this, 0), "Unexpected hello packet");
        if (CommonInitializer.modernForwarding != null) {
            StateUtil.setState(this, 0);
            LogManager.getLogger().debug("Sent Forward Request");
            this.connection.send(new ClientboundCustomQueryPacket(100,
                    new DiscardedQueryPayload((ResourceLocation) CommonInitializer.channelResource())));
            this.pcf$listen = true;
            ci.cancel();
        }
    }

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    private void onHandleCustomQueryPacket(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        if ((packet.transactionId() == CommonInitializer.QUERY_ID) && StateUtil.stateEquals(this, 0) && this.pcf$listen) {
            this.pcf$listen = false;
            try {
                if(packet.payload() == null) {
                    throw new Exception("Got empty packet");
                }
                FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
                packet.payload().write(data);

                this.authenticatedProfile = CommonInitializer.modernForwarding.handleForwardingPacket((Payload) data, (Connection) connection);
                this.arclight$preLogin();
                StateUtil.setState(this, 4);
            } catch (Exception e) {
                this.shadow$disconnect(Component.nullToEmpty("Direct connections to this server are not permitted!"));
                LogManager.getLogger().warn("Exception verifying forwarded player info", e);
            }
            ci.cancel();
        }
    }

    @Shadow(remap = false)
    @SuppressWarnings({"MixinAnnotationTarget", "RedundantThrows"})
    void arclight$preLogin() throws Exception {}
}
