package org.tdddd.epca.impl.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tdddd.epca.impl.overworld.registry.items.item.InfestedLapisLazuli;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adds up to two random curses when the enchantment table is used with infested lapis.
 *
 * <p>1.21.1 port: enchantments became data-driven {@code Holder<Enchantment>} values stored in the
 * {@code minecraft:enchantments} component. {@code EnchantmentHelper.getEnchantments/setEnchantments}
 * and {@code BuiltInRegistries.ENCHANTMENT} are gone; the curse pool now comes from the
 * {@link EnchantmentTags#CURSE} tag of the dynamic enchantment registry, and the stack is edited
 * through {@link EnchantmentHelper#updateEnchantments}.</p>
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Shadow
    private net.minecraft.world.Container enchantSlots;

    
    @Inject(
            method = "clickMenuButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;getItem(I)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            )
    )
    private void addCurses(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        ItemStack lapis = this.enchantSlots.getItem(1);
        if (!(lapis.getItem() instanceof InfestedLapisLazuli)) return;

        
        ItemStack enchantedItem = this.enchantSlots.getItem(0);
        if (enchantedItem.isEmpty()) return;

        Optional<HolderSet.Named<Enchantment>> curseTag = player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getTag(EnchantmentTags.CURSE);
        if (curseTag.isEmpty()) return;

        List<Holder<Enchantment>> curses = new ArrayList<>();
        for (Holder<Enchantment> curse : curseTag.get()) {
            curses.add(curse);
        }

        if (curses.isEmpty()) return;

        List<Holder<Enchantment>> selected = new ArrayList<>();
        if (curses.size() == 1) {
            selected.add(curses.get(0));
        } else {
            Holder<Enchantment> first = curses.get(player.getRandom().nextInt(curses.size()));
            selected.add(first);
            Holder<Enchantment> second;
            do {
                second = curses.get(player.getRandom().nextInt(curses.size()));
            } while (second == first);
            selected.add(second);
        }

        EnchantmentHelper.updateEnchantments(enchantedItem, mutable -> {
            for (Holder<Enchantment> curse : selected) {
                if (mutable.getLevel(curse) <= 0) {
                    mutable.set(curse, 1);
                }
            }
        });
    }
}
