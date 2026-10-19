package org.tdddd.epca.impl.overworld.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class EvolutionDataStorage extends SavedData {
    private static final String DATA_NAME = "EvolutionData";

    
    private final Map<ResourceKey<Level>, Integer> dimensionPoints = new ConcurrentHashMap<>();
    
    private final Map<ResourceKey<Level>, Long> dimensionCooldowns = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Integer> overriddenStages = new ConcurrentHashMap<>();

    public EvolutionDataStorage() {
        
        dimensionPoints.put(Level.OVERWORLD, 0);
        dimensionPoints.put(Level.NETHER, -50);
        dimensionPoints.put(Level.END, -50);
        
        ResourceKey<Level> twilightForest = ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.parse("dimension")),
                ResourceLocation.parse("twilightforest:twilight_forest"));
        dimensionPoints.put(twilightForest, -50);
    }

    public EvolutionDataStorage(CompoundTag nbt) {
        
        ListTag pointsList = nbt.getList("Dimensions", Tag.TAG_COMPOUND);
        for (int i = 0; i < pointsList.size(); i++) {
            CompoundTag tag = pointsList.getCompound(i);
            ResourceKey<Level> dim = createDimensionKey(tag.getString("Dimension"));
            int points = tag.getInt("Points");
            dimensionPoints.put(dim, points);
        }

        
        if (nbt.contains("Cooldowns", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("Cooldowns", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                ResourceKey<Level> dim = createDimensionKey(tag.getString("Dimension"));
                long end = tag.getLong("CooldownEnd");
                dimensionCooldowns.put(dim, end);
            }
        }
        if (nbt.contains("OverriddenStages", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("OverriddenStages", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                ResourceKey<Level> dim = createDimensionKey(tag.getString("Dimension"));
                int stage = tag.getInt("Stage");
                overriddenStages.put(dim, stage);
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider registries) {
        
        ListTag pointsList = new ListTag();
        dimensionPoints.forEach((dim, points) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", dim.location().toString());
            tag.putInt("Points", points);
            pointsList.add(tag);
        });
        nbt.put("Dimensions", pointsList);

        
        ListTag cooldownList = new ListTag();
        dimensionCooldowns.forEach((dim, end) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", dim.location().toString());
            tag.putLong("CooldownEnd", end);
            cooldownList.add(tag);
        });
        nbt.put("Cooldowns", cooldownList);

        ListTag stageList = new ListTag();
        overriddenStages.forEach((dim, stage) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", dim.location().toString());
            tag.putInt("Stage", stage);
            stageList.add(tag);
        });
        nbt.put("OverriddenStages", stageList);
        return nbt;
    }

    public static EvolutionDataStorage get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        // 1.21: computeIfAbsent takes a SavedData.Factory<T> and the deserializer is a
        // BiFunction<CompoundTag, HolderLookup.Provider, T>.
        return storage.computeIfAbsent(
                new SavedData.Factory<>(EvolutionDataStorage::new,
                        (tag, registries) -> new EvolutionDataStorage(tag)),
                DATA_NAME
        );
    }

    
    public int getPointsForDimension(ResourceKey<Level> dimension) {
        return dimensionPoints.getOrDefault(dimension, 0);
    }

    public void setPointsForDimension(ResourceKey<Level> dimension, int points) {
        dimensionPoints.put(dimension, points);
        setDirty();
    }

    
    public long getCooldownEndForDimension(ResourceKey<Level> dimension) {
        return dimensionCooldowns.getOrDefault(dimension, 0L);
    }

    public void setCooldownEndForDimension(ResourceKey<Level> dimension, long endTick) {
        dimensionCooldowns.put(dimension, endTick);
        setDirty();
    }

    
    public Integer getOverriddenStage(ResourceKey<Level> dimension) {
        return overriddenStages.get(dimension);
    }

    public void setOverriddenStage(ResourceKey<Level> dimension, int stage) {
        overriddenStages.put(dimension, stage);
        setDirty();
    }

    public void clearOverriddenStage(ResourceKey<Level> dimension) {
        overriddenStages.remove(dimension);
        setDirty();
    }

    public Set<ResourceKey<Level>> getOverriddenDimensions() {
        return Collections.unmodifiableSet(overriddenStages.keySet());
    }

    
    public void resetDimension(ResourceKey<Level> dimension) {
        if (dimension.equals(Level.OVERWORLD)) setPointsForDimension(dimension, 0);
        else if (dimension.equals(Level.NETHER) || dimension.equals(Level.END))
            setPointsForDimension(dimension, -50);
        else setPointsForDimension(dimension, 0);
        clearOverriddenStage(dimension); 
    }

    public void resetAllDimensions() {
        resetDimension(Level.OVERWORLD);
        resetDimension(Level.NETHER);
        resetDimension(Level.END);
        for (ResourceKey<Level> dim : dimensionPoints.keySet()) {
            if (!dim.equals(Level.OVERWORLD) && !dim.equals(Level.NETHER) && !dim.equals(Level.END)) {
                resetDimension(dim);
            }
        }
    }

    private ResourceKey<Level> createDimensionKey(String location) {
        return ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.parse("dimension")),
                ResourceLocation.parse(location));
    }
}