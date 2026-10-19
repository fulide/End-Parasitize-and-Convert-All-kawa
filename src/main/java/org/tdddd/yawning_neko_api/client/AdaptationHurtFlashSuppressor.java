package org.tdddd.yawning_neko_api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.tdddd.yawning_neko_api.data.AdaptationColorConfig;
import org.tdddd.yawning_neko_api.data.DamageAdaptation;

/**
 * Suppresses the vanilla red hurt flash for entities that are currently showing an adaptation tint.
 *
 * <p>Port of the original client tick handler ({@code ClientTickEvent} phase END →
 * {@link ClientTickEvent.Post}).</p>
 */
public final class AdaptationHurtFlashSuppressor {
    private AdaptationHurtFlashSuppressor() {
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living) {
                if (living.hurtTime > 0
                        && AdaptationColorConfig.applies(living)
                        && DamageAdaptation.getActiveAdaptationEvent(living) >= 0) {
                    living.hurtTime = 0;
                    living.hurtDuration = 0;
                }
            }
        }
    }
}
