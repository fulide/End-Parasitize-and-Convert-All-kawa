package org.tdddd.epca.impl.network.packet.c2s;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.items.item.DiseasedHeart;
import org.tdddd.epca.impl.overworld.registry.items.item.InfestedFlesh;
import org.tdddd.epca.impl.overworld.registry.items.item.LivingArmorBox;
import org.tdddd.epca.impl.overworld.registry.items.item.ParasiteViscera;

/** Client -&gt; server: the mod's "key" was pressed (living-armor box left click in GUI). No payload data. */
public class KeyPacket implements CustomPacketPayload {

    public static final Type<KeyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(epca.MODID, "key"));

    public static final StreamCodec<ByteBuf, KeyPacket> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
            }, buf -> new KeyPacket());

    public KeyPacket() {
    }

    @Override
    public Type<KeyPacket> type() {
        return TYPE;
    }

    public static void handle(KeyPacket payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                handleKeyAction(player);
            }
        });
    }

    private static void handleKeyAction(ServerPlayer player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        // 1.21.1: ItemStack#isEdible() is gone; edibility is the presence of the FOOD data component.
        if (!mainHandItem.isEmpty() && (mainHandItem.has(DataComponents.FOOD) || mainHandItem.getItem() instanceof InfestedFlesh || mainHandItem.getItem() instanceof DiseasedHeart
                || mainHandItem.getItem() instanceof ParasiteViscera) &&
                offHandItem.getItem() instanceof LivingArmorBox) {

            LivingArmorBox box = (LivingArmorBox) offHandItem.getItem();
            box.handleLeftClickInGUI(player, offHandItem, mainHandItem);
        } else if (!offHandItem.isEmpty() && (offHandItem.has(DataComponents.FOOD) || offHandItem.getItem() instanceof InfestedFlesh || offHandItem.getItem() instanceof DiseasedHeart
                || offHandItem.getItem() instanceof ParasiteViscera) &&
                mainHandItem.getItem() instanceof LivingArmorBox) {

            LivingArmorBox box = (LivingArmorBox) mainHandItem.getItem();
            box.handleLeftClickInGUI(player, mainHandItem, offHandItem);
        }
    }
}
