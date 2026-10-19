package org.tdddd.epca.impl.overworld.registry.entities;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModEntities;

/**
 * NeoForge 1.21.1 capability providers for EPCA entities.
 *
 * <p>1.20.1 exposed {@code ForgeCapabilities.ITEM_HANDLER} by overriding
 * {@code Entity#getCapability(...)} and returning a {@code LazyOptional}. That API (capability providers,
 * {@code CapabilityManager}, {@code LazyOptional}) no longer exists in NeoForge 21.1; capabilities are now
 * <em>registered</em> against an {@code EntityCapability} on the mod event bus, and querying returns the handler
 * directly (or {@code null}) instead of a {@code LazyOptional}.</p>
 *
 * <p>The two Reshape entities keep their 27-slot internal inventory; this class exposes it under
 * {@link Capabilities.ItemHandler#ENTITY} (generic entity inventory queries) and
 * {@link Capabilities.ItemHandler#ENTITY_AUTOMATION} (hoppers, droppers and modded automation, matching the old
 * side-agnostic {@code ITEM_HANDLER} behaviour, because every {@code Direction} side used to return the same
 * handler).</p>
 */
@EventBusSubscriber(modid = epca.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class EpcaEntityCapabilities {

    private EpcaEntityCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.ItemHandler.ENTITY, ModEntities.RESHAPE_LONGARMS.get(),
                (entity, context) -> entity.getItemHandler());
        event.registerEntity(Capabilities.ItemHandler.ENTITY, ModEntities.RESHAPE_YELLOWEYE.get(),
                (entity, context) -> entity.getItemHandler());

        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, ModEntities.RESHAPE_LONGARMS.get(),
                (entity, side) -> entity.getItemHandler());
        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, ModEntities.RESHAPE_YELLOWEYE.get(),
                (entity, side) -> entity.getItemHandler());
    }
}
