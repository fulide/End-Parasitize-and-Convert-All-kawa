package org.tdddd.yawning_neko_api.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.yawning_neko_api.data.DamageAdaptation;
import org.tdddd.yawning_neko_api.network.ModNetwork;

/**
 * Server → client: the new adaptation level of {@code damageTypeKeyStr} for an entity.
 */
public record AdaptationDataSyncPacket(
        int entityId, int adaptationLevel, String damageTypeKeyStr, int maxAdaptations)
        implements CustomPacketPayload {
    public static final Type<AdaptationDataSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModNetwork.MOD_ID, "adaptation_data_sync"));

    public static final StreamCodec<ByteBuf, AdaptationDataSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AdaptationDataSyncPacket::entityId,
            ByteBufCodecs.VAR_INT,
            AdaptationDataSyncPacket::adaptationLevel,
            ByteBufCodecs.STRING_UTF8,
            AdaptationDataSyncPacket::damageTypeKeyStr,
            ByteBufCodecs.VAR_INT,
            AdaptationDataSyncPacket::maxAdaptations,
            AdaptationDataSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AdaptationDataSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            Player player = context.player();
            if (player.level().getEntity(payload.entityId()) instanceof LivingEntity living) {
                DamageAdaptation.updateClientAdaptationData(
                        living,
                        payload.adaptationLevel(),
                        payload.damageTypeKeyStr(),
                        payload.maxAdaptations());
            }
        });
    }
}
