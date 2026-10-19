package org.tdddd.epca.impl.overworld.registry.items.item;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.tdddd.epca.impl.overworld.registry.ModDataComponents;

public class LivingArmorAdaptation {
    private static final int MAX_ADAPTATIONS_PER_PIECE = 9; 
    private static final float ADAPTATION_REDUCTION_PER_STACK = 0.0125f; 
    private static final float MAX_REDUCTION = 0.45f; 

    public static float getLimitedDamage(Player player, DamageSource source, float damage) {
        
        if (isFireDamage(source)) {
            return damage;
        }

        float damageReduction = getDamageReduction(player);
        float minDamage = (100 * (1 - damageReduction)) + 1;
        float reducedDamage = damage * (1 - damageReduction);

        return Math.min(damage, Math.max(reducedDamage, minDamage));
    }

    
    public static void addAdaptation(ItemStack armorStack) {
        
        int currentCount = ModDataComponents.getInt(armorStack, ModDataComponents.ADAPTATION_COUNT);

        
        if (currentCount < MAX_ADAPTATIONS_PER_PIECE) {
            ModDataComponents.setInt(armorStack, ModDataComponents.ADAPTATION_COUNT, currentCount + 1);
        }
    }

    
    public static int getAdaptationCountForArmor(ItemStack armorStack) {
        return ModDataComponents.getInt(armorStack, ModDataComponents.ADAPTATION_COUNT);
    }

    
    public static int getTotalAdaptationCount(Player player) {
        int total = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof LivingArmorItem) {
                total += getAdaptationCountForArmor(armor);
            }
        }
        return total;
    }

    
    public static float getDamageReduction(Player player) {
        int adaptationCount = getTotalAdaptationCount(player);
        return Math.min(adaptationCount * ADAPTATION_REDUCTION_PER_STACK, MAX_REDUCTION);
    }

    
    public static int getLivingArmorCount(Player player) {
        int count = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof LivingArmorItem) {
                count++;
            }
        }
        return count;
    }

    
    public static boolean isFireDamage(DamageSource source) {
        
        return source.is(DamageTypeTags.IS_FIRE);
    }
}