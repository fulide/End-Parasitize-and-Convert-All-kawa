package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: add or remove the acid-water colour transition effect. */
public class AcidWaterColorPacket implements CustomPacketPayload {

    public static final Type<AcidWaterColorPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "acid_water_color"));

    public static final StreamCodec<ByteBuf, AcidWaterColorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.waterPos,
            BlockPos.STREAM_CODEC, packet -> packet.acidPos,
            ByteBufCodecs.INT, packet -> packet.distance,
            ByteBufCodecs.BOOL, packet -> packet.add,
            AcidWaterColorPacket::new);

    private final BlockPos waterPos;
    private final BlockPos acidPos;
    private final int distance;
    private final boolean add;

    public AcidWaterColorPacket(BlockPos waterPos, BlockPos acidPos, int distance, boolean add) {
        this.waterPos = waterPos;
        this.acidPos = acidPos;
        this.distance = distance;
        this.add = add;
    }

    @Override
    public Type<AcidWaterColorPacket> type() {
        return TYPE;
    }

    public static void handle(AcidWaterColorPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleAcidWaterColor(
                payload.waterPos, payload.acidPos, payload.distance, payload.add));
    }
}
