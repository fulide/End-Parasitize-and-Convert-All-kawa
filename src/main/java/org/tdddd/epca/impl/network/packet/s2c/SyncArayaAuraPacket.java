package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.events.ArayaConstants;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: the complete set of players with an active Alayavijnana aura. */
public class SyncArayaAuraPacket implements CustomPacketPayload {

    /** Hard cap on how many entries one batch may carry, on the sending and receiving side. */
    public static final int MAX_ENTRIES = ArayaConstants.AURA_MAX_ENTRIES;

    public static final Type<SyncArayaAuraPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_araya_aura"));

    public static final StreamCodec<ByteBuf, SyncArayaAuraPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> value.encode((FriendlyByteBuf) buf),
            buf -> decode((FriendlyByteBuf) buf));

    private final int[] entityIds;
    private final double[] positions;

    public SyncArayaAuraPacket(int[] entityIds, double[] positions) {
        this.entityIds = entityIds;
        this.positions = positions;
    }

    void encode(FriendlyByteBuf buf) {
        int size = Math.min(this.entityIds.length, this.positions.length / 3);
        buf.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            buf.writeVarInt(this.entityIds[i]);
            buf.writeDouble(this.positions[i * 3]);
            buf.writeDouble(this.positions[i * 3 + 1]);
            buf.writeDouble(this.positions[i * 3 + 2]);
        }
    }

    static SyncArayaAuraPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        // Checked before the arrays are allocated, so a malformed count cannot blow up the client.
        if (size < 0 || size > MAX_ENTRIES) {
            throw new IllegalArgumentException(
                    "SyncArayaAuraPacket: " + size + " entries exceeds " + MAX_ENTRIES);
        }
        int[] entityIds = new int[size];
        double[] positions = new double[size * 3];
        for (int i = 0; i < size; i++) {
            entityIds[i] = buf.readVarInt();
            positions[i * 3] = buf.readDouble();
            positions[i * 3 + 1] = buf.readDouble();
            positions[i * 3 + 2] = buf.readDouble();
        }
        return new SyncArayaAuraPacket(entityIds, positions);
    }

    @Override
    public Type<SyncArayaAuraPacket> type() {
        return TYPE;
    }

    public static void handle(SyncArayaAuraPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleArayaAura(payload.entityIds, payload.positions));
    }
}
