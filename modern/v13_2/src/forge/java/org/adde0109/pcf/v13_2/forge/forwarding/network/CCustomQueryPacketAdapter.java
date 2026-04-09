package org.adde0109.pcf.v13_2.forge.forwarding.network;

import static dev.neuralnexus.taterapi.resources.Identifier.identifier;

import dev.neuralnexus.taterapi.network.protocol.login.ClientboundCustomQueryPacket;
import dev.neuralnexus.taterapi.network.protocol.login.custom.CustomQueryPayload;
import dev.neuralnexus.taterapi.serialization.Result;
import dev.neuralnexus.taterapi.serialization.codecs.ReversibleCodec;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.server.SPacketCustomPayloadLogin;

import org.adde0109.pcf.mixin.v13_2.forge.forwarding.modern.ClientboundCustomQueryPacketAccessor;

public final class CCustomQueryPacketAdapter
        implements ReversibleCodec<SPacketCustomPayloadLogin, ClientboundCustomQueryPacket> {
    public static final CCustomQueryPacketAdapter INSTANCE = new CCustomQueryPacketAdapter();

    @Override
    public Result<ClientboundCustomQueryPacket> encode(SPacketCustomPayloadLogin object) {
        return Result.success(
                new ClientboundCustomQueryPacket(
                        ((ClientboundCustomQueryPacketAccessor) object).pcf$getTransactionId(),
                        CustomQueryPayload.codec(
                                        ((ClientboundCustomQueryPacketAccessor) object)
                                                .pcf$getIdentifier()
                                                .toString())
                                .decode(
                                        ((ClientboundCustomQueryPacketAccessor) object)
                                                .pcf$getData()
                                                .slice())));
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public Result<SPacketCustomPayloadLogin> decode(ClientboundCustomQueryPacket object) {
        SPacketCustomPayloadLogin mcObject = new SPacketCustomPayloadLogin();
        ((ClientboundCustomQueryPacketAccessor) mcObject)
                .pcf$setTransactionId(object.transactionId());
        ((ClientboundCustomQueryPacketAccessor) mcObject)
                .pcf$setIdentifier(identifier(object.payload().id()));
        ((ClientboundCustomQueryPacketAccessor) mcObject)
                .pcf$setData(new PacketBuffer(object.payload().data().slice()));
        return Result.success(mcObject);
    }
}
