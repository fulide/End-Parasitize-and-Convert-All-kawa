package org.tdddd.epca.impl.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.tdddd.epca.impl.network.packet.c2s.FlightStatePacket;
import org.tdddd.epca.impl.network.packet.c2s.KeyPacket;
import org.tdddd.epca.impl.network.packet.c2s.KeyPressPacket;
import org.tdddd.epca.impl.network.packet.c2s.NestLeaderDecomposeParasitePacket;
import org.tdddd.epca.impl.network.packet.c2s.NestLeaderOrganActionPacket;
import org.tdddd.epca.impl.network.packet.c2s.NestLeaderOrganTeleportPacket;
import org.tdddd.epca.impl.network.packet.c2s.PlayerMotionPacket;
import org.tdddd.epca.impl.network.packet.c2s.RequestOpenNestLeaderOrgansPacket;
import org.tdddd.epca.impl.network.packet.c2s.ToggleFollowPacket;
import org.tdddd.epca.impl.network.packet.c2s.VKeyStatePacket;
import org.tdddd.epca.impl.network.packet.s2c.AcidWaterColorPacket;
import org.tdddd.epca.impl.network.packet.s2c.BiomassSyncPacket;
import org.tdddd.epca.impl.network.packet.s2c.ClearErosionDisplayPacket;
import org.tdddd.epca.impl.network.packet.s2c.ColorEffectPacket;
import org.tdddd.epca.impl.network.packet.s2c.EnderErosionSyncPacket;
import org.tdddd.epca.impl.network.packet.s2c.InfestedSourcePacket;
import org.tdddd.epca.impl.network.packet.s2c.ScreenCorruptionPacket;
import org.tdddd.epca.impl.network.packet.s2c.SpawnArayaSlashPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncAllInfestedSourcesPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncArayaAuraPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncArayaFirePacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncEvolutionStagePacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncHotbarMarkPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncNestLeaderOrgansPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncNestLeadersPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncNoteTabsPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncRitualAuraPacket;
import org.tdddd.epca.impl.network.packet.s2c.SyncSoulProtectionPacket;

/**
 * Payload registration entry point and the historical convenience send API of the mod.
 *
 * <p>The 1.20.1 {@code SimpleChannel} is gone in NeoForge 1.21.1; every packet is now a
 * {@link CustomPacketPayload} registered in {@link #onRegisterPayloads(RegisterPayloadHandlersEvent)}.
 * The helper methods below are kept so that the rest of the mod can keep sending packets without
 * knowing about {@link PacketDistributor}.</p>
 */
public class ModNetwork {

    /** Protocol version of the channel, kept from the 1.20.1 network (was "1"). */
    private static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {
    }

