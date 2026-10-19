package org.tdddd.epca.impl.overworld.registry.items.item;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.tdddd.epca.impl.overworld.registry.ModDataComponents;

public abstract class WingChestItem extends ArmorItem {
    public final WingChestManager.WingType wingType;

    /** 1.21.1 {@link ArmorItem} needs a {@link Holder} of the registered armor material. */
    public WingChestItem(Holder<ArmorMaterial> material, Type type, Properties properties, WingChestManager.WingType wingType) {
        super(material, type, properties);
        this.wingType = wingType;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        
        if (!ModDataComponents.has(stack, ModDataComponents.ADAPTATION_COUNT)) {
            ModDataComponents.setInt(stack, ModDataComponents.ADAPTATION_COUNT, 0);
        }
    }

    /** 1.20.1 {@code TickEvent.PlayerTickEvent} (START phase) became {@code PlayerTickEvent.Pre} in 1.21.1. */
    @SubscribeEvent
     public void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);

        
        if (chestItem.getItem() == this &&
                !WingChestManager.activeWingTypes.containsKey(player.getUUID())) {
            WingChestManager.registerWingPlayer(player, wingType);
        }
    }
}
