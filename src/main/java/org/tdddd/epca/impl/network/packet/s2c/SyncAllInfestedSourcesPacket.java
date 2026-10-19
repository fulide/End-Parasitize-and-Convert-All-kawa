package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: replace the whole infested-source cache with this batch. */
public class SyncAllInfestedSourcesPacket implements CustomPacketPayload {

    public static final Type<SyncAllInfestedSourcesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_all_infested_sources"));

    // Manual codec: the 1.20.1 wire format writes the size as a plain int (not a varint).
    public static final StreamCodec<ByteBuf, SyncAllInfestedSourcesPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, value) -> value.encode((FriendlyByteBuf) buf),
                    buf -> decode((FriendlyByteBuf) buf));

    private final Collection<BlockPos> positions;

    public SyncAllInfestedSourcesPacket(Collection<BlockPos> positions) {
        this.positions = positions;
    }

    void encode(FriendlyByteBuf buf) {
        buf.writeInt(positions.size());
        for (BlockPos pos : positions) {
            buf.writeBlockPos(pos);
        }
    }

    static SyncAllInfestedSourcesPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Collection<BlockPos> positions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            positions.add(buf.readBlockPos());
        }
        return new SyncAllInfestedSourcesPacket(positions);
    }

    @Override
    public Type<SyncAllInfestedSourcesPacket> type() {
        return TYPE;
    }

    public static void handle(SyncAllInfestedSourcesPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleSyncAllInfestedSources(payload.positions));
    }
}
