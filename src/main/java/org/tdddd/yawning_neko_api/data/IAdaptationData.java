package org.tdddd.yawning_neko_api.data;

import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.tdddd.yawning_neko_api.YawningNekoApi;

/**
 * Per-entity adaptation state. In 1.20.1 this was a Forge capability; in 1.21.1 it is a NeoForge
 * data attachment (see {@link YawningNekoApi#ADAPTATION_DATA}).
 *
 * <p>Use {@link #get(LivingEntity)} to obtain it. The default attachment value is created lazily
 * and is never {@code null}, matching the old {@code LazyOptional.of(...)} behaviour.</p>
 */
public interface IAdaptationData extends INBTSerializable<CompoundTag> {
    /**
     * Returns the adaptation data of an entity, creating the default value when absent.
     */
    static IAdaptationData get(LivingEntity entity) {
        return entity.getData(YawningNekoApi.ADAPTATION_DATA);
    }

    /**
     * Returns the adaptation data of an entity, or {@code null} when no data has been attached yet.
     * This mirrors the old {@code entity.getCapability(CAPABILITY).orElse(null)} call pattern.
     */
    static IAdaptationData getOrNull(LivingEntity entity) {
        return entity.getExistingDataOrNull(YawningNekoApi.ADAPTATION_DATA);
    }

    int getAdaptationLevel(String damageTypeKey);

    void setAdaptationLevel(String damageTypeKey, int level);

    Map<String, Integer> getAllAdaptations();

    void clearAdaptations();

    int getMaxAdaptations();

    void setMaxAdaptations(int max);

    void recordAdaptation(String damageType);

    String getLastDamagedType();

    void setLastDamagedType(String damageType);

    int getDeathCount();

    void setDeathCount(int count);

    void incrementDeathCount();

    String getCurrentConfigId();

    void setCurrentConfigId(String configId);

    long getBrokenAdaptationEndTick();

    void setBrokenAdaptationEndTick(long tick);

    boolean isInBrokenAdaptation(long currentTick);

    long getSpawnTime();

    void setSpawnTime(long tick);

    long getLastHurtTime();

    void setLastHurtTime(long tick);
}
