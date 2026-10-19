package org.tdddd.yawning_neko_api.data;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Default {@link IAdaptationData} implementation, stored as a NeoForge data attachment on the
 * entity (replacing the old {@code ICapabilitySerializable} provider).
 */
public class AdaptationDataCapability implements IAdaptationData {
    private final Map<String, Integer> adaptations = new HashMap<>();
    private int deathCount = 0;
    private String currentConfigId = "";
    private long brokenAdaptationEndTick = 0L;
    private int maxAdaptations = 0;
    private String lastDamagedType = null;
    private long spawnTime = 0L;
    private long lastHurtTime = 0L;

    public AdaptationDataCapability() {
    }

    @Override
    public void recordAdaptation(String damageType) {
        this.adaptations.put(damageType, this.adaptations.getOrDefault(damageType, 0) + 1);
        this.lastDamagedType = damageType;
    }

    @Override
    public String getLastDamagedType() {
        return this.lastDamagedType;
    }

    @Override
    public void setLastDamagedType(String damageType) {
        this.lastDamagedType = damageType;
    }

    @Override
    public int getMaxAdaptations() {
        return this.maxAdaptations;
    }

    @Override
    public void setMaxAdaptations(int max) {
        this.maxAdaptations = max;
    }

    @Override
    public int getAdaptationLevel(String damageTypeKey) {
        return this.adaptations.getOrDefault(damageTypeKey, 0);
    }

    @Override
    public void setAdaptationLevel(String damageTypeKey, int level) {
        if (level <= 0) {
            this.adaptations.remove(damageTypeKey);
        } else {
            this.adaptations.put(damageTypeKey, level);
        }
    }

    @Override
    public Map<String, Integer> getAllAdaptations() {
        return new HashMap<>(this.adaptations);
    }

    @Override
    public void clearAdaptations() {
        this.adaptations.clear();
    }

    @Override
    public int getDeathCount() {
        return this.deathCount;
    }

    @Override
    public void setDeathCount(int count) {
        this.deathCount = count;
    }

    @Override
    public void incrementDeathCount() {
        this.deathCount++;
    }

    @Override
    public String getCurrentConfigId() {
        return this.currentConfigId;
    }

    @Override
    public void setCurrentConfigId(String configId) {
        this.currentConfigId = configId;
    }

    @Override
    public long getBrokenAdaptationEndTick() {
        return this.brokenAdaptationEndTick;
    }

    @Override
    public void setBrokenAdaptationEndTick(long tick) {
        this.brokenAdaptationEndTick = tick;
    }

    @Override
    public boolean isInBrokenAdaptation(long currentTick) {
        return currentTick < this.brokenAdaptationEndTick;
    }

    @Override
    public long getSpawnTime() {
        return this.spawnTime;
    }

    @Override
    public void setSpawnTime(long tick) {
        this.spawnTime = tick;
    }

    @Override
    public long getLastHurtTime() {
        return this.lastHurtTime;
    }

    @Override
    public void setLastHurtTime(long tick) {
        this.lastHurtTime = tick;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag adaptTag = new CompoundTag();
        this.adaptations.forEach(adaptTag::putInt);
        tag.put("adaptations", adaptTag);
        tag.putInt("deathCount", this.deathCount);
        tag.putString("configId", this.currentConfigId);
        tag.putLong("brokenEnd", this.brokenAdaptationEndTick);
        tag.putInt("maxAdaptations", this.maxAdaptations);
        if (this.lastDamagedType != null) {
            tag.putString("lastDamageType", this.lastDamagedType);
        }

        // Original code normalised lastDamagedType from the freshly written tag.
        if (tag.contains("lastDamageType")) {
            this.lastDamagedType = tag.getString("lastDamageType");
        } else {
            this.lastDamagedType = null;
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.adaptations.clear();
        CompoundTag adaptTag = tag.getCompound("adaptations");
        for (String key : adaptTag.getAllKeys()) {
            this.adaptations.put(key, adaptTag.getInt(key));
        }

        this.deathCount = tag.getInt("deathCount");
        this.currentConfigId = tag.getString("configId");
        this.brokenAdaptationEndTick = tag.getLong("brokenEnd");
        this.maxAdaptations = tag.getInt("maxAdaptations");
        this.spawnTime = 0L;
        this.lastHurtTime = 0L;
    }
}
