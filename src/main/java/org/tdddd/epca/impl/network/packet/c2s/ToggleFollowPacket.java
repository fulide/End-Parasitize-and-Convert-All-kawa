package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.data.NestLeaderManager;
import org.tdddd.epca.impl.overworld.registry.entities.IParasite;

/** Client -&gt; server: toggle whether nearby parasites follow the nest leader. */
public class ToggleFollowPacket implements CustomPacketPayload {

    public static final Type<ToggleFollowPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "toggle_follow"));

    public static final StreamCodec<ByteBuf, ToggleFollowPacket> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
            }, buf -> new ToggleFollowPacket());

    public ToggleFollowPacket() {
    }

    @Override
    public Type<ToggleFollowPacket> type() {
        return TYPE;
    }

    public static void handle(ToggleFollowPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!NestLeaderManager.isNestLeader(player.getUUID())) {
                return;
            }

            ServerLevel level = (ServerLevel) player.level();
            AABB box = player.getBoundingBox().inflate(16);
            List<Entity> parasites = level.getEntities(player, box, e -> e instanceof IParasite);

            boolean hasFollower = false;
            for (Entity e : parasites) {
                if (e instanceof IParasite p) {
                    if (player.getUUID().equals(p.getFollowTarget())) {
                        hasFollower = true;
                        break;
                    }
                }
            }

            boolean follow = !hasFollower;
            for (Entity e : parasites) {
                ((IParasite) e).setFollowTarget(follow ? player.getUUID() : null);
            }
        });
    }
}
