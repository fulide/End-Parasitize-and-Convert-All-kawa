package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;

/** Client -&gt; server: teleport-style motion applied to another player (wing chest ability). */
public class PlayerMotionPacket implements CustomPacketPayload {

    public static final Type<PlayerMotionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "player_motion"));

    public static final StreamCodec<ByteBuf, PlayerMotionPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.playerId,
            ByteBufCodecs.DOUBLE, packet -> packet.motion.x,
            ByteBufCodecs.DOUBLE, packet -> packet.motion.y,
            ByteBufCodecs.DOUBLE, packet -> packet.motion.z,
            (playerId, x, y, z) -> new PlayerMotionPacket(playerId, new Vec3(x, y, z)));

    private final UUID playerId;
    private final Vec3 motion;

    public PlayerMotionPacket(UUID playerId, Vec3 motion) {
        this.playerId = playerId;
        this.motion = motion;
    }

    @Override
    public Type<PlayerMotionPacket> type() {
        return TYPE;
    }

    public static void handle(PlayerMotionPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) {
                return;
            }
            ServerPlayer targetPlayer = sender.server.getPlayerList().getPlayer(payload.playerId);
            if (targetPlayer != null) {
                targetPlayer.setDeltaMovement(payload.motion);
            }
        });
    }
}
