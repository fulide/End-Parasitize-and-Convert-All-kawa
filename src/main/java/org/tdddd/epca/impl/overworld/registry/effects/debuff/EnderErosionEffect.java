package org.tdddd.epca.impl.overworld.registry.effects.debuff;

import org.tdddd.epca.impl.overworld.registry.ModEffects;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.tdddd.epca.impl.overworld.registry.effects.RemovableEffect;
import org.tdddd.yawning_neko_api.damages.ModDamageTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.neoforged.neoforge.common.NeoForge;

public class EnderErosionEffect extends MobEffect implements RemovableEffect {

    
    private static final Map<UUID, Float> erosionValues = new HashMap<>();
    
    private static final Map<UUID, Integer> erosionTimers = new HashMap<>();

    public EnderErosionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B008B); 
        
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isRemovable() {
        return false; 
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            
            UUID uuid = entity.getUUID();
            float currentErosion = erosionValues.getOrDefault(uuid, 0f);
            float newErosion = currentErosion + (amplifier + 1) * 0.5f; 
            erosionValues.put(uuid, newErosion);
        }
        super.applyEffectTick(entity, amplifier);
            return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        
        return duration % 10 == 0;
    }

    @SubscribeEvent
     public void onLivingTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide) {
            UUID uuid = event.getEntity().getUUID();
            if (event.getEntity().hasEffect(ModEffects.ENDER_EROSION)) {
                
                int timer = erosionTimers.getOrDefault(uuid, 0);
                timer++;
                if (timer >= 10) { 
                    timer = 0;
                }
                erosionTimers.put(uuid, timer);
            }
        }
    }

    @SubscribeEvent
     public void onEffectRemove(MobEffectEvent.Remove event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null && effectInstance.getEffect() == this && event.getEntity() != null) {
            LivingEntity entity = event.getEntity();
            UUID uuid = entity.getUUID();

            float erosion = erosionValues.getOrDefault(uuid, 0f);

            if (erosion > 0 && !entity.level().isClientSide) {
                float minimumDamage = erosion * 0.25f;
                
                Registry<DamageType> registry = entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE);
                Holder<DamageType> holder = registry.getHolderOrThrow(ModDamageTypes.MINIMUM);
                DamageSource minimumSource = new DamageSource(holder);
                entity.hurt(minimumSource, minimumDamage);

                
                float magicDamage = erosion * 0.75f;
                entity.hurt(entity.damageSources().magic(), magicDamage);
            }

            
            erosionValues.remove(uuid);
            erosionTimers.remove(uuid);
        }
    }

    @SubscribeEvent
     public void onEffectAdded(MobEffectEvent.Added event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null && effectInstance.getEffect() == this && event.getEntity() != null) {
            
            UUID uuid = event.getEntity().getUUID();
            erosionValues.put(uuid, 0f);
            erosionTimers.put(uuid, 0);
        }
    }

    @SubscribeEvent
     public void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null && effectInstance.getEffect() == this && event.getEntity() != null) {
            LivingEntity entity = event.getEntity();
            UUID uuid = entity.getUUID();

            float erosion = erosionValues.getOrDefault(uuid, 0f);

            if (erosion > 0 && !entity.level().isClientSide) {
                
                float setHealthDamage = erosion * 0.25f;
                float currentHealth = entity.getHealth();
                float newHealth = currentHealth - setHealthDamage;

                if (newHealth <= 0) {
                    entity.setHealth(0);
                    entity.die(entity.damageSources().magic());
                } else {
                    entity.setHealth(newHealth);
                }

                
                float magicDamage = erosion * 0.75f;
                entity.hurt(entity.damageSources().magic(), magicDamage);
            }

            
            erosionValues.remove(uuid);
            erosionTimers.remove(uuid);
        }
    }

    @SubscribeEvent
     public void onEntityDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity().hasEffect(ModEffects.ENDER_EROSION)) {
            
            UUID uuid = event.getEntity().getUUID();
            erosionValues.remove(uuid);
            erosionTimers.remove(uuid);
        }
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(attributeMap, amplifier);
        // 1.21.1 no longer passes the LivingEntity here; per-entity state is initialised lazily in
        // applyEffectTick (erosionValues/erosionTimers use getOrDefault) and cleared on death.
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
        
    }
}