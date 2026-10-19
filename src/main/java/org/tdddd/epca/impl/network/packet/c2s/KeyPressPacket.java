package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.events.KeyInputHandler;

/** Client -&gt; server: space / shift key state for the given player. */
public class KeyPressPacket implements CustomPacketPayload {

    public static final Type<KeyPressPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "key_press"));

    public static final StreamCodec<ByteBuf, KeyPressPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.playerId,
            ByteBufCodecs.BOOL, packet -> packet.spacePressed,
            ByteBufCodecs.BOOL, packet -> packet.shiftPressed,
            KeyPressPacket::new);

    private final UUID playerId;
    private final boolean spacePressed;
    private final boolean shiftPressed;

    public KeyPressPacket(UUID playerId, boolean spacePressed, boolean shiftPressed) {
        this.playerId = playerId;
        this.spacePressed = spacePressed;
        this.shiftPressed = shiftPressed;
    }

    @Override
    public Type<KeyPressPacket> type() {
        return TYPE;
    }

    public static void handle(KeyPressPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> KeyInputHandler.updateKeyState(
                payload.playerId,
                payload.spacePressed,
                payload.shiftPressed));
    }
}
