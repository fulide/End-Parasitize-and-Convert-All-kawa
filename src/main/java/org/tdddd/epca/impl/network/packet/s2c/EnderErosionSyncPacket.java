package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: ender erosion values of one player. */
public class EnderErosionSyncPacket implements CustomPacketPayload {

    public static final Type<EnderErosionSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "ender_erosion_sync"));

    public static final StreamCodec<ByteBuf, EnderErosionSyncPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.playerUUID,
            ByteBufCodecs.FLOAT, packet -> packet.erosionValue,
            ByteBufCodecs.INT, packet -> packet.effectLevel,
            ByteBufCodecs.FLOAT, packet -> packet.maxHealth,
            ByteBufCodecs.FLOAT, packet -> packet.currentHealth,
            EnderErosionSyncPacket::new);

    private final UUID playerUUID;
    private final float erosionValue;
    private final int effectLevel;
    private final float maxHealth;
    private final float currentHealth;

    public EnderErosionSyncPacket(UUID playerUUID, float erosionValue, int effectLevel, float maxHealth, float currentHealth) {
        this.playerUUID = playerUUID;
        this.erosionValue = erosionValue;
        this.effectLevel = effectLevel;
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
    }

    @Override
    public Type<EnderErosionSyncPacket> type() {
        return TYPE;
    }

    public static void handle(EnderErosionSyncPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleEnderErosionSync(
                payload.playerUUID,
                payload.erosionValue,
                payload.effectLevel,
                payload.maxHealth,
                payload.currentHealth));
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public float getErosionValue() {
        return erosionValue;
    }

    public int getEffectLevel() {
        return effectLevel;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }
}
