package org.tdddd.epca.impl.compat;

/**
 * 1.21.1 port: eej soft-dependency stub.
 *
 * <p>The 1.20.1 build had an optional integration with the <b>End Element
 * Journey</b> ({@code eej}) mod. eej is Forge-only and has no build for
 * Minecraft 1.21.1, so the integration was removed for this port.</p>
 *
 * <p>This class is deliberately dependency-free (it no longer even touches
 * {@code ModList}) and always reports the integration as unavailable, so the
 * few remaining guarded call sites in epca stay dormant. It is kept - rather
 * than deleted - only so those call sites still compile; the main port work
 * may remove them and this class afterwards.</p>
 */
public final class EejCompat {

    /** The mod id of the removed optional dependency. */
    public static final String MOD_ID = "eej";

    private EejCompat() {
    }

    /**
     * Always {@code false} on 1.21.1: epca ships no eej integration any more.
     */
    public static boolean loaded() {
        return false;
    }
}