    /**
     * Registers {@link #onRegisterPayloads(RegisterPayloadHandlersEvent)} on the mod event bus.
     *
     * <p>Call this from the mod constructor with the mod event bus:
     * {@code ModNetwork.register(modEventBus);} — see {@code epca.java}.</p>
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetwork::onRegisterPayloads);
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // ---------------------------------------------------------------- C2S (client -> server)
        registrar.playToServer(FlightStatePacket.TYPE, FlightStatePacket.STREAM_CODEC,
                FlightStatePacket::handle);
        registrar.playToServer(PlayerMotionPacket.TYPE, PlayerMotionPacket.STREAM_CODEC,
                PlayerMotionPacket::handle);
        registrar.playToServer(KeyPacket.TYPE, KeyPacket.STREAM_CODEC,
                KeyPacket::handle);
        registrar.playToServer(KeyPressPacket.TYPE, KeyPressPacket.STREAM_CODEC,
                KeyPressPacket::handle);
        registrar.playToServer(VKeyStatePacket.TYPE, VKeyStatePacket.STREAM_CODEC,
                VKeyStatePacket::handle);
        registrar.playToServer(ToggleFollowPacket.TYPE, ToggleFollowPacket.STREAM_CODEC,
                ToggleFollowPacket::handle);
        registrar.playToServer(RequestOpenNestLeaderOrgansPacket.TYPE,
                RequestOpenNestLeaderOrgansPacket.STREAM_CODEC,
                RequestOpenNestLeaderOrgansPacket::handle);
        registrar.playToServer(NestLeaderOrganActionPacket.TYPE,
                NestLeaderOrganActionPacket.STREAM_CODEC,
                NestLeaderOrganActionPacket::handle);
        registrar.playToServer(NestLeaderOrganTeleportPacket.TYPE,
                NestLeaderOrganTeleportPacket.STREAM_CODEC,
                NestLeaderOrganTeleportPacket::handle);
        registrar.playToServer(NestLeaderDecomposeParasitePacket.TYPE,
                NestLeaderDecomposeParasitePacket.STREAM_CODEC,
                NestLeaderDecomposeParasitePacket::handle);
        registrar.playToServer(InfestedSourcePacket.RequestAllInfestedSourcesPacket.TYPE,
                InfestedSourcePacket.RequestAllInfestedSourcesPacket.STREAM_CODEC,
                InfestedSourcePacket.RequestAllInfestedSourcesPacket::handle);

        // ---------------------------------------------------------------- S2C (server -> client)
        registrar.playToClient(AcidWaterColorPacket.TYPE, AcidWaterColorPacket.STREAM_CODEC,
                AcidWaterColorPacket::handle);
        registrar.playToClient(EnderErosionSyncPacket.TYPE, EnderErosionSyncPacket.STREAM_CODEC,
                EnderErosionSyncPacket::handle);
        registrar.playToClient(ClearErosionDisplayPacket.TYPE, ClearErosionDisplayPacket.STREAM_CODEC,
                ClearErosionDisplayPacket::handle);
        registrar.playToClient(SyncEvolutionStagePacket.TYPE, SyncEvolutionStagePacket.STREAM_CODEC,
                SyncEvolutionStagePacket::handle);
        registrar.playToClient(SyncNoteTabsPacket.TYPE, SyncNoteTabsPacket.STREAM_CODEC,
                SyncNoteTabsPacket::handle);
        registrar.playToClient(ColorEffectPacket.TYPE, ColorEffectPacket.STREAM_CODEC,
                ColorEffectPacket::handle);
        registrar.playToClient(InfestedSourcePacket.AddInfestedSourcePacket.TYPE,
                InfestedSourcePacket.AddInfestedSourcePacket.STREAM_CODEC,
                InfestedSourcePacket.AddInfestedSourcePacket::handle);
        registrar.playToClient(InfestedSourcePacket.RemoveInfestedSourcePacket.TYPE,
                InfestedSourcePacket.RemoveInfestedSourcePacket.STREAM_CODEC,
                InfestedSourcePacket.RemoveInfestedSourcePacket::handle);
        registrar.playToClient(SyncAllInfestedSourcesPacket.TYPE,
                SyncAllInfestedSourcesPacket.STREAM_CODEC,
                SyncAllInfestedSourcesPacket::handle);
        registrar.playToClient(BiomassSyncPacket.TYPE, BiomassSyncPacket.STREAM_CODEC,
                BiomassSyncPacket::handle);
        registrar.playToClient(SyncNestLeadersPacket.TYPE, SyncNestLeadersPacket.STREAM_CODEC,
                SyncNestLeadersPacket::handle);
        registrar.playToClient(SyncSoulProtectionPacket.TYPE, SyncSoulProtectionPacket.STREAM_CODEC,
                SyncSoulProtectionPacket::handle);
        registrar.playToClient(SyncRitualAuraPacket.TYPE, SyncRitualAuraPacket.STREAM_CODEC,
                SyncRitualAuraPacket::handle);
        registrar.playToClient(SyncArayaAuraPacket.TYPE, SyncArayaAuraPacket.STREAM_CODEC,
                SyncArayaAuraPacket::handle);
        registrar.playToClient(SyncArayaFirePacket.TYPE, SyncArayaFirePacket.STREAM_CODEC,
                SyncArayaFirePacket::handle);
        registrar.playToClient(SpawnArayaSlashPacket.TYPE, SpawnArayaSlashPacket.STREAM_CODEC,
                SpawnArayaSlashPacket::handle);
        registrar.playToClient(SyncHotbarMarkPacket.TYPE, SyncHotbarMarkPacket.STREAM_CODEC,
                SyncHotbarMarkPacket::handle);
        registrar.playToClient(ScreenCorruptionPacket.TYPE, ScreenCorruptionPacket.STREAM_CODEC,
                ScreenCorruptionPacket::handle);
        registrar.playToClient(SyncNestLeaderOrgansPacket.TYPE,
                SyncNestLeaderOrgansPacket.STREAM_CODEC,
                SyncNestLeaderOrgansPacket::handle);
    }

    // -------------------------------------------------------------------- send helpers

    /** Sends a payload to one player. */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Object-typed overload kept so legacy call sites that hold the packet as {@code Object} compile. */
    public static void sendToPlayer(ServerPlayer player, Object payload) {
        sendToPlayer(player, (CustomPacketPayload) payload);
    }

    /** Sends a payload to every player on the server. */
    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    /** Object-typed overload kept for legacy call sites. */
    public static void sendToAll(Object payload) {
        sendToAll((CustomPacketPayload) payload);
    }

    /**
     * Sends a payload from the client to the server.
     *
     * <p>This is the replacement of the old {@code send(PacketTarget, MSG)}: a packet target no
     * longer exists on NeoForge 1.21.1, and the only client-originated direction is "to the server".</p>
     */
    public static void send(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    /** Object-typed overload kept for legacy call sites. */
    public static void send(Object payload) {
        send((CustomPacketPayload) payload);
    }

    /** Sends a payload to the server (client side only). */
    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    /** Object-typed overload kept for legacy call sites. */
    public static void sendToServer(Object payload) {
        sendToServer((CustomPacketPayload) payload);
    }

    /**
     * Sends a payload to every player tracking the given entity (legacy argument order is
     * {@code (payload, entity)}).
     */
    public static void sendToAllTracking(CustomPacketPayload payload, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    /** Object-typed overload kept for legacy call sites. */
    public static void sendToAllTracking(Object payload, Entity entity) {
        sendToAllTracking((CustomPacketPayload) payload, entity);
    }

    /**
     * Sends a payload to every player tracking the given entity and to the entity itself
     * (legacy argument order is {@code (entity, payload)}).
     */
    public static void sendToAllTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    /** Object-typed overload kept for legacy call sites. */
    public static void sendToAllTracking(Entity entity, Object payload) {
        sendToAllTracking(entity, (CustomPacketPayload) payload);
    }
}
