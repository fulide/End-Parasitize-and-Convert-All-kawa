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

/** Server -&gt; client: biomass points (and nest leader flag) of the local player. */
public class BiomassSyncPacket implements CustomPacketPayload {

    public static final Type<BiomassSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "biomass_sync"));

    public static final StreamCodec<ByteBuf, BiomassSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, packet -> packet.isNestLeader,
            ByteBufCodecs.INT, packet -> packet.points,
            BiomassSyncPacket::new);

    private final boolean isNestLeader;
    private final int points;

    public BiomassSyncPacket(boolean isNestLeader, int points) {
        this.isNestLeader = isNestLeader;
        this.points = points;
    }

    @Override
    public Type<BiomassSyncPacket> type() {
        return TYPE;
    }

    public static void handle(BiomassSyncPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleBiomassSync(payload.isNestLeader, payload.points));
    }
}
