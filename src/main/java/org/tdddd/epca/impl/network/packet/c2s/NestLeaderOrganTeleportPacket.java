package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;

/** Client -&gt; server: nest leader organ teleport (SPEC B). No payload data. */
public class NestLeaderOrganTeleportPacket implements CustomPacketPayload {

    public static final Type<NestLeaderOrganTeleportPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "nest_leader_organ_teleport"));

    public static final StreamCodec<ByteBuf, NestLeaderOrganTeleportPacket> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
            }, buf -> new NestLeaderOrganTeleportPacket());

    public NestLeaderOrganTeleportPacket() {
    }

    @Override
    public Type<NestLeaderOrganTeleportPacket> type() {
        return TYPE;
    }

    public static void handle(NestLeaderOrganTeleportPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                NestLeaderOrganTeleportHandler.handleRequest(sender);
            }
        });
    }
}
