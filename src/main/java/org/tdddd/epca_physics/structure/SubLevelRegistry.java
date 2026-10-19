package org.tdddd.epca_physics.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.1 compatibility shim for {@code epca_physics}'s {@code SubLevelRegistry}.
 *
 * <p>The original registry owns the whole physicalized-block simulation: it
 * captures blocks out of the world, simulates rigid bodies, ticks behaviours,
 * forces chunks, mines blocks and synchronizes poses/contents to clients. None
 * of that exists on the 1.21.1 port, because the Forge-only
 * {@code epca_physics} 1.0.0 mod cannot be loaded.</p>
 *
 * <p>This shim provides the small, safe subset epca actually calls:</p>
 * <ul>
 *   <li>{@link #registerBehaviour(ResourceLocation, Function)} — records the
 *       factory so behaviour ids remain known (no tick loop ever invokes it);</li>
 *   <li>{@link #captureSingle(ServerLevel, BlockPos, BlockState, SubLevelBehaviour)}
 *       and {@link #capture(ServerLevel, BlockPos, BlockPos, SubLevelBehaviour)}
 *       — build a detached {@link SubLevel} value object and leave the real
 *       world completely untouched, so the "physicalized block" degrades to a
 *       normal block;</li>
 *   <li>{@link #release(ServerLevel, SubLevel)} — a no-op.</li>
 * </ul>
 *
 * <p><b>Guarantee:</b> every public method is defensive and never throws. The
 * shim performs no world mutation, no chunk forcing, no networking and no
 * registration of live sub-levels.</p>
 */
public final class SubLevelRegistry {

    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger("epca_physics-shim");

    private static final Map<ResourceLocation, Function<CompoundTag, SubLevelBehaviour>> BEHAVIOUR_FACTORIES =
            new ConcurrentHashMap<>();

    /** Kept for API parity; the shim never puts live sub-levels in here. */
    private static final Map<UUID, SubLevel> SUB_LEVELS = new ConcurrentHashMap<>();

    private static final AtomicBoolean DEGRADED_WARNING_LOGGED = new AtomicBoolean();

    private static final AABB FULL_CUBE = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private SubLevelRegistry() {
    }

    // ------------------------------------------------------------------
    // Behaviour registry
    // ------------------------------------------------------------------

    /**
     * Registers a behaviour factory under {@code id}, mirroring the original
     * API. The factory is stored but never invoked by the shim's own code;
     * {@link #createBehaviour(ResourceLocation, CompoundTag)} can be used by
     * callers that still want to instantiate one.
     *
     * @param id      behaviour id, ignored when {@code null}
     * @param factory factory from saved NBT to behaviour, ignored when {@code null}
     */
    public static void registerBehaviour(@Nullable ResourceLocation id,
                                         @Nullable Function<CompoundTag, SubLevelBehaviour> factory) {
        if (id == null || factory == null) {
            return;
        }
        try {
            BEHAVIOUR_FACTORIES.put(id, factory);
        } catch (Throwable throwable) {
            LOGGER.log(java.util.logging.Level.WARNING,
                    "[epca_physics shim] could not register sub-level behaviour " + id, throwable);
        }
    }

    /**
     * Instantiates a previously registered behaviour and calls
     * {@link SubLevelBehaviour#load(CompoundTag)} on it.
     *
     * @return the behaviour, or {@code null} when unknown or when creation
     *         failed (the shim never propagates an exception)
     */
    @Nullable
    public static SubLevelBehaviour createBehaviour(@Nullable ResourceLocation id, @Nullable CompoundTag data) {
        if (id == null) {
            return null;
        }
        Function<CompoundTag, SubLevelBehaviour> factory = BEHAVIOUR_FACTORIES.get(id);
        if (factory == null) {
            return null;
        }
        CompoundTag safeData = data != null ? data : new CompoundTag();
        try {
            SubLevelBehaviour behaviour = factory.apply(safeData);
            if (behaviour != null) {
                behaviour.load(safeData);
            }
            return behaviour;
        } catch (Throwable throwable) {
            LOGGER.log(java.util.logging.Level.WARNING,
                    "[epca_physics shim] failed to create sub-level behaviour " + id, throwable);
            return null;
        }
    }

    /** True when a factory is registered for {@code id}. */
    public static boolean isBehaviourRegistered(@Nullable ResourceLocation id) {
        return id != null && BEHAVIOUR_FACTORIES.containsKey(id);
    }

    // ------------------------------------------------------------------
    // Capture (degraded: read-only, leaves the world untouched)
    // ------------------------------------------------------------------

    /**
     * Degraded replacement for the original "turn this block into a physical
     * body" operation.
     *
     * <p>The block is <b>not</b> removed from the world, no sub-level is
     * registered and nothing is synchronized. A detached {@link SubLevel}
     * value object describing the block is returned so callers that use the
     * return value keep working; the block simply stays a normal block.</p>
     *
     * @return a detached sub-level value object, never {@code null}
     */
    public static SubLevel captureSingle(@Nullable ServerLevel level, @Nullable BlockPos pos,
                                         @Nullable BlockState state,
                                         @Nullable SubLevelBehaviour behaviour) {
        warnDegraded();
        BlockPos safePos = pos != null ? pos : BlockPos.ZERO;
        BlockState safeState = state != null ? state : Blocks.AIR.defaultBlockState();
        try {
            List<SubLevel.BlockEntry> entries = new ArrayList<>(1);
            entries.add(new SubLevel.BlockEntry(BlockPos.ZERO, safeState, 1.0D, FULL_CUBE, null));
            SubLevel subLevel = new SubLevel(UUID.randomUUID(), entries,
                    new Vec3(safePos.getX(), safePos.getY(), safePos.getZ()));
            subLevel.setDimension(level != null ? level.dimension() : Level.OVERWORLD);
            subLevel.setBehaviour(behaviour);
            return subLevel;
        } catch (Throwable throwable) {
            LOGGER.log(java.util.logging.Level.WARNING,
                    "[epca_physics shim] captureSingle failed; returning an empty sub-level", throwable);
            SubLevel fallback = new SubLevel(UUID.randomUUID(), List.of(), Vec3.ZERO);
            fallback.setBehaviour(behaviour);
            return fallback;
        }
    }

    /**
     * Degraded, read-only replacement for {@code capture(level, a, b)}.
     *
     * <p>Collects up to {@link SubLevel#MAX_BLOCKS} non-air block states into a
     * detached value object. The blocks are <b>not</b> removed from the
     * world.</p>
     */
    public static SubLevel capture(@Nullable ServerLevel level, @Nullable BlockPos a, @Nullable BlockPos b) {
        return capture(level, a, b, null);
    }

    /**
     * Degraded, read-only replacement for
     * {@code capture(level, a, b, behaviour)}.
     *
     * @return a detached sub-level value object, never {@code null}
     */
    public static SubLevel capture(@Nullable ServerLevel level, @Nullable BlockPos a, @Nullable BlockPos b,
                                   @Nullable SubLevelBehaviour behaviour) {
        warnDegraded();
        BlockPos safeA = a != null ? a : BlockPos.ZERO;
        BlockPos safeB = b != null ? b : safeA;
        int minX = Math.min(safeA.getX(), safeB.getX());
        int minY = Math.min(safeA.getY(), safeB.getY());
        int minZ = Math.min(safeA.getZ(), safeB.getZ());
        int maxX = Math.max(safeA.getX(), safeB.getX());
        int maxY = Math.max(safeA.getY(), safeB.getY());
        int maxZ = Math.max(safeA.getZ(), safeB.getZ());

        List<SubLevel.BlockEntry> entries = new ArrayList<>();
        if (level != null) {
            try {
                for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
                    if (entries.size() >= SubLevel.MAX_BLOCKS) {
                        break;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    BlockPos offset = new BlockPos(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ);
                    entries.add(new SubLevel.BlockEntry(offset, state, 1.0D, FULL_CUBE, null));
                }
            } catch (Throwable throwable) {
                LOGGER.log(java.util.logging.Level.FINE,
                        "[epca_physics shim] capture aborted while reading the region", throwable);
            }
        }

        SubLevel subLevel = new SubLevel(UUID.randomUUID(), entries,
                new Vec3(minX, minY, minZ));
        subLevel.setDimension(level != null ? level.dimension() : Level.OVERWORLD);
        subLevel.setBehaviour(behaviour);
        return subLevel;
    }

    /**
     * No-op: the shim never captured the block, so there is nothing to
     * release, no chunk to unforce and nothing to sync.
     */
    public static void release(@Nullable ServerLevel level, @Nullable SubLevel subLevel) {
        if (subLevel == null) {
            return;
        }
        try {
            SUB_LEVELS.remove(subLevel.id());
        } catch (Throwable ignored) {
            // no-op by design
        }
    }

    // ------------------------------------------------------------------
    // Minimal in-memory bookkeeping (API parity, not used by epca)
    // ------------------------------------------------------------------

    @Nullable
    public static SubLevel byId(@Nullable UUID id) {
        return id != null ? SUB_LEVELS.get(id) : null;
    }

    public static Collection<SubLevel> all() {
        return new ArrayList<>(SUB_LEVELS.values());
    }

    public static int count() {
        return SUB_LEVELS.size();
    }

    public static boolean isEmpty() {
        return SUB_LEVELS.isEmpty();
    }

    public static void put(@Nullable SubLevel subLevel) {
        if (subLevel != null) {
            SUB_LEVELS.put(subLevel.id(), subLevel);
        }
    }

    public static void remove(@Nullable UUID id) {
        if (id != null) {
            SUB_LEVELS.remove(id);
        }
    }

    public static void clear() {
        SUB_LEVELS.clear();
    }

    // ------------------------------------------------------------------
    // Diagnostics
    // ------------------------------------------------------------------

    private static void warnDegraded() {
        if (DEGRADED_WARNING_LOGGED.compareAndSet(false, true)) {
            LOGGER.info("[epca_physics shim] epca_physics is not available on 1.21.1: "
                    + "physicalized sub-level blocks are disabled and degrade to normal blocks.");
        }
    }
}
