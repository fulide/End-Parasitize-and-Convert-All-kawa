package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: the complete set of nest leader UUIDs. */
public class SyncNestLeadersPacket implements CustomPacketPayload {

    public static final Type<SyncNestLeadersPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_nest_leaders"));

    // Manual codec: the 1.20.1 wire format writes the size as a plain int (not a varint).
    public static final StreamCodec<ByteBuf, SyncNestLeadersPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> value.encode((FriendlyByteBuf) buf),
            buf -> new SyncNestLeadersPacket((FriendlyByteBuf) buf));

    private final Set<UUID> leaders;

    public SyncNestLeadersPacket(Set<UUID> leaders) {
        this.leaders = new HashSet<>(leaders);
    }

    public SyncNestLeadersPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        leaders = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            leaders.add(buf.readUUID());
        }
    }

    void encode(FriendlyByteBuf buf) {
        buf.writeInt(leaders.size());
        for (UUID uuid : leaders) {
            buf.writeUUID(uuid);
        }
    }

    @Override
    public Type<SyncNestLeadersPacket> type() {
        return TYPE;
    }

    public static void handle(SyncNestLeadersPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleNestLeaders(payload.leaders));
    }
}
