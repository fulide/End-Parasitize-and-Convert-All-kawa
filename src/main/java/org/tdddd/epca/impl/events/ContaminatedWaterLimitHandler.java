package org.tdddd.epca.impl.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.entities.entity.misc.ContaminatedWater;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID)
public class ContaminatedWaterLimitHandler {
    private static final int MAX_CONTAMINATED_WATER = 24;
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof ContaminatedWater)) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        int existingCount = 0;
        for (Entity e : level.getEntities().getAll()) {
            // only count live ones: discarded entities stay in the level's entity manager for a
            // while, and counting them used to lock the cap permanently once it was hit
            if (e instanceof ContaminatedWater && e != entity && !e.isRemoved()) {
                existingCount++;
            }
        }

        if (existingCount >= org.tdddd.epca.impl.ModConfig.getContaminatedWaterLimit()) {
            // Cancel the join: entity.discard() alone still lets ServerLevel.addEntity register the
            // entity (and potentially send it to clients), which is how ghost copies piled up.
            event.setCanceled(true);
            entity.discard();
        }
    }
}