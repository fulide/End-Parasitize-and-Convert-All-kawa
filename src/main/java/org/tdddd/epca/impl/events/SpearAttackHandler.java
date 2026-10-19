package org.tdddd.epca.impl.events;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModItems;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class SpearAttackHandler {

    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        
        if (!(event.getEntity().level().isClientSide)) return;

        
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

        
        ItemStack mainHand = player.getMainHandItem();
        boolean isSpear = mainHand.getItem() == ModItems.WOODEN_SPEAR.get() ||
                mainHand.getItem() == ModItems.STONE_SPEAR.get() ||
                mainHand.getItem() == ModItems.FLINT_SPEAR.get() ||
                mainHand.getItem() == ModItems.COPPER_SPEAR.get() ||
                mainHand.getItem() == ModItems.IRON_SPEAR.get() ||
                mainHand.getItem() == ModItems.GOLDEN_SPEAR.get() ||
                mainHand.getItem() == ModItems.DIAMOND_SPEAR.get() ||
                mainHand.getItem() == ModItems.NETHERITE_SPEAR.get();

        
        boolean isLiving = event.getTarget() instanceof LivingEntity;

        
        if (isSpear && isLiving) {
            // Player Animator is optional; the compat class is the only place that touches its API.
            if (org.tdddd.epca.impl.compat.PlayerAnimSupport.available()) {
                org.tdddd.epca.impl.compat.PlayerAnimCompat.play(player, org.tdddd.epca.impl.compat.PlayerAnimCompat.STAB);
            }
        }
    }
}