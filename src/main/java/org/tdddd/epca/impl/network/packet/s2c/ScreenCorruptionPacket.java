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

/** Server -&gt; client: start or stop the BioTort screen-corruption effect. */
public class ScreenCorruptionPacket implements CustomPacketPayload {

    public static final Type<ScreenCorruptionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "screen_corruption"));

    // Wire format kept from 1.20.1: boolean + two varints (negatives clamped to 0 on encode).
    public static final StreamCodec<ByteBuf, ScreenCorruptionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, packet -> packet.active,
            ByteBufCodecs.VAR_INT, packet -> Math.max(0, packet.corruptionTicks),
            ByteBufCodecs.VAR_INT, packet -> Math.max(0, packet.decayTicks),
            ScreenCorruptionPacket::new);

    private final boolean active;
    private final int corruptionTicks;
    private final int decayTicks;

    public ScreenCorruptionPacket(boolean active, int corruptionTicks, int decayTicks) {
        this.active = active;
        this.corruptionTicks = corruptionTicks;
        this.decayTicks = decayTicks;
    }

    /** Start the corruption with the given timings. */
    public static ScreenCorruptionPacket start(int corruptionTicks, int decayTicks) {
        return new ScreenCorruptionPacket(true, corruptionTicks, decayTicks);
    }

    /** Stop the corruption / clear the overlay. */
    public static ScreenCorruptionPacket stop() {
        return new ScreenCorruptionPacket(false, 0, 0);
    }

    @Override
    public Type<ScreenCorruptionPacket> type() {
        return TYPE;
    }

    public static void handle(ScreenCorruptionPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleScreenCorruption(
                payload.active, payload.corruptionTicks, payload.decayTicks));
    }
}
