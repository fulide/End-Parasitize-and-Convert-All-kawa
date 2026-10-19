package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;
import org.tdddd.epca.impl.overworld.data.EPCANoteTabData;

/** Server -&gt; client: the EPCA note tab tree. */
public class SyncNoteTabsPacket implements CustomPacketPayload {

    public static final Type<SyncNoteTabsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "sync_note_tabs"));

    // The tab serialisation lives in EPCANoteTabData and works on a FriendlyByteBuf.
    public static final StreamCodec<ByteBuf, SyncNoteTabsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> EPCANoteTabData.writeParentTabList((FriendlyByteBuf) buf, value.tabs),
            buf -> new SyncNoteTabsPacket(EPCANoteTabData.readParentTabList((FriendlyByteBuf) buf)));

    private final List<EPCANoteTabData.ParentTab> tabs;

    public SyncNoteTabsPacket(List<EPCANoteTabData.ParentTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    public Type<SyncNoteTabsPacket> type() {
        return TYPE;
    }

    public static void handle(SyncNoteTabsPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleNoteTabs(payload.tabs));
    }
}
