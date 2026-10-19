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

/** Server -&gt; client: which hotbar slots are marked by the BioTort effect, for how long. */
public class SyncHotbarMarkPacket implements CustomPacketPayload {

    /** Number of bits of {@code slotMask} (hotbar slots 0..8). */
    public static final int SLOT_MASK_BITS = 9;

    public static final Type<SyncHotbarMarkPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_hotbar_mark"));

    // Wire format kept from 1.20.1: two varints.
    public static final StreamCodec<ByteBuf, SyncHotbarMarkPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, packet -> packet.slotMask,
            ByteBufCodecs.VAR_INT, packet -> packet.durationTicks,
            SyncHotbarMarkPacket::new);

    private final int slotMask;
    private final int durationTicks;

    public SyncHotbarMarkPacket(int slotMask, int durationTicks) {
        this.slotMask = slotMask;
        this.durationTicks = durationTicks;
    }

    /** Mark the given hotbar slots. */
    public static SyncHotbarMarkPacket mark(int slotMask, int durationTicks) {
        return new SyncHotbarMarkPacket(slotMask, durationTicks);
    }

    /** Clear every hotbar mark. */
    public static SyncHotbarMarkPacket clear() {
        return new SyncHotbarMarkPacket(0, 0);
    }

    @Override
    public Type<SyncHotbarMarkPacket> type() {
        return TYPE;
    }

    public static void handle(SyncHotbarMarkPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleHotbarMark(
                payload.slotMask, payload.durationTicks));
    }
}
