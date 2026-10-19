package org.tdddd.yawning_neko_api.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.yawning_neko_api.client.ClientPacketHandlers;
import org.tdddd.yawning_neko_api.network.ModNetwork;

/**
 * Server → client: an adaptation effect (1 = partial, 2 = full) just fired for an entity.
 */
public record AdaptationEventSyncPacket(int entityId, int eventType) implements CustomPacketPayload {
    public static final Type<AdaptationEventSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModNetwork.MOD_ID, "adaptation_event_sync"));

    public static final StreamCodec<ByteBuf, AdaptationEventSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AdaptationEventSyncPacket::entityId,
            ByteBufCodecs.VAR_INT,
            AdaptationEventSyncPacket::eventType,
            AdaptationEventSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AdaptationEventSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            ClientPacketHandlers.handleAdaptationEvent(payload.entityId(), payload.eventType());
        });
    }
}
