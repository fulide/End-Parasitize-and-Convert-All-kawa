package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/** Server -&gt; client: drop the ender-erosion display values of one player. */
public class ClearErosionDisplayPacket implements CustomPacketPayload {

    public static final Type<ClearErosionDisplayPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "clear_erosion_display"));

    public static final StreamCodec<ByteBuf, ClearErosionDisplayPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.playerUUID,
            ClearErosionDisplayPacket::new);

    private final UUID playerUUID;

    public ClearErosionDisplayPacket(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public Type<ClearErosionDisplayPacket> type() {
        return TYPE;
    }

    public static void handle(ClearErosionDisplayPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleClearErosionDisplay(payload.playerUUID));
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
