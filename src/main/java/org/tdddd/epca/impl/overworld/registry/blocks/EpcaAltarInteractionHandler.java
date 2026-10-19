package org.tdddd.epca.impl.overworld.registry.blocks;

/**
 * 1.21.1 port: removed eej altar interaction handler.
 *
 * <p>The 1.20.1 version implemented eej's {@code AltarInteractionHandler} and
 * performed the altar sacrifice (kill stick diagnostics, gem-block sacrifice
 * detection, animal/villager consumption and the {@code BlockConversionManager}
 * sacrifice task). eej has no 1.21.1 build, so the handler was reduced to an
 * empty, dependency-free shell.</p>
 *
 * <p>Nothing constructs this class any more ({@code EejAltarCompat} is a
 * no-op). It is kept only because {@code epca.java} still imports it; it may be
 * deleted once that import is removed.</p>
 */
public final class EpcaAltarInteractionHandler {

    public EpcaAltarInteractionHandler() {
        // no-op
    }
}
