package org.tdddd.epca.impl.overworld.registry.blocks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.tdddd.epca.impl.epca;

/**
 * NeoForge 1.21.1 capability providers for EPCA block entities.
 *
 * <p>1.20.1 exposed {@code ForgeCapabilities.ITEM_HANDLER} by overriding
 * {@code BlockEntity#getCapability(...)} and returning a {@code LazyOptional}. That API (capability
 * providers, {@code CapabilityManager}, {@code LazyOptional}) no longer exists in NeoForge 21.1;
 * capabilities are now <em>registered</em> against a {@code BlockCapability} on the mod event bus,
 * and querying returns the handler directly (or {@code null}) instead of a {@code LazyOptional}.</p>
 *
 * <p>This mirrors the entity-side {@code EpcaEntityCapabilities} class. It is picked up
 * automatically through {@link EventBusSubscriber}, so no manual registration in {@code epca.java} is
 * required.</p>
 */
@EventBusSubscriber(modid = epca.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class SwallowCystCapabilities {

    private SwallowCystCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.SWALLOW_CYST.get(),
                (be, side) -> be.getInventory());
    }
}
