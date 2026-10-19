package org.tdddd.epca.impl.network.packet.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ClientPacketHandlers;

/**
 * Server -&gt; client: one Alayavijnana slash was cut, draw it here.
 *
 * <p>Fire-and-forget: the client drops the entry after its hold + fade time, so a client that joins
 * later simply never saw it.</p>
 */
public class SpawnArayaSlashPacket implements CustomPacketPayload {

    public static final Type<SpawnArayaSlashPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "spawn_araya_slash"));

    // Wire format kept from 1.20.1: 3 doubles, 2 floats, 1 var-long.
    public static final StreamCodec<ByteBuf, SpawnArayaSlashPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, packet -> packet.x,
            ByteBufCodecs.DOUBLE, packet -> packet.y,
            ByteBufCodecs.DOUBLE, packet -> packet.z,
            ByteBufCodecs.FLOAT, packet -> packet.directionX,
            ByteBufCodecs.FLOAT, packet -> packet.directionZ,
            ByteBufCodecs.VAR_LONG, packet -> packet.startTick,
            SpawnArayaSlashPacket::new);

    private final double x;
    private final double y;
    private final double z;
    private final float directionX;
    private final float directionZ;
    private final long startTick;

    public SpawnArayaSlashPacket(double x, double y, double z, float directionX, float directionZ,
                                 long startTick) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.directionX = directionX;
        this.directionZ = directionZ;
        this.startTick = startTick;
    }

    @Override
    public Type<SpawnArayaSlashPacket> type() {
        return TYPE;
    }

    public static void handle(SpawnArayaSlashPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> ClientPacketHandlers.handleArayaSlash(
                payload.x, payload.y, payload.z, payload.directionX, payload.directionZ, payload.startTick));
    }
}
