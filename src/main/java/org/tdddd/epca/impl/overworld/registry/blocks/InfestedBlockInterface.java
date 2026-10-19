package org.tdddd.epca.impl.overworld.registry.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Marker interface for the mod's infested blocks.
 *
 * <p>1.21.1 moved enchantments to {@code Holder<Enchantment>} values, so the old
 * {@code stack.getEnchantmentLevel(Enchantments.SILK_TOUCH)} no longer compiles. This shared helper
 * resolves the enchantment holder from the level's registry and keeps the original check
 * ({@code level > 0}) intact.</p>
 */
public interface InfestedBlockInterface {

    /** True when {@code stack} carries Silk Touch I or higher. */
    static boolean hasSilkTouch(Level level, ItemStack stack) {
        if (level == null || stack == null || stack.isEmpty()) {
            return false;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.SILK_TOUCH),
                stack) > 0;
    }
}
