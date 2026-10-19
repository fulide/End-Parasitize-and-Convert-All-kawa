package org.tdddd.yawning_neko_api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.tdddd.yawning_neko_api.data.DamageAdaptation;

/**
 * Client-only packet handling. Isolated in this package so dedicated servers never resolve
 * {@link Minecraft}.
 */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void handleAdaptationEvent(int entityId, int eventType) {
        Minecraft minecraft = Minecraft.getInstance();
        DamageAdaptation.updateClientAdaptationEventById(entityId, eventType, minecraft.level);
        if (minecraft.level != null && minecraft.level.getEntity(entityId) instanceof LivingEntity living) {
            DamageAdaptation.updateClientAdaptationEvent(living, eventType);
        }
    }

    public static void registerClientEvents() {
        NeoForge.EVENT_BUS.addListener(AdaptationHurtFlashSuppressor::onClientTick);
    }
}
