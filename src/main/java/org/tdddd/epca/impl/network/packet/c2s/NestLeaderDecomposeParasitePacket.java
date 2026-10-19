package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;

/**
 * Client -&gt; server: decompose the parasite the nest leader is looking at.
 *
 * <p>No payload data: the server re-picks the target from the sender's eye position and look angle
 * (see {@link NestLeaderDecomposeParasiteHandler#pickParasite(ServerPlayer)}), exactly as in 1.20.1.</p>
 */
public class NestLeaderDecomposeParasitePacket implements CustomPacketPayload {

    public static final Type<NestLeaderDecomposeParasitePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "nest_leader_decompose_parasite"));

    public static final StreamCodec<ByteBuf, NestLeaderDecomposeParasitePacket> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
            }, buf -> new NestLeaderDecomposeParasitePacket());

    public NestLeaderDecomposeParasitePacket() {
    }

    @Override
    public Type<NestLeaderDecomposeParasitePacket> type() {
        return TYPE;
    }

    public static void handle(NestLeaderDecomposeParasitePacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                NestLeaderDecomposeParasiteHandler.handleRequest(sender);
            }
        });
    }
}
