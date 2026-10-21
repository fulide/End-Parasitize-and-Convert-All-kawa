package org.tdddd.yawning_neko_api.events;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.tdddd.yawning_neko_api.data.EntityAttackEffectManager;
import org.tdddd.yawning_neko_api.data.EntityUpgradeManager;

/**
 * Applies the on-attack mob effects configured in {@code entity_attack_effects/*.json}.
 */
public class EntityAttackEffectHandler {
    private static final Random RANDOM = new Random();

    public EntityAttackEffectHandler() {
    }

    public void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            EntityUpgradeManager.setupEntityUpgradeLevel(mob);
        }
    }

    /**
     * 1.20.1 used {@code LivingDamageEvent}; in 1.21.1 the equivalent stage (after reductions, before
     * health is changed) is {@link LivingDamageEvent.Pre}.
     */
    public void onEntityAttack(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof Mob attacker) {
            LivingEntity target = event.getEntity();
            int currentLevel = EntityUpgradeManager.getEntityUpgradeLevel(attacker);
            List<EntityAttackEffectManager.AttackEffect> effects =
                    EntityAttackEffectManager.getAttackEffects(attacker, currentLevel);
            boolean upgraded = this.attemptPreAttackLevelUp(attacker, effects);
            if (upgraded) {
                int newLevel = EntityUpgradeManager.getEntityUpgradeLevel(attacker);
                effects = EntityAttackEffectManager.getAttackEffects(attacker, newLevel);
            }

            for (EntityAttackEffectManager.AttackEffect effect : effects) {
                float roll = RANDOM.nextFloat();
                if (roll <= effect.getProbability()) {
                    this.applyEffect(target, effect);
                }
            }
        }
    }

    private void applyEffect(LivingEntity target, EntityAttackEffectManager.AttackEffect effectConfig) {
        // epca:viral / epca:bleeding may be substituted by another mod's effect at runtime
        ResourceLocation effectId = ResourceLocation.tryParse(
                org.tdddd.epca.impl.compat.EffectCompat.mapDataEffectId(effectConfig.getEffectId()));
        if (effectId == null) {
            return;
        }
        Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
        if (holder.isEmpty()) {
            return;
        }

        int duration = effectConfig.getDuration();
        if (duration == -1) {
            duration = Integer.MAX_VALUE;
        }

        MobEffectInstance effectInstance = new MobEffectInstance(
                holder.get(),
                duration,
                effectConfig.getAmplifier(),
                effectConfig.isAmbient(),
                effectConfig.hasVisibleParticles(),
                effectConfig.shouldShowIcon());
        target.addEffect(effectInstance);
    }

    private boolean attemptPreAttackLevelUp(
            Mob attacker, List<EntityAttackEffectManager.AttackEffect> effects) {
        boolean upgraded = false;
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType());
        if (entityId == null) {
            return false;
        }
        EntityUpgradeManager.UpgradeConfig config = EntityUpgradeManager.getUpgradeConfig(entityId.toString());
        if (config == null) {
            return false;
        } else {
            for (EntityAttackEffectManager.AttackEffect effect : effects) {
                String effectId = effect.getEffectId();
                if (config.levelUpChances.containsKey(effectId)) {
                    float upgradeChance = config.levelUpChances.get(effectId);
                    float roll = RANDOM.nextFloat();
                    if (roll <= upgradeChance) {
                        int currentEffectLevel = EntityUpgradeManager.getEffectSpecificLevel(attacker, effectId);
                        int maxEffectLevel = EntityAttackEffectManager.getMaxEffectLevel(attacker, effectId);
                        if (currentEffectLevel < maxEffectLevel) {
                            EntityUpgradeManager.increaseEffectSpecificLevel(attacker, effectId, maxEffectLevel);
                            upgraded = true;
                        }
                    }
                }
            }

            return upgraded;
        }
    }
}
