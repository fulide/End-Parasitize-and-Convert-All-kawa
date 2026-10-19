package org.tdddd.epca.impl.overworld.registry.effects.debuff;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModEffects;
import org.tdddd.epca.impl.overworld.registry.effects.RemovableEffect;
import org.tdddd.epca.impl.overworld.registry.entities.IParasite;

import java.util.UUID;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID)
public class DeepSneakEffect extends MobEffect implements RemovableEffect{
    // 1.21.1: AttributeModifier is identified by a ResourceLocation, not a UUID.
    private static final net.minecraft.resources.ResourceLocation SPEED_MODIFIER_ID =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(epca.MODID, "deep_sneak_speed");
    private static final net.minecraft.resources.ResourceLocation DAMAGE_MODIFIER_ID =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(epca.MODID, "deep_sneak_damage");

    public DeepSneakEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x0000AA);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        
        boolean isParasite = IParasite.isParasiteByTagOrInterface(entity);

        if (isParasite) {
            if (entity.isOnFire() && !entity.isInLava()) {
                entity.clearFire();
            }
            removeSpeedModifier(entity);
            return true;
        }

        if (!hasLeatherArmor(entity)) {
            float damage = 0.025f * (amplifier + 1);
            if (entity.isAlive() && !entity.isInvulnerable()) {
                entity.hurt(entity.damageSources().freeze(), damage);
            }
        }

        if (!hasTurtleHelmet(entity)) {
            applySpeedModifier(entity, amplifier);
        } else {
            removeSpeedModifier(entity);
        }
            return true;
    }

    private static boolean hasLeatherArmor(LivingEntity entity) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getMaterial() == ArmorMaterials.LEATHER) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasTurtleHelmet(LivingEntity entity) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty() && helmet.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getMaterial() == ArmorMaterials.TURTLE;
        }
        return false;
    }

    private static void applySpeedModifier(LivingEntity entity, int amplifier) {
        AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance == null) return;

        
        instance.removeModifier(SPEED_MODIFIER_ID);

        double amount = -0.0125 * (amplifier + 1);
        AttributeModifier modifier = new AttributeModifier(
                SPEED_MODIFIER_ID,
                amount,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        instance.addPermanentModifier(modifier);
    }

    private static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    // 1.21.1 removed MobEffect#getAttributeModifierValue; the modifier amount is baked into the
    // AttributeTemplate created by applySpeedModifier.

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(ModEffects.DEEP_SNEAK) && !IParasite.isParasiteByTagOrInterface(attacker)) {
                if (!hasTurtleHelmet(attacker)) {
                    int amplifier = attacker.getEffect(ModEffects.DEEP_SNEAK).getAmplifier();
                    float damageReduction = 0.0125f * (amplifier + 1);
                    float newDamage = event.getAmount() * (1 - damageReduction);
                    event.setAmount(newDamage);
                }
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean isRemovable() {
        return false; 
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == ModEffects.DEEP_SNEAK) {
            LivingEntity entity = event.getEntity();
            if (entity != null) {
                removeSpeedModifier(entity);
            }
        }
    }
}