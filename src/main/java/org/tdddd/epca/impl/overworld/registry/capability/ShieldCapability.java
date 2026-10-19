package org.tdddd.epca.impl.overworld.registry.capability;

/**
 * Deprecated 1.20.1-era name kept as a thin alias so that existing imports keep compiling.
 *
 * <p><b>Use {@link ShieldData} / {@link ModAttachments#SHIELD} instead.</b> The old class was a Forge capability
 * provider; that API is gone in NeoForge 1.21.1 and is replaced by the data attachment stored in
 * {@link ModAttachments#SHIELD}.</p>
 */
@Deprecated(forRemoval = false)
public class ShieldCapability extends ShieldData {

    public ShieldCapability() {
        super();
    }
}
