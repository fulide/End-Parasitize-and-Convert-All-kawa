package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: tint an entity for a number of ticks (conversion visuals). */
public class ColorEffectPacket implements CustomPacketPayload {

    /** Default tint duration in ticks. */
    public static final int DEFAULT_DURATION = 10;

    public static final Type<ColorEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "color_effect"));

    public static final StreamCodec<ByteBuf, ColorEffectPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, packet -> packet.entityId,
            ByteBufCodecs.INT, packet -> packet.type,
            ByteBufCodecs.INT, packet -> packet.duration,
            ColorEffectPacket::new);

    private final int entityId;
    private final int type;
    private final int duration;

    public ColorEffectPacket(LivingEntity entity, int type) {
        this(entity, type, DEFAULT_DURATION);
    }

    public ColorEffectPacket(LivingEntity entity, int type, int duration) {
        this.entityId = entity.getId();
        this.type = type;
        this.duration = duration;
    }

    private ColorEffectPacket(int entityId, int type, int duration) {
        this.entityId = entityId;
        this.type = type;
        this.duration = duration;
    }

    @Override
    public Type<ColorEffectPacket> type() {
        return TYPE;
    }

    public static void handle(ColorEffectPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleColorEffect(
                payload.entityId, payload.type, payload.duration));
    }
}
