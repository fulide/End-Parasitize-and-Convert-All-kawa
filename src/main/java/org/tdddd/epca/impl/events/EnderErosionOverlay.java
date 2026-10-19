package org.tdddd.epca.impl.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModEffects;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Full-screen green tint shown while {@code epca:ender_erosion} is active.
 *
 * <p>1.21.1 port notes: {@code IGuiOverlay}/{@code ForgeGui}/{@code RegisterGuiOverlaysEvent} were
 * removed. Overlays are now {@link LayeredDraw.Layer}s registered on a
 * {@link RegisterGuiLayersEvent} (a <b>mod-bus</b> event, hence {@code bus = MOD}), and a layer only
 * receives the {@link GuiGraphics} plus a {@link DeltaTracker} - the screen size comes from the
 * graphics object.</p>
 */
@EventBusSubscriber(modid = epca.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class EnderErosionOverlay implements LayeredDraw.Layer {

    private static final ResourceLocation EROSION_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(epca.MODID, "textures/gui/ender_erosion.png");

    private static final float MIN_BRIGHTNESS = 0.85f;
    private static final float MAX_BRIGHTNESS = 1.0f;
    private static final float CYCLE_SECONDS = 0.8f;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        MobEffectInstance effect = player.getEffect(ModEffects.ENDER_EROSION);
        if (effect == null) return;

        long now = System.currentTimeMillis();
        float progress = (now % (long) (CYCLE_SECONDS * 1000)) / (CYCLE_SECONDS * 1000f);
        float brightness = (float) (MIN_BRIGHTNESS + (MAX_BRIGHTNESS - MIN_BRIGHTNESS) *
                (0.5 + 0.5 * Math.cos(2 * Math.PI * progress)));

        RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        guiGraphics.blit(EROSION_TEXTURE, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.9f);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(epca.MODID, "ender_erosion"),
                new EnderErosionOverlay());
    }
}
