package org.tdddd.yawning_neko_api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.tdddd.yawning_neko_api.data.EntityImmunityEffectManager;

/**
 * Denies mob effects listed in {@code entity_immunity_effects/*.json} for the target entity.
 */
public final class EffectImmunityHandler {
    private EffectImmunityHandler() {
    }

    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide) {
            MobEffectInstance effectInstance = event.getEffectInstance();
            if (effectInstance != null) {
                ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect().value());
                if (effectId != null && EntityImmunityEffectManager.isImmune(entity, effectId.toString())
                        || (effectId != null && EntityImmunityEffectManager.isImmune(entity,
                                org.tdddd.epca.impl.compat.EffectCompat.immunityAlias(effectId.toString())))) {
                    event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                }
            }
        }
    }
}
