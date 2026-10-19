package org.tdddd.epca.impl.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.minecraft.core.registries.BuiltInRegistries;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModEffects;
import org.tdddd.epca.impl.overworld.registry.effects.debuff.FearEffect;

import java.util.Set;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID)
public class FearEffectEventHandler {
    private static final Set<ResourceLocation> THROWABLE_ITEMS = Set.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "snowball"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "ender_pearl"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "egg"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "experience_bottle"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "splash_potion"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "lingering_potion")
    );

    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        
        if (!player.hasEffect(ModEffects.FEAR)) return;

        int amplifier = player.getEffect(ModEffects.FEAR).getAmplifier();
        if (FearEffect.shouldPreventBlockPlacement(amplifier)) {
            player.displayClientMessage(FearEffect.FEAR_MESSAGE, true);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        
        if (!player.hasEffect(ModEffects.FEAR)) return;

        
        ItemStack stack = player.getItemInHand(event.getHand());
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        
        if (itemId != null && (THROWABLE_ITEMS.contains(itemId) || stack.getItem() instanceof ProjectileWeaponItem)) {
            return;
        }

        int amplifier = player.getEffect(ModEffects.FEAR).getAmplifier();
        if (FearEffect.shouldPreventItemUse(amplifier)) {
            player.displayClientMessage(FearEffect.FEAR_MESSAGE, true);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}