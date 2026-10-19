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

/** Server -&gt; client: the render-only fire blocks the Alayavijnana aura keeps alive. */
public class SyncArayaFirePacket implements CustomPacketPayload {

    /** Hard cap on how many fire blocks one batch may carry, on the sending and receiving side. */
    public static final int MAX_ENTRIES = 256;

    /** Height decode factor of the byte written by {@link #encode(FriendlyByteBuf)}. */
    public static final float HEIGHT_SCALE = 255.0F;

    public static final Type<SyncArayaFirePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_araya_fire"));

    public static final StreamCodec<ByteBuf, SyncArayaFirePacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> value.encode((FriendlyByteBuf) buf),
            buf -> decode((FriendlyByteBuf) buf));

    private final long rolledAtTick;
    private final long[] bornAgoTicks;
    private final int[] xs;
    private final int[] ys;
    private final int[] zs;
    private final int[] lifetimes;
    private final float[] heights;

    public SyncArayaFirePacket(long rolledAtTick, long[] bornAgoTicks, int[] xs, int[] ys, int[] zs,
                               int[] lifetimes, float[] heights) {
        this.rolledAtTick = rolledAtTick;
        this.bornAgoTicks = bornAgoTicks;
        this.xs = xs;
        this.ys = ys;
        this.zs = zs;
        this.lifetimes = lifetimes;
        this.heights = heights;
    }

    void encode(FriendlyByteBuf buf) {
        int size = Math.min(Math.min(this.xs.length, this.ys.length), Math.min(this.zs.length,
                this.lifetimes.length));
        size = Math.min(size, this.heights.length);
        size = Math.min(size, this.bornAgoTicks.length);
        size = Math.min(size, MAX_ENTRIES);
        buf.writeVarInt(size);
        buf.writeVarLong(this.rolledAtTick);
        for (int i = 0; i < size; i++) {
            buf.writeBlockPos(new BlockPos(this.xs[i], this.ys[i], this.zs[i]));
            buf.writeVarInt(Math.max(1, this.lifetimes[i]));
            // 1/255-block steps: the client multiplies by the same factor.
            int height = Math.round(this.heights[i] * 255.0F);
            buf.writeByte(Math.max(0, Math.min(255, height)));
            // Age of this entry relative to the batch tick: the fire field keeps overlapping waves, so
            // every block carries its own birthday instead of sharing the wave's.
            buf.writeVarInt((int) Math.min(Integer.MAX_VALUE, Math.max(0L, this.bornAgoTicks[i])));
        }
    }

    static SyncArayaFirePacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        // Checked before the arrays are allocated, so a malformed count cannot blow up the client.
        if (size < 0 || size > MAX_ENTRIES) {
            throw new IllegalArgumentException(
                    "SyncArayaFirePacket: " + size + " entries exceeds " + MAX_ENTRIES);
        }
        long rolledAtTick = buf.readVarLong();
        int[] xs = new int[size];
        int[] ys = new int[size];
        int[] zs = new int[size];
        int[] lifetimes = new int[size];
        float[] heights = new float[size];
        long[] bornAgoTicks = new long[size];
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            xs[i] = pos.getX();
            ys[i] = pos.getY();
            zs[i] = pos.getZ();
            lifetimes[i] = buf.readVarInt();
            heights[i] = (buf.readByte() & 0xFF) / HEIGHT_SCALE;
            bornAgoTicks[i] = buf.readVarInt();
        }
        return new SyncArayaFirePacket(rolledAtTick, bornAgoTicks, xs, ys, zs, lifetimes, heights);
    }

    @Override
    public Type<SyncArayaFirePacket> type() {
        return TYPE;
    }

    public static void handle(SyncArayaFirePacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleArayaFire(
                payload.xs, payload.ys, payload.zs, payload.rolledAtTick,
                payload.bornAgoTicks, payload.lifetimes, payload.heights));
    }
}
