package org.tdddd.epca.impl.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.items.CustomTab;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Draws the special-rendering entries of an {@link CustomTab} on top of the creative inventory.
 *
 * <p>1.20.1 listened to the single {@code RenderGuiEvent.Post}; 1.21.1 renders the HUD through a
 * named {@link LayeredDraw} and fires {@code RenderGuiLayerEvent.Post} once per layer (the aggregate
 * {@code RenderGuiEvent} still exists, but a layer is the composable 1.21 way). Registering this as
 * a {@link LayeredDraw.Layer} above all others keeps the old "once, after the HUD" behaviour and
 * avoids drawing the entries once per vanilla layer.</p>
 */
@EventBusSubscriber(modid = epca.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class CustomTabRenderer implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof CreativeModeInventoryScreen creativeScreen)) {
            return;
        }

        
        CreativeModeTab selectedTab = CreativeModeInventoryScreen.selectedTab;
        if (!(selectedTab instanceof CustomTab customTab)) {
            return;
        }

        int mouseX = (int) mc.mouseHandler.xpos();
        int mouseY = (int) mc.mouseHandler.ypos();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);

        int leftPos = creativeScreen.width;
        int topPos = creativeScreen.height;

        int slotSize = 18;
        int startX = leftPos + 8;
        int startY = topPos + 8;

        for (var entry : customTab.renderedEntries.entrySet()) {
            int slotIndex = entry.getKey();
            CustomTab.ITabEntry tabEntry = entry.getValue();
            if (tabEntry.hasSpecialRendering()) {
                int col = slotIndex % 9;
                int row = slotIndex / 9;
                int x = startX + col * slotSize;
                int y = startY + row * slotSize;
                tabEntry.render(graphics, x, y, mouseX, mouseY, partialTick);
            }
        }
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(epca.MODID, "custom_tab_renderer"),
                new CustomTabRenderer());
    }
}
