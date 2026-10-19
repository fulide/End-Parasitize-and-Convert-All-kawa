package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganData;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganGate;

/** Server -&gt; client: nest leader organ data, inner-slot gates and whether to open the GUI. */
public class SyncNestLeaderOrgansPacket implements CustomPacketPayload {

    public static final Type<SyncNestLeaderOrgansPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_nest_leader_organs"));

    // Wire format kept from 1.20.1: compound tag + 3 booleans.
    public static final StreamCodec<ByteBuf, SyncNestLeaderOrgansPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, packet -> packet.organData,
            ByteBufCodecs.BOOL, packet -> packet.torsoInnerOpen,
            ByteBufCodecs.BOOL, packet -> packet.headInnerOpen,
            ByteBufCodecs.BOOL, packet -> packet.openScreen,
            SyncNestLeaderOrgansPacket::new);

    private final CompoundTag organData;
    private final boolean torsoInnerOpen;
    private final boolean headInnerOpen;
    private final boolean openScreen;

    /** STAGE 1 constructor: opens the screen. */
    public SyncNestLeaderOrgansPacket(NestLeaderOrganData data, NestLeaderOrganGate.Gates gates) {
        this(data, gates, true);
    }

    public SyncNestLeaderOrgansPacket(NestLeaderOrganData data, NestLeaderOrganGate.Gates gates,
                                      boolean openScreen) {
        CompoundTag tag = new CompoundTag();
        data.save(tag);
        this.organData = tag;
        this.torsoInnerOpen = gates != null && gates.torsoInnerOpen();
        this.headInnerOpen = gates != null && gates.headInnerOpen();
        this.openScreen = openScreen;
    }

    public SyncNestLeaderOrgansPacket(CompoundTag organData, boolean torsoInnerOpen, boolean headInnerOpen) {
        this(organData, torsoInnerOpen, headInnerOpen, true);
    }

    public SyncNestLeaderOrgansPacket(CompoundTag organData, boolean torsoInnerOpen, boolean headInnerOpen,
                                      boolean openScreen) {
        this.organData = organData == null ? new CompoundTag() : organData;
        this.torsoInnerOpen = torsoInnerOpen;
        this.headInnerOpen = headInnerOpen;
        this.openScreen = openScreen;
    }

    public boolean openScreen() {
        return openScreen;
    }

    @Override
    public Type<SyncNestLeaderOrgansPacket> type() {
        return TYPE;
    }

    public static void handle(SyncNestLeaderOrgansPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleNestLeaderOrgans(
                payload.organData, payload.torsoInnerOpen, payload.headInnerOpen, payload.openScreen));
    }
}
