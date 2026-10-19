package org.tdddd.epca.impl.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tdddd.epca.impl.overworld.registry.items.EnchantingFuelSlot;
import org.tdddd.epca.impl.overworld.registry.items.item.InfestedLapisLazuli;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enchanting-table integration for {@code epca:infested_lapis_lazuli}.
 *
 * <p>Two independent things are patched here:</p>
 * <ol>
 *   <li><b>Fuel slot acceptance.</b> 1.21.1 vanilla hard-codes the lapis slot to
 *       {@code stack.is(Items.LAPIS_LAZULI)} inside an anonymous {@link Slot} subclass, so a modded
 *       lapis-like item can never be placed there. After the menu is constructed we swap that slot
 *       for an {@link EnchantingFuelSlot}, which also accepts the {@code c:enchanting_fuels} tag and
 *       the infested lapis item itself. Everything else in the menu (cost calculation, consumption
 *       in {@code clickMenuButton}, {@code getGoldCount()}) only counts the stack, so no further
 *       hook is needed for the item to be spent exactly like lapis.</li>
 *   <li><b>Curses.</b> Using the table with infested lapis adds up to two random curses.</li>
 * </ol>
 *
 * <p>1.21.1 port note: enchantments are data-driven {@code Holder<Enchantment>} values stored in the
 * {@code minecraft:enchantments} component. {@code EnchantmentHelper.getEnchantments/setEnchantments}
 * and {@code BuiltInRegistries.ENCHANTMENT} are gone; the curse pool now comes from the
 * {@link EnchantmentTags#CURSE} tag of the dynamic enchantment registry, and the stack is edited
 * through {@link EnchantmentHelper#updateEnchantments}.</p>
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    /** Replaces the vanilla lapis-only slot with one that also accepts modded enchanting fuel. */
    @Inject(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL")
    )
    private void epca$acceptModdedEnchantingFuel(CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (menu.slots.size() <= 1) {
            return;
        }
        Slot vanillaLapisSlot = menu.slots.get(1);
        if (vanillaLapisSlot instanceof EnchantingFuelSlot) {
            return;
        }
        menu.slots.set(1, new EnchantingFuelSlot(vanillaLapisSlot.container, 1, 35, 47));
    }

    /**
     * Shift-click routing: vanilla sends "lapis" to the fuel slot and everything else to the item
     * slot, so that single comparison has to learn about modded fuel as well. {@code require = 0}
     * keeps a future vanilla change from turning this into a hard crash (shift-click then simply
     * falls back to vanilla behaviour).
     */
    @Redirect(
            method = "quickMoveStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
            ),
            require = 0
    )
    private boolean epca$countModdedFuelAsLapis(ItemStack stack, Item item) {
        return stack.is(item) || EnchantingFuelSlot.isFuel(stack);
    }

    /** Adds up to two random curses when the enchantment is bought with infested lapis. */
    @Inject(
            method = "clickMenuButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;getItem(I)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            )
    )
    private void addCurses(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        ItemStack lapis = menu.slots.get(1).getItem();
        if (!(lapis.getItem() instanceof InfestedLapisLazuli)) return;

        ItemStack enchantedItem = menu.slots.get(0).getItem();
        if (enchantedItem.isEmpty()) return;

        Optional<HolderSet.Named<Enchantment>> curseTag = player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getTag(EnchantmentTags.CURSE);
        if (curseTag.isEmpty()) return;

        List<Holder<Enchantment>> curses = new ArrayList<>(curseTag.get().stream().toList());
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
