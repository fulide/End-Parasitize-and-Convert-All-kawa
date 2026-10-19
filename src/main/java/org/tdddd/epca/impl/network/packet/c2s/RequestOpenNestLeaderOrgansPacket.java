package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ModNetwork;
import org.tdddd.epca.impl.network.packet.s2c.SyncNestLeaderOrgansPacket;
import org.tdddd.epca.impl.overworld.data.NestLeaderManager;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganData;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganGate;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganSavedData;

/** Client -&gt; server: open (or refresh) the nest leader organ GUI. */
public class RequestOpenNestLeaderOrgansPacket implements CustomPacketPayload {

    public static final Type<RequestOpenNestLeaderOrgansPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "request_open_nest_leader_organs"));

    public static final StreamCodec<ByteBuf, RequestOpenNestLeaderOrgansPacket> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
            }, buf -> new RequestOpenNestLeaderOrgansPacket());

    public RequestOpenNestLeaderOrgansPacket() {
    }

    @Override
    public Type<RequestOpenNestLeaderOrgansPacket> type() {
        return TYPE;
    }

    public static void handle(RequestOpenNestLeaderOrgansPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) {
                return;
            }
            if (!NestLeaderManager.isNestLeader(sender.getUUID())) {
                sender.displayClientMessage(
                        Component.literal("只有领巢者才能打开器官界面"), true);
                return;
            }

            NestLeaderOrganData data = NestLeaderOrganSavedData.readOrCreate(sender);
            NestLeaderOrganGate.Gates gates = NestLeaderOrganGate.scan(sender);
            ModNetwork.sendToPlayer(sender, new SyncNestLeaderOrgansPacket(data, gates));
        });
    }
}
