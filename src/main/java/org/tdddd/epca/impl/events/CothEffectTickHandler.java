package org.tdddd.epca.impl.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.overworld.registry.ModEffects;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class CothEffectTickHandler {
    private static final List<EffectRemovalRequest> removalRequests = new ArrayList<>();

    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        
        if (!removalRequests.isEmpty()) {
            List<EffectRemovalRequest> toRemove = new ArrayList<>(removalRequests);
            removalRequests.clear();

            for (EffectRemovalRequest request : toRemove) {
                request.process();
            }
        }
    }

    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        LivingEntity player = event.getEntity();

        
        if (player.level().isClientSide) return;

        
        if (player.getPersistentData().getBoolean("ShouldRemoveCothEffect")) {
            player.getPersistentData().remove("ShouldRemoveCothEffect");
            int amplifierToRemove = player.getPersistentData().getInt("CothEffectToRemoveAmplifier");
            player.getPersistentData().remove("CothEffectToRemoveAmplifier");

            MobEffectInstance cothEffect = player.getEffect(ModEffects.COTH);

            
            if (cothEffect != null && cothEffect.getAmplifier() == amplifierToRemove) {
                player.removeEffect(ModEffects.COTH);

                
                
            }
        }

        
        checkAndFixEffectLevels(player);
    }

    
    private static void checkAndFixEffectLevels(LivingEntity entity) {
        MobEffectInstance currentCoth = entity.getEffect(ModEffects.COTH);
        if (currentCoth == null) return;

        
        
    }

    
    public static void scheduleEffectRemoval(LivingEntity entity, int amplifier) {
        removalRequests.add(new EffectRemovalRequest(entity, amplifier));
    }

    
    private static class EffectRemovalRequest {
        private final LivingEntity entity;
        private final int amplifier;

        public EffectRemovalRequest(LivingEntity entity, int amplifier) {
            this.entity = entity;
            this.amplifier = amplifier;
        }

        public void process() {
            if (!entity.isAlive()) return;

            MobEffectInstance cothEffect = entity.getEffect(ModEffects.COTH);
            
            if (cothEffect != null && cothEffect.getAmplifier() == amplifier) {
                entity.removeEffect(ModEffects.COTH);
            }
        }
    }
}