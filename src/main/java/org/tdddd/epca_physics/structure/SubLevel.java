package org.tdddd.epca_physics.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 1.21.1 compatibility shim for {@code epca_physics}'s {@code SubLevel}.
 *
 * <p>The original class is a rigid body made of up to {@value #MAX_BLOCKS}
 * block states, simulated by the Forge-only {@code epca_physics} mod. That mod
 * cannot be used on 1.21.1 / NeoForge, so this class is reduced to a plain
 * <b>value object</b>: it stores the sub-level id, its dimension, pose
 * (position + rotation), velocity and the block payload, and it implements the
 * pure geometry queries epca relies on.</p>
 *
 * <p>There is deliberately <b>no</b> physics, collision, block-tick or
 * networking code here. All methods are defensive no-ops/identity operations
 * and are guaranteed not to throw, even when given {@code null} arguments.</p>
 *
 * <p>API surface kept because epca uses it:</p>
 * <ul>
 *   <li>{@link #worldCentreOfMass()}</li>
 *   <li>{@link #worldAabb()}</li>
 *   <li>{@link #position()}, {@link #rotation()}</li>
 *   <li>{@link #velocity()}, {@link #setVelocity(Vec3)}</li>
 *   <li>{@link #rotateAboutCentreOfMass(Quaternionf)}</li>
 *   <li>{@link #countBlocks(Predicate)}</li>
 *   <li>{@link #blockWorldPos(BlockEntry)}</li>
 *   <li>{@link BlockEntry#state()}</li>
 * </ul>
 */
public final class SubLevel {

    /** Hard cap on the number of blocks a sub-level may hold. */
    public static final int MAX_BLOCKS = 4096;

    /** An empty box, returned when a shape query finds nothing. */
    public static final AABB EMPTY_BOX = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    /** Full-cube local box used by the convenience {@link BlockEntry} constructor. */
    private static final AABB FULL_CUBE = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private static final double AXIS_EPSILON = 1.0E-5D;

    private final UUID id;
    private final List<BlockEntry> blocks;

    private ResourceKey<Level> dimension = Level.OVERWORLD;
    private Vec3 position;
    private Quaternionf rotation;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 angularVelocity = Vec3.ZERO;

    @Nullable
    private SubLevelBehaviour behaviour;

    private AABB localBounds = FULL_CUBE;
    private Vec3 localCentreOfMass = new Vec3(0.5D, 0.5D, 0.5D);
    private double totalMass;

    /**
     * Creates a sub-level with an identity rotation.
     *
     * @param id       unique id, a random one is generated when {@code null}
     * @param blocks   initial block payload, may be {@code null}
     * @param position world position of the local origin, may be {@code null}
     */
    public SubLevel(UUID id, List<BlockEntry> blocks, Vec3 position) {
        this(id, blocks, position, new Quaternionf());
    }

    /**
     * Creates a sub-level.
     *
     * @param id       unique id, a random one is generated when {@code null}
     * @param blocks   initial block payload, may be {@code null}
     * @param position world position of the local origin, may be {@code null}
     * @param rotation local-to-world rotation, may be {@code null} (identity)
     */
    public SubLevel(UUID id, List<BlockEntry> blocks, Vec3 position, Quaternionf rotation) {
        this.id = id != null ? id : UUID.randomUUID();
        this.blocks = new ArrayList<>();
        if (blocks != null) {
            for (BlockEntry entry : blocks) {
                if (entry != null && this.blocks.size() < MAX_BLOCKS) {
                    this.blocks.add(entry);
                }
            }
        }
        this.position = position != null ? position : Vec3.ZERO;
        this.rotation = rotation != null ? new Quaternionf(rotation) : new Quaternionf();
        this.recompute();
    }

    // ------------------------------------------------------------------
    // Identity / dimension
    // ------------------------------------------------------------------

    public UUID id() {
        return this.id;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public void setDimension(@Nullable ResourceKey<Level> dimension) {
        this.dimension = dimension != null ? dimension : Level.OVERWORLD;
    }

    // ------------------------------------------------------------------
    // Payload
    // ------------------------------------------------------------------

    /** The immutable block payload. The returned list is unmodifiable. */
    public List<BlockEntry> blocks() {
        return Collections.unmodifiableList(this.blocks);
    }

    public int size() {
        return this.blocks.size();
    }

    public boolean isEmpty() {
        return this.blocks.isEmpty();
    }

    public double totalMass() {
        return this.totalMass;
    }

    /**
     * Recomputes the local bounds, total mass and centre of mass. Safe to call
     * at any time; never throws.
     */
    public void recompute() {
        double mass = 0.0D;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        double momentX = 0.0D;
        double momentY = 0.0D;
        double momentZ = 0.0D;

        try {
            for (BlockEntry entry : this.blocks) {
                double entryMass = entry.mass();
                if (Double.isFinite(entryMass) && entryMass > 0.0D) {
                    mass += entryMass;
                }
                AABB local = entry.localBox().move(entry.offset().getX(), entry.offset().getY(), entry.offset().getZ());
                minX = Math.min(minX, local.minX);
                minY = Math.min(minY, local.minY);
                minZ = Math.min(minZ, local.minZ);
                maxX = Math.max(maxX, local.maxX);
                maxY = Math.max(maxY, local.maxY);
                maxZ = Math.max(maxZ, local.maxZ);
                if (entryMass > 0.0D) {
                    Vec3 centre = local.getCenter();
                    momentX += entryMass * centre.x;
                    momentY += entryMass * centre.y;
                    momentZ += entryMass * centre.z;
                }
            }
        } catch (Throwable ignored) {
            // Defensive: a malformed payload must not break the caller.
            mass = 0.0D;
            minX = minY = minZ = 0.0D;
            maxX = maxY = maxZ = 1.0D;
            momentX = momentY = momentZ = 0.0D;
        }

        this.totalMass = mass;
        this.localBounds = this.blocks.isEmpty() || minX > maxX
                ? FULL_CUBE
                : new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        this.localCentreOfMass = mass > 0.0D
                ? new Vec3(momentX / mass, momentY / mass, momentZ / mass)
                : this.localBounds.getCenter();
    }

    public AABB localBounds() {
        return this.localBounds;
    }

    public Vec3 localCentreOfMass() {
        return this.localCentreOfMass;
    }

    public Vec3 worldCentreOfMass() {
        return this.localToWorld(this.localCentreOfMass);
    }

    // ------------------------------------------------------------------
    // Pose
    // ------------------------------------------------------------------

    public Vec3 position() {
        return this.position;
    }

    public void setPosition(@Nullable Vec3 position) {
        this.position = position != null ? position : Vec3.ZERO;
    }

    /** Returns a defensive copy of the rotation. */
    public Quaternionf rotation() {
        return new Quaternionf(this.rotation);
    }

    public void setRotation(@Nullable Quaternionf rotation) {
        this.rotation = rotation != null ? new Quaternionf(rotation) : new Quaternionf();
    }

    /**
     * Rotates the body around its current world centre of mass, keeping that
     * centre fixed. This is the operation epca's rolling pumpkin behaviour uses.
     */
    public void rotateAboutCentreOfMass(@Nullable Quaternionf nextRotation) {
        if (nextRotation == null) {
            return;
        }
        Vec3 comWorld = this.worldCentreOfMass();
        Vector3f local = new Vector3f((float) this.localCentreOfMass.x,
                (float) this.localCentreOfMass.y,
                (float) this.localCentreOfMass.z);
        Vector3f rotated = nextRotation.transform(local);
        this.position = new Vec3(comWorld.x - rotated.x, comWorld.y - rotated.y, comWorld.z - rotated.z);
        this.rotation = new Quaternionf(nextRotation);
    }

    public Vec3 velocity() {
        return this.velocity;
    }

    public void setVelocity(@Nullable Vec3 velocity) {
        this.velocity = velocity != null ? velocity : Vec3.ZERO;
    }

    public Vec3 angularVelocity() {
        return this.angularVelocity;
    }

    public void setAngularVelocity(@Nullable Vec3 angularVelocity) {
        this.angularVelocity = angularVelocity != null ? angularVelocity : Vec3.ZERO;
    }

    @Nullable
    public SubLevelBehaviour behaviour() {
        return this.behaviour;
    }

    public void setBehaviour(@Nullable SubLevelBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    // ------------------------------------------------------------------
    // Geometry
    // ------------------------------------------------------------------

    public Vec3 localToWorld(@Nullable Vec3 local) {
        if (local == null) {
            return this.position;
        }
        Vector3f rotated = this.rotation.transform(
                new Vector3f((float) local.x, (float) local.y, (float) local.z));
        return new Vec3(this.position.x + rotated.x, this.position.y + rotated.y, this.position.z + rotated.z);
    }

    public Vec3 worldToLocal(@Nullable Vec3 world) {
        if (world == null) {
            return Vec3.ZERO;
        }
        Vector3f relative = new Vector3f(
                (float) (world.x - this.position.x),
                (float) (world.y - this.position.y),
                (float) (world.z - this.position.z));
        Vector3f unrotated = this.rotation.transformInverse(relative);
        return new Vec3(unrotated.x, unrotated.y, unrotated.z);
    }

    /** The axis-aligned bounds of the rotated body in world space. */
    public AABB worldAabb() {
        if (this.blocks.isEmpty()) {
            return new AABB(this.position.x, this.position.y, this.position.z,
                    this.position.x + 1.0D, this.position.y + 1.0D, this.position.z + 1.0D);
        }
        return rotateBox(this.localBounds, this.position, this.rotation);
    }

    public AABB worldAabbAt(@Nullable Vec3 atPosition, @Nullable Quaternionf atRotation) {
        Vec3 pos = atPosition != null ? atPosition : this.position;
        Quaternionf rot = atRotation != null ? atRotation : this.rotation;
        if (this.blocks.isEmpty()) {
            return new AABB(pos.x, pos.y, pos.z, pos.x + 1.0D, pos.y + 1.0D, pos.z + 1.0D);
        }
        return rotateBox(this.localBounds, pos, rot);
    }

    public AABB blockWorldBox(BlockEntry entry) {
        return this.blockWorldBoxAt(entry, this.position, this.rotation);
    }

    public AABB blockWorldBoxAt(@Nullable BlockEntry entry, @Nullable Vec3 atPosition, @Nullable Quaternionf atRotation) {
        Vec3 pos = atPosition != null ? atPosition : this.position;
        Quaternionf rot = atRotation != null ? atRotation : this.rotation;
        if (entry == null) {
            return new AABB(pos.x, pos.y, pos.z, pos.x + 1.0D, pos.y + 1.0D, pos.z + 1.0D);
        }
        AABB local = entry.localBox().move(entry.offset().getX(), entry.offset().getY(), entry.offset().getZ());
        return rotateBox(local, pos, rot);
    }

    /**
     * World position of the centre of a block entry, computed the same way as
     * the original implementation.
     */
    public BlockPos blockWorldPos(@Nullable BlockEntry entry) {
        if (entry == null) {
            return BlockPos.containing(this.position);
        }
        return BlockPos.containing(this.localToWorld(entry.localCenter()));
    }

    public double distanceToSqr(@Nullable Vec3 point) {
        return point == null ? 0.0D : this.position.distanceToSqr(point);
    }

    // ------------------------------------------------------------------
    // Block queries
    // ------------------------------------------------------------------

    public List<BlockState> blockStates() {
        if (this.blocks.isEmpty()) {
            return List.of();
        }
        List<BlockState> states = new ArrayList<>(this.blocks.size());
        for (BlockEntry entry : this.blocks) {
            states.add(entry.state());
        }
        return Collections.unmodifiableList(states);
    }

    /**
     * Counts the block entries whose state matches {@code filter}.
     *
     * @return the match count; {@code 0} when {@code filter} is {@code null}
     */
    public int countBlocks(@Nullable Predicate<BlockState> filter) {
        if (filter == null) {
            return 0;
        }
        int count = 0;
        try {
            for (BlockEntry entry : this.blocks) {
                if (filter.test(entry.state())) {
                    count++;
                }
            }
        } catch (Throwable ignored) {
            // A predicate must never break the caller.
        }
        return count;
    }

    @Nullable
    public BlockEntry entryAt(@Nullable BlockPos offset) {
        if (offset == null) {
            return null;
        }
        for (BlockEntry entry : this.blocks) {
            if (entry.offset().equals(offset)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public BlockEntry removeBlock(@Nullable BlockPos offset) {
        if (offset == null) {
            return null;
        }
        for (int i = 0; i < this.blocks.size(); i++) {
            if (this.blocks.get(i).offset().equals(offset)) {
                BlockEntry removed = this.blocks.remove(i);
                this.recompute();
                return removed;
            }
        }
        return null;
    }

    public void addBlock(@Nullable BlockEntry entry) {
        if (entry == null || this.blocks.size() >= MAX_BLOCKS) {
            return;
        }
        this.blocks.add(entry);
        this.recompute();
    }

    // ------------------------------------------------------------------
    // Static geometry helpers (kept for API parity)
    // ------------------------------------------------------------------

    /**
     * Returns the axis-aligned bounds of {@code local} transformed by
     * {@code rotation} and translated by {@code position}.
     */
    public static AABB rotateBox(@Nullable AABB local, @Nullable Vec3 position, @Nullable Quaternionf rotation) {
        AABB box = local != null ? local : FULL_CUBE;
        Vec3 pos = position != null ? position : Vec3.ZERO;
        Quaternionf rot = rotation != null ? rotation : new Quaternionf();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (int corner = 0; corner < 8; corner++) {
            double x = (corner & 1) == 0 ? box.minX : box.maxX;
            double y = (corner & 2) == 0 ? box.minY : box.maxY;
            double z = (corner & 4) == 0 ? box.minZ : box.maxZ;
            Vector3f rotated = rot.transform(new Vector3f((float) x, (float) y, (float) z));
            minX = Math.min(minX, pos.x + rotated.x);
            minY = Math.min(minY, pos.y + rotated.y);
            minZ = Math.min(minZ, pos.z + rotated.z);
            maxX = Math.max(maxX, pos.x + rotated.x);
            maxY = Math.max(maxY, pos.y + rotated.y);
            maxZ = Math.max(maxZ, pos.z + rotated.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** True when the rotation maps every local axis onto a world axis. */
    public static boolean isAxisAligned(@Nullable Quaternionf rotation) {
        Quaternionf rot = rotation != null ? rotation : new Quaternionf();
        return isUnitAxis(rot.transform(new Vector3f(1.0F, 0.0F, 0.0F)))
                && isUnitAxis(rot.transform(new Vector3f(0.0F, 1.0F, 0.0F)))
                && isUnitAxis(rot.transform(new Vector3f(0.0F, 0.0F, 1.0F)));
    }

    public boolean isAxisAligned() {
        return isAxisAligned(this.rotation);
    }

    private static boolean isUnitAxis(Vector3f v) {
        return Math.abs(Math.abs(v.x) - 1.0F) < AXIS_EPSILON && Math.abs(v.y) < AXIS_EPSILON && Math.abs(v.z) < AXIS_EPSILON
                || Math.abs(Math.abs(v.y) - 1.0F) < AXIS_EPSILON && Math.abs(v.x) < AXIS_EPSILON && Math.abs(v.z) < AXIS_EPSILON
                || Math.abs(Math.abs(v.z) - 1.0F) < AXIS_EPSILON && Math.abs(v.x) < AXIS_EPSILON && Math.abs(v.y) < AXIS_EPSILON;
    }

    /** Rotates a world vector back into local space. */
    public static Vec3 worldToLocalAt(@Nullable Vec3 world, @Nullable Vec3 atPosition, @Nullable Quaternionf atRotation) {
        if (world == null) {
            return Vec3.ZERO;
        }
        Vec3 pos = atPosition != null ? atPosition : Vec3.ZERO;
        Quaternionf rot = atRotation != null ? atRotation : new Quaternionf();
        Vector3f relative = new Vector3f(
                (float) (world.x - pos.x), (float) (world.y - pos.y), (float) (world.z - pos.z));
        Vector3f unrotated = rot.transformInverse(relative);
        return new Vec3(unrotated.x, unrotated.y, unrotated.z);
    }

    // ------------------------------------------------------------------
    // Block payload entry
    // ------------------------------------------------------------------

    /**
     * One block inside a sub-level: local offset, block state, mass, local
     * collision box and (optional) block-entity NBT. Immutable apart from the
     * block-entity tag.
     */
    public static final class BlockEntry {

        private final BlockPos offset;
        private final BlockState state;
        private final double mass;
        private final AABB localBox;

        @Nullable
        private CompoundTag beTag;

        public BlockEntry(BlockPos offset, BlockState state, double mass, AABB localBox, @Nullable CompoundTag beTag) {
            this.offset = offset != null ? offset : BlockPos.ZERO;
            this.state = state != null ? state : Blocks.AIR.defaultBlockState();
            this.mass = Double.isFinite(mass) && mass > 0.0D ? mass : 0.0D;
            this.localBox = localBox != null ? localBox : FULL_CUBE;
            this.beTag = beTag;
        }

        /** Convenience entry for a single unit-cube block. */
        public BlockEntry(BlockPos offset, BlockState state) {
            this(offset, state, 1.0D, FULL_CUBE, null);
        }

        public BlockPos offset() {
            return this.offset;
        }

        public BlockState state() {
            return this.state;
        }

        public double mass() {
            return this.mass;
        }

        public AABB localBox() {
            return this.localBox;
        }

        @Nullable
        public CompoundTag beTag() {
            return this.beTag;
        }

        public void setBeTag(@Nullable CompoundTag tag) {
            this.beTag = tag;
        }

        public Vec3 localCenter() {
            return new Vec3(this.offset.getX() + 0.5D, this.offset.getY() + 0.5D, this.offset.getZ() + 0.5D);
        }

        /** True when this entry has collision (full-cube heuristic in the shim). */
        public boolean collidable() {
            try {
                return !this.state.isAir();
            } catch (Throwable ignored) {
                return false;
            }
        }
    }
}
