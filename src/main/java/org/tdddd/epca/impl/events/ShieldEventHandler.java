package org.tdddd.epca.impl.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.overworld.registry.effects.buff.SoulProtectionEffect;
import org.tdddd.epca.impl.utils.ShieldProtectionHelper;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class ShieldEventHandler {

    /**
     *
     *
     * <p> {@link LivingDamageEvent} 1.20.1 Forge  {@code LivingEntity#actuallyHurt}
     * {@code _tmp_vanilla_src/net/minecraft/world/entity/LivingEntity.java:1632}
     * {@code Player#actuallyHurt}{@code .../player/Player.java:915}//
     *  {@code setHealth} = {@code (amplifier + 1) * 0.5}
     *  I = 0.5 II = 1.0 III = 1.5
     * {@link ShieldProtectionHelper#ignoredDamage(int)}  {@link ShieldProtectionHelper#IGNORE_PER_LEVEL}</p>
     *
     * <p><b></b> {@code mixin/common/LivingEntityMixin#onSetHealth}
     *  {@link ShieldProtectionHelper#applyShieldPool}  {@code setHealth}
     * </p>
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        float original = event.getNewDamage();
        if (original <= 0) return;

        float remaining = ShieldProtectionHelper.applyDamageIgnore(entity, original);
        if (remaining <= 0) {
            event.setNewDamage(0.0F);
        } else {
            event.setNewDamage(remaining);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;
        if (!(instance.getEffect().value() instanceof SoulProtectionEffect)) return;

        //  Added ShieldProtectionHelper.applyShieldPool
        // syncShieldToDuration entity.addEffect()
        //   Forge  LivingEntity#addEffect  MobEffectEvent.Added
        // _tmp_vanilla_src/net/minecraft/world/entity/LivingEntity.java:924-940  929
        //    +  1.0
        // ATTACK_DAMAGE
        if (event.getOldEffectInstance() != null
                && event.getOldEffectInstance().getAmplifier() == instance.getAmplifier()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        float totalShield = computeShieldForEntity(entity, instance.getAmplifier());
        ShieldCapabilityHandler.get(entity).setShield(totalShield);
    }

    
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;
        if (!(instance.getEffect().value() instanceof SoulProtectionEffect)) return;

        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        ShieldCapabilityHandler.get(entity).setShield(0);
    }

    
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;
        if (!(instance.getEffect().value() instanceof SoulProtectionEffect)) return;

        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        ShieldCapabilityHandler.get(entity).setShield(0);
    }

    
    private static float computeShieldForEntity(LivingEntity entity, int amplifier) {
        float baseShield = (amplifier + 1) * 0.5f;
        float maxHealth = entity.getMaxHealth();
        float extraShield = 0f;
        if (maxHealth > 25f) {
            int extraSteps = (int) ((maxHealth - 25f) / 50f);
            extraShield = extraSteps * 0.5f;
        }
        return baseShield + (extraShield * (amplifier + 1));
    }
}