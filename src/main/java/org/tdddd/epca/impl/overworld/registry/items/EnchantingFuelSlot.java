package org.tdddd.epca.impl.overworld.registry.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.tdddd.epca.impl.overworld.registry.ModItems;

/**
 * Replacement for the vanilla enchanting-table fuel slot.
 *
 * <p>1.21.1 vanilla hard-codes the fuel slot to {@code stack.is(Items.LAPIS_LAZULI)}
 * ({@code EnchantmentMenu} even carries a "TODO - replace with the tag" comment), so a modded
 * lapis-like item can never be placed there. {@link org.tdddd.epca.impl.mixin.common.EnchantmentMenuMixin}
 * swaps the vanilla slot for this one, which additionally accepts</p>
 * <ul>
 *   <li>items in the conventional {@code c:enchanting_fuels} tag (the tag the upstream mod already
 *       shipped for exactly this purpose), and</li>
 *   <li>{@code epca:infested_lapis_lazuli} explicitly, so the feature still works if a pack strips
 *       or renames the tag.</li>
 * </ul>
 *
 * <p>The rest of {@code EnchantmentMenu} (cost calculation, consumption in
 * {@code clickMenuButton}, {@code getGoldCount()}) never inspects the item id, so accepting the item
 * here is all that is needed for it to be spent like lapis.</p>
 */
public class EnchantingFuelSlot extends Slot {

    /** Conventional tag used by the upstream mod for "counts as enchanting fuel". */
    public static final TagKey<Item> ENCHANTING_FUELS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "enchanting_fuels"));

    private static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI =
            ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli");

    public EnchantingFuelSlot(net.minecraft.world.Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    /** True when {@code stack} may be burned as enchanting fuel. */
    public static boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(Items.LAPIS_LAZULI)
                || stack.is(ENCHANTING_FUELS)
                || stack.is(ModItems.INFESTED_LAPIS_LAZULI.get());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isFuel(stack);
    }

    @Override
    public com.mojang.datafixers.util.Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_LAPIS_LAZULI);
    }
}
