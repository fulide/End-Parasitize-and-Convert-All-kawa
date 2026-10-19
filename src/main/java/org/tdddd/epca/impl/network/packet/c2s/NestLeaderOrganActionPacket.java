package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.network.ModNetwork;
import org.tdddd.epca.impl.network.packet.s2c.SyncNestLeaderOrgansPacket;
import org.tdddd.epca.impl.overworld.data.NestLeaderManager;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganData;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganGate;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganSavedData;

/**
 * Client -&gt; server: one GUI action of the nest leader organ screen.
 *
 * <p>Wire format is unchanged from 1.20.1: action id, slot and button, all as varints.</p>
 */
public class NestLeaderOrganActionPacket implements CustomPacketPayload {

    public static final Type<NestLeaderOrganActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "nest_leader_organ_action"));

    public static final StreamCodec<ByteBuf, NestLeaderOrganActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, packet -> packet.action.id(),
            ByteBufCodecs.VAR_INT, packet -> packet.slot,
            ByteBufCodecs.VAR_INT, packet -> packet.button,
            (actionId, slot, button) -> new NestLeaderOrganActionPacket(Action.byId(actionId), slot, button));

    /** Actions the organ GUI can send. */
    public enum Action {
        /** Take / put one stack. */
        CLICK,
        /** Shift move. */
        QUICK_MOVE,
        /** Clear the slot. */
        CLEAR,
        /** Return the carried stack. */
        RETURN_CARRIED,
        /** An inventory slot (0-35) is clicked. */
        INVENTORY_SLOT;

        /** Stable wire id; equal to ordinal. */
        public int id() {
            return switch (this) {
                case CLICK -> 0;
                case QUICK_MOVE -> 1;
                case CLEAR -> 2;
                case RETURN_CARRIED -> 3;
                case INVENTORY_SLOT -> 4;
            };
        }

        @Nullable
        public static Action byId(int id) {
            return switch (id) {
                case 0 -> CLICK;
                case 1 -> QUICK_MOVE;
                case 2 -> CLEAR;
                case 3 -> RETURN_CARRIED;
                case 4 -> INVENTORY_SLOT;
                default -> null;
            };
        }
    }

    /** {@code Inventory#items} size: 3 rows of 9 plus the hotbar. */
    public static final int INVENTORY_SLOT_COUNT = 36;

    /** Left click. */
    public static final int BUTTON_LEFT = 0;
    /** Right click. */
    public static final int BUTTON_RIGHT = 1;
    /** Middle click, reused as the "quick move from inventory" marker for {@link Action#INVENTORY_SLOT}. */
    public static final int BUTTON_MIDDLE = 2;
    /** Shift click on an inventory slot. */
    public static final int BUTTON_INVENTORY_QUICK_MOVE = BUTTON_MIDDLE;

    private final Action action;
    private final int slot;
    private final int button;

    public NestLeaderOrganActionPacket(Action action, int slot, int button) {
        this.action = action == null ? Action.RETURN_CARRIED : action;
        this.slot = slot;
        this.button = button;
    }

    public NestLeaderOrganActionPacket(Action action, int slot) {
        this(action, slot, BUTTON_LEFT);
    }

    /** Return whatever the cursor carries to the player. */
    public static NestLeaderOrganActionPacket returnCarried() {
        return new NestLeaderOrganActionPacket(Action.RETURN_CARRIED, -1, BUTTON_LEFT);
    }

    /** Click an inventory slot (0-35). */
    public static NestLeaderOrganActionPacket inventorySlot(int inventorySlot, int button) {
        return new NestLeaderOrganActionPacket(Action.INVENTORY_SLOT, inventorySlot, button);
    }

    /** Shift click an inventory slot. */
    public static NestLeaderOrganActionPacket inventoryQuickMove(int inventorySlot) {
        return new NestLeaderOrganActionPacket(Action.INVENTORY_SLOT, inventorySlot,
                BUTTON_INVENTORY_QUICK_MOVE);
    }

    public Action action() {
        return action;
    }

    public int slot() {
        return slot;
    }

    public int button() {
        return button;
    }

    @Override
    public Type<NestLeaderOrganActionPacket> type() {
        return TYPE;
    }

    public static void handle(NestLeaderOrganActionPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) {
                return;
            }
            if (!NestLeaderManager.isNestLeader(sender.getUUID())) {
                return;
            }

            NestLeaderOrganData data = NestLeaderOrganSavedData.readOrCreate(sender);
            NestLeaderOrganActionHandler.handle(sender, data, payload);
            NestLeaderOrganSavedData.write(sender, data);

            NestLeaderOrganGate.Gates gates = NestLeaderOrganGate.scan(sender);
            ModNetwork.sendToPlayer(sender, new SyncNestLeaderOrgansPacket(data, gates, false));
        });
    }
}
