package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;

/** Client -&gt; server: state of the "V" key, stored in the player's persistent data. */
public class VKeyStatePacket implements CustomPacketPayload {

    public static final Type<VKeyStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "v_key_state"));

    public static final StreamCodec<ByteBuf, VKeyStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, packet -> packet.pressed,
            VKeyStatePacket::new);

    private final boolean pressed;

    public VKeyStatePacket(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public Type<VKeyStatePacket> type() {
        return TYPE;
    }

    public static void handle(VKeyStatePacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                player.getPersistentData().putBoolean("VKeyPressed", payload.pressed);
            }
        });
    }
}
