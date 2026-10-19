package org.tdddd.yawning_neko_api.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.tdddd.yawning_neko_api.YawningNekoApi;
import org.tdddd.yawning_neko_api.network.packet.AdaptationDataSyncPacket;
import org.tdddd.yawning_neko_api.network.packet.AdaptationEventSyncPacket;

/**
 * 1.21.1 payload registration and sending. Replaces the old Forge {@code SimpleChannel}.
 */
public final class ModNetwork {
    public static final String MOD_ID = YawningNekoApi.MODID;
    public static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                AdaptationDataSyncPacket.TYPE,
                AdaptationDataSyncPacket.STREAM_CODEC,
                AdaptationDataSyncPacket::handle);
        registrar.playToClient(
                AdaptationEventSyncPacket.TYPE,
                AdaptationEventSyncPacket.STREAM_CODEC,
                AdaptationEventSyncPacket::handle);
    }

    /**
     * Sends a payload to every player that is tracking the entity (the entity itself is not included,
     * matching the old {@code PacketDistributor.TRACKING_ENTITY} behaviour).
     */
    public static void sendToAllTracking(CustomPacketPayload payload, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }
}
