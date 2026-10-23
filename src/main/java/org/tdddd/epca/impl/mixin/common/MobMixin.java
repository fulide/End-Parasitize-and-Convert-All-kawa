package org.tdddd.epca.impl.mixin.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tdddd.epca.impl.overworld.data.EvolutionManager;
import org.tdddd.epca.impl.overworld.data.SafetyDaySavedData;
import org.tdddd.epca.impl.overworld.registry.entities.IParasite;
import org.tdddd.epca.impl.events.NaturalSpawnProtection;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void onCheckDespawn(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        if (!(mob.level() instanceof ServerLevel level)) return;

        // Peaceful must win: vanilla Mob#checkDespawn discards hostile mobs there, and our
        // "do not despawn" protections must not keep them alive (reported as "mobs still spawn and
        // keep aggro in peaceful, they just cannot deal damage").
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return;
        }

        
        if (mob instanceof IParasite) {
            
            SafetyDaySavedData data = SafetyDaySavedData.get(level);
            long currentTick = level.getGameTime();
            if (data.isSafetyDayActive(currentTick)) {
                
                if (NaturalSpawnProtection.isNaturallySpawned(mob)) {
                    mob.discard();
                    ci.cancel();
                    return;
                }
            }
        }

        
        boolean canDespawn = NaturalSpawnProtection.canBeNaturallyDespawnedCached(mob);
        if (!canDespawn) {
            // Let vanilla distance-despawn non-persistent parasites, otherwise a swarm that walked
            // away from the player can only ever grow (and keeps ticking forever).
            if (!mob.isPersistenceRequired() && !mob.requiresCustomPersistence()) {
                net.minecraft.world.entity.player.Player nearest =
                        level.getNearestPlayer(mob, -1.0D);
                double distance = org.tdddd.epca.impl.ModConfig.getDespawnDistanceBlocks();
                if (nearest == null || mob.distanceToSqr(nearest) > distance * distance) {
                    return;     // fall through to the vanilla distance despawn
                }
            }
            mob.setNoActionTime(0);
            ci.cancel();
        }
    }

    @Inject(
            method = "finalizeSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onFinalizeSpawn(
            ServerLevelAccessor levelAccessor,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
            return;
        }

        int stage = EvolutionManager.getStageForDimension(levelAccessor.getLevel());
        if (stage != 13) {
            return;
        }

        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof IParasite)) {
            mob.discard();
            cir.setReturnValue(null);
        }
    }
}