package org.tdddd.epca.impl.client.render.sky;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * <b></b>
 *
 * <p> Forge  {@code RenderLevelStageEvent.Stage.AFTER_SKY}
 * {@code LevelRenderer.renderLevel()}
 * {@code renderSky}  438AFTER_SKY  441
 *  {@code renderChunkLayer}  557</p>
 *
 * <p> {@link SkyRuptureRenderer}
 *
 * <b></b>
 * </p>
 */
@EventBusSubscriber(modid = epca.MODID, value = Dist.CLIENT)
public final class SkyRuptureStageListener {

    private SkyRuptureStageListener() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            SkyRuptureRenderer.renderAfterSky();
        }
    }
}

