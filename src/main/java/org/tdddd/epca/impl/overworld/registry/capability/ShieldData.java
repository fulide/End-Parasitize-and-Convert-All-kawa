package org.tdddd.epca.impl.overworld.registry.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Shield pool state, stored as the {@link ModAttachments#SHIELD} data attachment.
 *
 * <p>Replaces the 1.20.1 {@code ShieldCapability} capability provider. Access it with
 * {@code entity.getData(ModAttachments.SHIELD)} (or {@link #get(LivingEntity)}); the events that used to attach and
 * clear the capability now simply read/write this attachment.</p>
 *
 * <p>NBT layout: {@code {Shield: float}}.</p>
 */
public class ShieldData implements IShieldCapability, INBTSerializable<CompoundTag> {

    public static final String NBT_SHIELD = "Shield";

    private float shield = 0.0f;

    public ShieldData() {
    }

    /** Returns the attachment, creating the default value if the entity does not have one yet. */
    public static ShieldData get(LivingEntity entity) {
        return entity.getData(ModAttachments.SHIELD);
    }

    @Override
    public float getShield() {
        return shield;
    }

    @Override
    public void setShield(float shield) {
        this.shield = Math.max(0, shield);
    }

    @Override
    public void addShield(float amount) {
        this.shield += amount;
        if (this.shield < 0) this.shield = 0;
    }

    @Override
    public void consumeShield(float amount) {
        this.shield = Math.max(0, this.shield - amount);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat(NBT_SHIELD, shield);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.shield = tag.getFloat(NBT_SHIELD);
    }

    /** Legacy helper kept from the capability provider (the attachment serializer uses the provider overload). */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat(NBT_SHIELD, shield);
        return tag;
    }

    /** Legacy helper kept from the capability provider. */
    public void deserializeNBT(CompoundTag tag) {
        this.shield = tag.getFloat(NBT_SHIELD);
    }
}
