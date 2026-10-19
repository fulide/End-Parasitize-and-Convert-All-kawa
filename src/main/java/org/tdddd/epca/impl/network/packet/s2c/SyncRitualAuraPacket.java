package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: the sacrifice rituals near the receiving player. */
public class SyncRitualAuraPacket implements CustomPacketPayload {

    /** Hard cap on how many entries one batch may carry, on both the sending and the receiving side. */
    public static final int MAX_ENTRIES = 32;

    public static final Type<SyncRitualAuraPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_ritual_aura"));

    public static final StreamCodec<ByteBuf, SyncRitualAuraPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> value.encode((FriendlyByteBuf) buf),
            buf -> decode((FriendlyByteBuf) buf));

    private final BlockPos[] centers;
    private final int[] remainingTicks;

    public SyncRitualAuraPacket(BlockPos[] centers, int[] remainingTicks) {
        this.centers = centers;
        this.remainingTicks = remainingTicks;
    }

    void encode(FriendlyByteBuf buf) {
        int size = Math.min(this.centers.length, this.remainingTicks.length);
        buf.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            buf.writeBlockPos(this.centers[i]);
            buf.writeVarInt(Math.max(0, this.remainingTicks[i]));
        }
    }

    static SyncRitualAuraPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        // Checked before the arrays are allocated, so a malformed count cannot blow up the client.
        if (size < 0 || size > MAX_ENTRIES) {
            throw new IllegalArgumentException(
                    "SyncRitualAuraPacket: " + size + " entries exceeds " + MAX_ENTRIES);
        }
        BlockPos[] centers = new BlockPos[size];
        int[] remainingTicks = new int[size];
        for (int i = 0; i < size; i++) {
            centers[i] = buf.readBlockPos();
            remainingTicks[i] = buf.readVarInt();
        }
        return new SyncRitualAuraPacket(centers, remainingTicks);
    }

    @Override
    public Type<SyncRitualAuraPacket> type() {
        return TYPE;
    }

    public static void handle(SyncRitualAuraPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleRitualAura(
                payload.centers, payload.remainingTicks));
    }
}
