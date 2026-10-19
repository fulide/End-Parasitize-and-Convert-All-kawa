package org.tdddd.epca.impl.compat;

import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.21.1 port: removed eej altar compatibility layer.
 *
 * <p>In 1.20.1 this class was the single place that referenced eej's altar API
 * ({@code AltarInteractionRegistry}, {@code AltarBlockTags}) and registered
 * {@code EpcaAltarInteractionHandler}. eej cannot be used on 1.21.1, so both
 * methods are now safe no-ops.</p>
 *
 * <p>The class itself is kept because {@code epca.java} and
 * {@code BlockConversionManager} still reference it; it is guaranteed not to
 * touch any eej class at load time.</p>
 */
public final class EejAltarCompat {

    private EejAltarCompat() {
    }

    /**
     * Formerly registered epca's altar interaction handler with eej.
     * Now a no-op.
     */
    public static void registerInteraction() {
        // no-op: eej is not available on 1.21.1
    }

    /**
     * Formerly tested eej's pedestal / altar-stone block tags.
     *
     * @return always {@code false} on 1.21.1, so the altar conversion speed
     *         multiplier in {@code BlockConversionManager} is never applied
     */
    public static boolean isAltarBlock(BlockState state) {
        return false;
    }
}
