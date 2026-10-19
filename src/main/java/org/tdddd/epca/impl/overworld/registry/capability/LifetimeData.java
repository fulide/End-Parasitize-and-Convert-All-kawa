package org.tdddd.epca.impl.overworld.registry.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

/**
 * Lifetime countdown state, stored as the {@link ModAttachments#LIFETIME} data attachment.
 *
 * <p>Replaces the 1.20.1 {@code LifetimeCapability} capability provider. Access it with
 * {@code entity.getData(ModAttachments.LIFETIME)} (or {@link #get(LivingEntity)}, which does the same); do
 * <b>not</b> construct it per entity yourself.</p>
 *
 * <p>NBT layout is identical to the old capability: {@code {RemainingTicks: int}}.</p>
 */
public class LifetimeData implements ILifetimeCapability, INBTSerializable<CompoundTag> {

    /** NBT key of the old capability — kept so existing worlds keep their countdowns. */
    public static final String NBT_REMAINING_TICKS = "RemainingTicks";

    /** {@code -1} means "no countdown" (the old default). */
    private int remainingTicks = -1;

    /**
     * Owner reference used only by the legacy constructor / no-arg {@link #tick()}. Attachment-created instances get
     * the entity passed explicitly to {@link #tick(LivingEntity)}.
     */
    @Nullable
    private transient LivingEntity owner;

    public LifetimeData() {
    }

    /** Legacy constructor kept for source compatibility with the old capability provider. */
    public LifetimeData(@Nullable LivingEntity owner) {
        this.owner = owner;
    }

    /** Returns the attachment, creating the default value if the entity does not have one yet. */
    public static LifetimeData get(LivingEntity entity) {
        return entity.getData(ModAttachments.LIFETIME);
    }

    /** Convenience for `entity.getData(ModAttachments.LIFETIME).setRemainingTicks(ticks)`. */
    public static void setRemainingTicks(LivingEntity entity, int ticks) {
        get(entity).setRemainingTicks(ticks);
    }

    @Override
    public void setRemainingTicks(int ticks) {
        this.remainingTicks = ticks;
    }

    @Override
    public int getRemainingTicks() {
        return remainingTicks;
    }

    /**
     * Ticks the countdown for the given entity and discards it when the counter reaches zero (server side only), which
     * is exactly what the old capability's static tick listener did.
     */
    public void tick(LivingEntity entity) {
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks == 0 && !entity.level().isClientSide) {
                entity.discard();
            }
        }
    }

    /**
     * Legacy no-arg tick: counts down but can only discard when this instance was created with
     * {@link #LifetimeData(LivingEntity)}. New code should call {@link #tick(LivingEntity)}.
     */
    @Override
    public void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks == 0 && owner != null && !owner.level().isClientSide) {
                owner.discard();
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_REMAINING_TICKS, remainingTicks);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.remainingTicks = tag.getInt(NBT_REMAINING_TICKS);
    }
}
