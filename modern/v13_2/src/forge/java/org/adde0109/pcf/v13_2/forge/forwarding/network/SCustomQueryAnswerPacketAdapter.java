package org.adde0109.pcf.v13_2.forge.forwarding.network;

import dev.neuralnexus.taterapi.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import dev.neuralnexus.taterapi.network.protocol.login.custom.CustomQueryAnswerPayload;
import dev.neuralnexus.taterapi.serialization.Result;
import dev.neuralnexus.taterapi.serialization.codecs.ReversibleCodec;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.client.CPacketCustomPayloadLogin;

import org.adde0109.pcf.mixin.v13_2.forge.forwarding.modern.ServerboundCustomQueryPacketAccessor;

public final class SCustomQueryAnswerPacketAdapter
        implements ReversibleCodec<CPacketCustomPayloadLogin, ServerboundCustomQueryAnswerPacket> {
    public static final SCustomQueryAnswerPacketAdapter INSTANCE =
            new SCustomQueryAnswerPacketAdapter();

    @SuppressWarnings("DataFlowIssue")
    @Override
    public Result<ServerboundCustomQueryAnswerPacket> encode(
            final CPacketCustomPayloadLogin object) {
        final int transactionId =
                ((ServerboundCustomQueryPacketAccessor) object).pcf$getTransactionId();
        if (((ServerboundCustomQueryPacketAccessor) object).pcf$getData() == null) {
            return Result.success(new ServerboundCustomQueryAnswerPacket(transactionId));
        }
        return Result.success(
                new ServerboundCustomQueryAnswerPacket(
                        transactionId,
                        CustomQueryAnswerPayload.codec(transactionId)
                                .decode(
                                        ((ServerboundCustomQueryPacketAccessor) object)
                                                .pcf$getData()
                                                .slice())));
    }

    @Override
    public Result<CPacketCustomPayloadLogin> decode(
            final ServerboundCustomQueryAnswerPacket object) {
        if (object.payload() == null) {
            return Result.success(new CPacketCustomPayloadLogin(object.transactionId(), null));
        }
        return Result.success(
                new CPacketCustomPayloadLogin(
                        object.transactionId(), new PacketBuffer(object.payload().data().slice())));
    }
}
