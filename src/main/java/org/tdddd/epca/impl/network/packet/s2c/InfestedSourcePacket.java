package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;
import org.tdddd.epca.impl.overworld.data.InfestedBlockManager;

/**
 * Infested-source block sync.
 *
 * <p>Three payloads live here: the two server -&gt; client add/remove notifications and the
 * client -&gt; server "send me everything" request.</p>
 */
public final class InfestedSourcePacket {

    private InfestedSourcePacket() {
    }

    /** Server -&gt; client: an infested source block was added. */
    public static class AddInfestedSourcePacket implements CustomPacketPayload {

        public static final Type<AddInfestedSourcePacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "add_infested_source"));

        public static final StreamCodec<ByteBuf, AddInfestedSourcePacket> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, packet -> packet.pos,
                        AddInfestedSourcePacket::new);

        private final BlockPos pos;

        public AddInfestedSourcePacket(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public Type<AddInfestedSourcePacket> type() {
            return TYPE;
        }

        public static void handle(AddInfestedSourcePacket payload, IPayloadContext context) {
            if (context.flow() != PacketFlow.CLIENTBOUND) {
                return;
            }
            context.enqueueWork(() -> ClientPacketHandlers.handleAddInfestedSource(payload.pos));
        }
    }

    /** Server -&gt; client: an infested source block was removed. */
    public static class RemoveInfestedSourcePacket implements CustomPacketPayload {

        public static final Type<RemoveInfestedSourcePacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "remove_infested_source"));

        public static final StreamCodec<ByteBuf, RemoveInfestedSourcePacket> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, packet -> packet.pos,
                        RemoveInfestedSourcePacket::new);

        private final BlockPos pos;

        public RemoveInfestedSourcePacket(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public Type<RemoveInfestedSourcePacket> type() {
            return TYPE;
        }

        public static void handle(RemoveInfestedSourcePacket payload, IPayloadContext context) {
            if (context.flow() != PacketFlow.CLIENTBOUND) {
                return;
            }
            context.enqueueWork(() -> ClientPacketHandlers.handleRemoveInfestedSource(payload.pos));
        }
    }

    /** Client -&gt; server: request the complete set of infested source blocks. */
    public static class RequestAllInfestedSourcesPacket implements CustomPacketPayload {

        public static final Type<RequestAllInfestedSourcesPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "request_all_infested_sources"));

        public static final StreamCodec<ByteBuf, RequestAllInfestedSourcesPacket> STREAM_CODEC =
                StreamCodec.of((buf, value) -> {
                }, buf -> new RequestAllInfestedSourcesPacket());

        @Override
        public Type<RequestAllInfestedSourcesPacket> type() {
            return TYPE;
        }

        public static void handle(RequestAllInfestedSourcesPacket payload, IPayloadContext context) {
            if (context.flow() != PacketFlow.SERVERBOUND) {
                return;
            }
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    InfestedBlockManager.syncAllToPlayer(player);
                }
            });
        }
    }
}
