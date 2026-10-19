package org.tdddd.epca.impl.events;

/**
 * Obsolete since the 1.21.1 port.
 *
 * <p>1.20.1 attached the soul-protection shield capability to every {@code LivingEntity} from
 * {@code AttachCapabilitiesEvent}. NeoForge 1.21.1 removed {@code AttachCapabilitiesEvent},
 * {@code Capability}, {@code CapabilityToken} and {@code LazyOptional}; the shield pool is now a
 * data attachment ({@code ModAttachments.SHIELD}, see {@link ShieldCapabilityHandler}) which needs
 * no attaching code at all, so this class is intentionally empty.</p>
 *
 * <p>It survives only because {@code epca.java} still calls
 * {@code NeoForge.EVENT_BUS.register(new ShieldAttachHandler())}. That registration is a harmless
 * no-op and can be deleted together with this class.</p>
 */
public class ShieldAttachHandler {
}
