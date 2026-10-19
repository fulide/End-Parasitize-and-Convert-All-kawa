package org.tdddd.epca.impl.overworld.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashSet;
import java.util.Set;

public class InfestedBlockSavedData extends SavedData {
    private static final String DATA_NAME = "epca_infested_blocks";
    private static final String TAG_BLOCKS = "blocks";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";
    private final Set<BlockPos> infestedBlocks = new HashSet<>();

    public static InfestedBlockSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        // 1.21: SavedData.Factory + Provider-aware deserializer.
        return storage.computeIfAbsent(
                new SavedData.Factory<>(InfestedBlockSavedData::new,
                        (tag, registries) -> {
                            InfestedBlockSavedData data = new InfestedBlockSavedData();
                            data.load(tag);
                            return data;
                        }),
                DATA_NAME
        );
    }

    public void add(BlockPos pos) {
        if (infestedBlocks.add(pos.immutable())) {
            setDirty();
        }
    }

    public void remove(BlockPos pos) {
        if (infestedBlocks.remove(pos.immutable())) {
            setDirty();
        }
    }

    public Set<BlockPos> getAll() {
        return new HashSet<>(infestedBlocks);
    }

    public boolean contains(BlockPos pos) {
        return infestedBlocks.contains(pos);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BlockPos pos : infestedBlocks) {
            // NbtUtils.writeBlockPos changed to an int-array tag in 1.21; keep the 1.20.1
            // {X,Y,Z} compound layout so existing saves keep loading.
            CompoundTag one = new CompoundTag();
            one.putInt(TAG_X, pos.getX());
            one.putInt(TAG_Y, pos.getY());
            one.putInt(TAG_Z, pos.getZ());
            list.add(one);
        }
        tag.put(TAG_BLOCKS, list);
        return tag;
    }

    public void load(CompoundTag tag) {
        infestedBlocks.clear();
        ListTag list = tag.getList(TAG_BLOCKS, net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag one = list.getCompound(i);
            infestedBlocks.add(new BlockPos(one.getInt(TAG_X), one.getInt(TAG_Y), one.getInt(TAG_Z)));
        }
    }
}