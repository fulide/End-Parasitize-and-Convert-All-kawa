package org.tdddd.epca.impl.overworld.registry.effects.debuff;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.tdddd.epca.impl.overworld.registry.effects.RemovableEffect;

public class CorrosiveEffect extends MobEffect implements RemovableEffect {
    public CorrosiveEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x0A5F20);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        
        int effectLevel = amplifier + 1;
        int damagePerSecond = effectLevel * 3; 

        
        if (entity.tickCount % 20 == 0) {
            
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                if (!slot.isArmor()) {
                    continue;
                }
                ItemStack armor = entity.getItemBySlot(slot);

                if (armor.isEmpty() || !armor.isDamageableItem()) {
                    continue;
                }

                
                if (entity.getRandom().nextFloat() < 1.0f) {
                    // 1.21.1: ItemStack#hurtAndBreak(int, LivingEntity, EquipmentSlot) replaces the
                    // old Consumer<LivingEntity> overload.
                    armor.hurtAndBreak(damagePerSecond, entity, slot);
                }
            }
        }
            return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        
        return true;
    }

    @Override
    public boolean isRemovable() {
        return false; 
    }
}