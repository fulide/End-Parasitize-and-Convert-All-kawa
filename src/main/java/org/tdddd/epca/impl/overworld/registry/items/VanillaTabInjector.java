package org.tdddd.epca.impl.overworld.registry.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModItems;

import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Injects the copper nugget into the vanilla ingredients tab, directly behind the iron nugget.
 *
 * <p><b>1.21.1 change:</b> {@code BuildCreativeModeTabContentsEvent#getEntries()} (a mutable sorted map) is gone. The
 * event now exposes immutable queries ({@code getParentEntries()}/{@code getSearchEntries()}) plus positional mutators
 * ({@code insertAfter}, {@code insertFirst}, {@code accept}, {@code remove}), so the old "snapshot, clear, re-add"
 * dance is no longer needed (and no longer possible).</p>
 *
 * <p><b>Note on the bus:</b> {@code BuildCreativeModeTabContentsEvent} is an {@code IModBusEvent} (NeoForge posts it
 * through {@code ModLoader.postEvent}), while {@code @EventBusSubscriber} without {@code bus =} listens on the GAME bus.
 * The registration is left exactly as it was ported (game bus), i.e. the handler does not run - which matches the
 * 1.20.1 behaviour. Add {@code bus = EventBusSubscriber.Bus.MOD} if the copper nugget is actually meant to show up in
 * the vanilla Ingredients tab. See the port report.</p>
 */
@EventBusSubscriber(modid = epca.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class VanillaTabInjector {

    private VanillaTabInjector() {}

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) return;

        ItemStack copper = new ItemStack(ModItems.COPPER_NUGGET.get());
        if (event.getParentEntries().stream().anyMatch(entry -> ItemStack.isSameItemSameComponents(entry, copper))) {
            return;
        }

        ItemStack ironNugget = new ItemStack(Items.IRON_NUGGET);
        boolean hasIronNugget = event.getParentEntries().stream()
                .anyMatch(entry -> ItemStack.isSameItemSameComponents(entry, ironNugget));

        if (hasIronNugget) {
            event.insertAfter(ironNugget, copper, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else {
            event.accept(copper, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
