package org.tdddd.epca.impl.overworld.registry.effects.buff;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import org.tdddd.epca.impl.overworld.registry.ModEffects;
import org.tdddd.epca.impl.overworld.registry.effects.RemovableEffect;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RageEffect extends MobEffect implements RemovableEffect {
    private static final String ATTACK_DAMAGE_UUID = "0a1b2c3d-4e5f-6789-abcd-ef0123456789";
    private static final String MOVEMENT_SPEED_UUID = "12345678-9abc-def0-1234-56789abcdef0";

    public RageEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x000000);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(org.tdddd.epca.impl.epca.MODID, "rage_attack_damage"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(org.tdddd.epca.impl.epca.MODID, "rage_movement_speed"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
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