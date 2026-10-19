package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: evolution stage of one dimension. */
public class SyncEvolutionStagePacket implements CustomPacketPayload {

    public static final Type<SyncEvolutionStagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_evolution_stage"));

    public static final StreamCodec<ByteBuf, SyncEvolutionStagePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, packet -> packet.dimension,
            ByteBufCodecs.INT, packet -> packet.stage,
            SyncEvolutionStagePacket::new);

    private final ResourceLocation dimension;
    private final int stage;

    public SyncEvolutionStagePacket(ResourceLocation dimension, int stage) {
        this.dimension = dimension;
        this.stage = stage;
    }

    @Override
    public Type<SyncEvolutionStagePacket> type() {
        return TYPE;
    }

    public static void handle(SyncEvolutionStagePacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleEvolutionStage(
                payload.dimension, payload.stage));
    }
}
