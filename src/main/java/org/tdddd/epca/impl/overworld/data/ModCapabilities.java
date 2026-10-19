package org.tdddd.epca.impl.overworld.data;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.IItemHandler;

public class ModCapabilities {
    /**
     * Item capability used for the living-armor "armor box" handler.
     *
     * <p>NeoForge 1.21 removed the untyped {@code Capability<T>} / {@code CapabilityManager} /
     * {@code CapabilityToken} API. Capabilities are now typed by where they can be queried
     * ({@link ItemCapability}, {@code BlockCapability}, {@code EntityCapability}); this one is queried
     * from an {@code ItemStack} via {@code stack.getCapability(ARMOR_HANDLER)}.</p>
     */
    public static final ItemCapability<IItemHandler, Void> ARMOR_HANDLER =
            ItemCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath("epca", "armor_handler"),
                    IItemHandler.class);
}
