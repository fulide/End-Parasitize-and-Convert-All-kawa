package org.tdddd.epca.impl.events;

import net.minecraft.world.entity.LivingEntity;
import org.tdddd.epca.impl.overworld.registry.capability.ModAttachments;
import org.tdddd.epca.impl.overworld.registry.capability.ShieldData;

/**
 * Accessor for the per-entity soul-protection shield pool.
 *
 * <p>1.20.1 used a Forge {@code Capability<IShieldCapability>} attached through
 * {@code AttachCapabilitiesEvent}. NeoForge 1.21.1 removed {@code Capability},
 * {@code CapabilityToken}, {@code LazyOptional} and {@code AttachCapabilitiesEvent}, so the pool is
 * now a data attachment ({@link ModAttachments#SHIELD}): it always resolves and never needs
 * attaching, invalidating or serialising by hand.</p>
 *
 * <p>The class name is kept because {@link org.tdddd.epca.impl.utils.ShieldProtectionHelper} and
 * {@code ShieldEventHandler} refer to it.</p>
 */
public final class ShieldCapabilityHandler {

    private ShieldCapabilityHandler() {
    }

    /** Returns the (lazily created, always present) shield pool of {@code entity}. */
    public static ShieldData get(LivingEntity entity) {
        return entity.getData(ModAttachments.SHIELD);
    }
}
