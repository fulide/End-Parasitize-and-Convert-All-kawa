package org.tdddd.epca.impl.overworld.registry.entities.entity.misc;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.tdddd.epca.impl.overworld.registry.ModEffects;
import org.tdddd.epca.impl.overworld.registry.entities.IParasite;
import org.tdddd.yawning_neko_api.damages.ModDamageTypes;

import java.util.List;
import java.util.UUID;

public class ContaminatedWater extends Entity {

    /** Radius added to the (un-inflated) collision box when looking for damage targets. */
    public static final double DAMAGE_RADIUS = 2.5D;

    /**
     * Natural end of life of this entity, in ticks: 200 ticks = 10 seconds.
     *
     * <p>This is the single, explicit removal rule of the entity. It replaces the old bare
     * {@code lifeTicks >= 200} literal, so the number is named and can be quoted anywhere.</p>
     */
    public static final int MAX_LIFETIME_TICKS = 200;

    /**
     * NBT key of {@link #lifeTicks}. The counter is persisted like a vanilla item's {@code Age}, so a
     * chunk unload or a world reload cannot restart the 10 second clock and keep the entity alive
     * forever.
     */
    private static final String TAG_LIFE_TICKS = "LifeTicks";

    private int lifeTicks = 0; 
    private final UUID entityId;

    
    private static final EntityDataAccessor<Boolean> DUMMY = SynchedEntityData.defineId(ContaminatedWater.class, EntityDataSerializers.BOOLEAN);

    public ContaminatedWater(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.entityId = this.getUUID();
        
        // NOTE: do NOT inflate the collision box here. Entity.baseTick() scans *every block in the
        // bounding box* for fluids (twice: water and lava); this entity's base box is already 5x5,
        // so inflating by 2.5 turned it into 10x10x10 (~1000 fluid lookups per tick per entity on
        // BOTH sides). That is what made dimension changes drop to 1 FPS. The damage radius is
        // applied to the query in tick() instead (see DAMAGE_RADIUS).
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        
        builder.define(DUMMY, false);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide) {
            // The client copy has no logic of its own: it only needs the lifetime counter so the
            // effect disappears even when the server-side chunk is loaded but not entity-ticking.
            // Skipping super.tick() avoids Entity.baseTick()'s fluid scan on the render thread
            // (the renderer reads GasCloudManager's own client tick counter, not the entity's).
            lifeTicks++;
            if (lifeTicks >= MAX_LIFETIME_TICKS) {
                this.discard();
            }
            return;
        }

        super.tick();
        lifeTicks++;

        
        if (lifeTicks >= MAX_LIFETIME_TICKS) {
            // Natural end of life. The discard is deliberately run on BOTH sides: the server copy is
            // the authoritative one (it is the one that damages and that unloads with the world), but
            // the client copy is the one that is actually rendered - and it is also the only copy
            // that still ticks when the server-side chunk is loaded but not entity-ticking (render
            // distance > simulation distance), where the server copy would freeze mid-life and the
            // effect would stay on screen forever. Removing the client copy here is always safe: once
            // the server does remove its own copy, the removal packet is a no-op on the client.
            this.discard();
            return;
        }

        if (!this.level().isClientSide && lifeTicks % 10 == 0) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            DamageSources damageSources = serverLevel.damageSources();

            List<Entity> entities = serverLevel.getEntities(
                    this,
                    this.getBoundingBox().inflate(DAMAGE_RADIUS),
                    entity -> entity instanceof LivingEntity
            );

            for (Entity entity : entities) {
                LivingEntity livingEntity = (LivingEntity) entity;
                
                if (!IParasite.isParasiteByTagOrInterface(livingEntity) && livingEntity.isInWater()) {
                    applyDamageWithMinMechanic(livingEntity, damageSources);
                }
            }
        }
    }

    
    private void applyDamageWithMinMechanic(LivingEntity target, DamageSources damageSources) {
        Level level = target.level();
        Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        Holder<DamageType> holder = registry.getHolderOrThrow(ModDamageTypes.MINIMUM);
        DamageSource minimumSource = new DamageSource(holder);
        target.hurt(minimumSource, 0.1F);
        addCothEffect(target);
        
    }


    
    private void addCothEffect(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(
                ModEffects.COTH,
                400,  
                0,    
                false,
                true,
                true
        ));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        // Restore the remaining lifetime instead of restarting it at zero.
        this.lifeTicks = Math.max(0, compoundTag.getInt(TAG_LIFE_TICKS));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt(TAG_LIFE_TICKS, this.lifeTicks);
    }

    
    @Override
    public boolean isAttackable() {
        return false;
    }
}