package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: the complete set of entities carrying {@code epca:soul_protection}. */
public class SyncSoulProtectionPacket implements CustomPacketPayload {

    /** Hard cap on how many entries one batch may carry, on both the sending and the receiving side. */
    public static final int MAX_ENTRIES = 256;

    /**
     * Remaining-duration value of an effect that never expires ({@code MobEffectInstance} uses
     * {@code -1}). The client treats any negative value as "do not fade out".
     */
    public static final int INFINITE_DURATION = -1;

    public static final Type<SyncSoulProtectionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_soul_protection"));

    public static final StreamCodec<ByteBuf, SyncSoulProtectionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> value.encode((FriendlyByteBuf) buf),
            buf -> decode((FriendlyByteBuf) buf));

    private final int[] entityIds;
    private final int[] remainingTicks;

    public SyncSoulProtectionPacket(int[] entityIds, int[] remainingTicks) {
        this.entityIds = entityIds;
        this.remainingTicks = remainingTicks;
    }

    void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityIds.length);
        for (int i = 0; i < this.entityIds.length; i++) {
            buf.writeVarInt(this.entityIds[i]);
            buf.writeVarInt(this.remainingTicks[i]);
        }
    }

    static SyncSoulProtectionPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        // Checked before the arrays are allocated, so a malformed count cannot blow up the client.
        if (size < 0 || size > MAX_ENTRIES) {
            throw new IllegalArgumentException(
                    "SyncSoulProtectionPacket: " + size + " entries exceeds " + MAX_ENTRIES);
        }
        int[] entityIds = new int[size];
        int[] remainingTicks = new int[size];
        for (int i = 0; i < size; i++) {
            entityIds[i] = buf.readVarInt();
            remainingTicks[i] = buf.readVarInt();
        }
        return new SyncSoulProtectionPacket(entityIds, remainingTicks);
    }

    @Override
    public Type<SyncSoulProtectionPacket> type() {
        return TYPE;
    }

    public static void handle(SyncSoulProtectionPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleSoulProtection(
                payload.entityIds, payload.remainingTicks));
    }
}
