package org.tdddd.epca_physics.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.1 compatibility shim for {@code epca_physics}'s sub-level behaviour API.
 *
 * <p>This interface is a source-compatible replacement for the interface of the
 * same name in the Forge-only {@code epca_physics} 1.0.0 mod, which cannot be
 * loaded on Minecraft 1.21.1 / NeoForge. It exists so that epca's
 * {@code InfestedPumpkinBehaviour} keeps compiling without changes.</p>
 *
 * <p><b>Behavioural note:</b> the shim does not contain the physics engine, a
 * server tick loop, or any networking. Consequently
 * {@link #serverTick(ServerLevel, SubLevel)} and
 * {@link #onBlockMined(ServerLevel, SubLevel, SubLevel.BlockEntry, ServerPlayer)}
 * are <i>never invoked</i> by {@link SubLevelRegistry}; they are retained purely
 * for API compatibility. Implementations may still be registered through
 * {@link SubLevelRegistry#registerBehaviour(ResourceLocation, java.util.function.Function)}
 * and serialized, which is harmless.</p>
 */
public interface SubLevelBehaviour {

    /**
     * The id this behaviour is registered under.
     *
     * @return the behaviour id, never {@code null} for well-formed implementations
     */
    ResourceLocation id();

    /**
     * Called once per server tick for a live sub-level in the original mod.
     * Never called by the 1.21.1 shim.
     */
    default void serverTick(ServerLevel level, SubLevel subLevel) {
        // no-op: the shim has no physics tick loop
    }

    /**
     * Called when a block belonging to the sub-level is mined in the original mod.
     *
     * @return {@code true} when the behaviour handled drops itself
     */
    default boolean onBlockMined(ServerLevel level, SubLevel subLevel,
                                 SubLevel.BlockEntry entry, @Nullable ServerPlayer player) {
        return false;
    }

    /** Persists behaviour-specific state. */
    default void save(CompoundTag tag) {
    }

    /** Restores behaviour-specific state. */
    default void load(CompoundTag tag) {
    }
}
