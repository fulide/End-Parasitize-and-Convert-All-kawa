package org.tdddd.epca.impl.overworld.registry.entities.entity.misc;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.tdddd.epca.impl.client.entity.IMotionAligned;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.tdddd.epca.impl.overworld.registry.ModEffects;
import org.tdddd.epca.impl.overworld.registry.entities.IParasite;
import org.tdddd.epca.impl.overworld.registry.ModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;

public class BoneArrow extends AbstractArrow implements GeoEntity, IMotionAligned {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int age = 0;
    private final int MAX_AGE = 30 * 20; 
    private static final Random RANDOM = new Random(); 

    public BoneArrow(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.setBaseDamage(8.0F); 
    }

    public BoneArrow(Level level, LivingEntity shooter) {
        super(ModEntities.BONE_ARROW.get(), shooter, level, EPCA_PICKUP_ICON.copy(), null);
        this.setNoGravity(false);
        this.setBaseDamage(8.0F); 
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age >= MAX_AGE && !this.level().isClientSide) {
            this.discard();
        }

        if (!this.inGround && age > 1) {
            Vec3 dm = this.getDeltaMovement();
            double horizontal = dm.horizontalDistance();
            if (dm.lengthSqr() > 1.0E-6D) {
                this.setYRot((float)(Mth.atan2(dm.x, dm.z) * (180F / Math.PI)));
                this.setXRot((float)(Mth.atan2(dm.y, horizontal) * (180F / Math.PI)));
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        
        if (this.level().isClientSide) {
            return;
        }

        
        LivingEntity shooter = this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null;
        var target = hitResult.getEntity();

        
        if (IParasite.isParasiteByTagOrInterface(shooter) && IParasite.isParasiteNoLivingByTagOrInterface(target)) {
            
            return;
        }

        
        boolean damageApplied = target.hurt(this.damageSources().arrow(this, shooter), 8.0F);

        
        if (damageApplied && target instanceof LivingEntity livingTarget) {
            applyCothEffect(livingTarget);

            if (target instanceof LivingEntity living && this.getPersistentData().getBoolean("InfestedFireArrow")) {
                living.setRemainingFireTicks(160);  
            }
        }

        
        this.discard();
    }

    
    private void applyCothEffect(LivingEntity target) {
        var existingEffect = target.getEffect(ModEffects.COTH);

        if (existingEffect != null) {
            
            int currentAmplifier = existingEffect.getAmplifier(); 
            int newAmplifier = currentAmplifier;
            if (RANDOM.nextFloat() < 0.7f) {
                
                newAmplifier = Math.min(currentAmplifier + 1, 2);
            }
            
            target.addEffect(new MobEffectInstance(ModEffects.COTH, 15 * 20, newAmplifier));
        } else {
            
            target.addEffect(new MobEffectInstance(ModEffects.COTH, 15 * 20, 0));
        }
    }

    /** 1.21's ItemStack#save throws "Cannot encode empty ItemStack", so the arrow always carries a
     *  non-empty pickup stack even though it is never handed to a player (pickup stays CREATIVE_ONLY).
     *  1.20.1 tolerated the empty stack. */
    private static final ItemStack EPCA_PICKUP_ICON = new ItemStack(net.minecraft.world.item.Items.ARROW);

    @Override
    protected ItemStack getDefaultPickupItem() {
        return EPCA_PICKUP_ICON.copy();
    }

    @Override
    protected ItemStack getPickupItem() {
        return EPCA_PICKUP_ICON.copy();
    }

    /** Belt and braces: an empty pickup stack must never reach the chunk serialiser. */
    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        if (this.getPickupItemStackOrigin().isEmpty()) {
            this.setPickupItemStack(EPCA_PICKUP_ICON.copy());
        }
        super.addAdditionalSaveData(tag);
    }


    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
