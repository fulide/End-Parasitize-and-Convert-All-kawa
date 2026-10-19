package org.tdddd.epca.impl.overworld.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class SafetyDaySavedData extends SavedData {
    private static final String DATA_NAME = "epca_safety_day";
    public long safetyDayEndTick = -1;      
    private boolean hasTriggeredEffect = false; 

    public static SafetyDaySavedData get(Level level) {
        if (level.isClientSide) throw new IllegalStateException("Only server side");
        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SafetyDaySavedData::new, SafetyDaySavedData::load),
                DATA_NAME);
    }

    private static SafetyDaySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SafetyDaySavedData data = new SafetyDaySavedData();
        data.safetyDayEndTick = tag.getLong("EndTick");
        data.hasTriggeredEffect = tag.getBoolean("TriggeredEffect");
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        tag.putLong("EndTick", safetyDayEndTick);
        tag.putBoolean("TriggeredEffect", hasTriggeredEffect);
        return tag;
    }

    public void startSafetyDay(long endTick) {
        this.safetyDayEndTick = endTick;
        this.hasTriggeredEffect = false;
        setDirty();
    }

    public boolean isSafetyDayActive(long currentTick) {
        return safetyDayEndTick != -1 && currentTick < safetyDayEndTick;
    }

    public boolean isSafetyDayFinished(long currentTick) {
        return safetyDayEndTick != -1 && currentTick >= safetyDayEndTick;
    }

    public void markEffectTriggered() {
        this.hasTriggeredEffect = true;
        setDirty();
    }

    public boolean isEffectTriggered() {
        return hasTriggeredEffect;
    }

    public void reset() {  
        safetyDayEndTick = -1;
        hasTriggeredEffect = false;
        setDirty();
    }
}