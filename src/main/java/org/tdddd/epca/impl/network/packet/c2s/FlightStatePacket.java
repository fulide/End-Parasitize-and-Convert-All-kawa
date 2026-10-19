package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.items.item.WingChestManager;

/** Client -&gt; server: the local player's wing-chest flight state changed. */
public class FlightStatePacket implements CustomPacketPayload {

    public static final Type<FlightStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "flight_state"));

    public static final StreamCodec<ByteBuf, FlightStatePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.playerId,
            ByteBufCodecs.BOOL, packet -> packet.flying,
            FlightStatePacket::new);

    private final boolean flying;
    private final UUID playerId;

    public FlightStatePacket(UUID playerId, boolean flying) {
        this.playerId = playerId;
        this.flying = flying;
    }

    @Override
    public Type<FlightStatePacket> type() {
        return TYPE;
    }

    public static void handle(FlightStatePacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) {
                return;
            }
            ServerLevel level = sender.serverLevel();
            // Same lookup as the 1.20.1 handler: resolve the player the client named, not the sender.
            ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(payload.playerId);
            if (player != null) {
                WingChestManager.syncFlightState(player, payload.flying);
            }
        });
    }
}
