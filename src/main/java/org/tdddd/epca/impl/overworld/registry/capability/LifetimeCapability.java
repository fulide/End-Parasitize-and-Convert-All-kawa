package org.tdddd.epca.impl.overworld.registry.capability;

import java.util.function.Supplier;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.Nullable;

/**
 * Deprecated 1.20.1-era name kept as a thin alias so that existing imports keep compiling.
 *
 * <p><b>Use {@link LifetimeData} / {@link ModAttachments#LIFETIME} instead.</b> The old class was a Forge capability
 * provider ({@code Capability}, {@code ICapabilitySerializable}, {@code LazyOptional},
 * {@code AttachCapabilitiesEvent}); that whole API is gone in NeoForge 1.21.1 and is replaced by the data attachment
 * stored in {@link #LIFETIME}.</p>
 */
@Deprecated(forRemoval = false)
public class LifetimeCapability extends LifetimeData {

    /** 1.20.1 {@code LifetimeCapability.LIFETIME} alias — the canonical type is {@link ModAttachments#LIFETIME}. */
    public static final Supplier<AttachmentType<LifetimeData>> LIFETIME = ModAttachments.LIFETIME;

    public LifetimeCapability() {
        super();
    }

    public LifetimeCapability(@Nullable LivingEntity entity) {
        super(entity);
    }
}
