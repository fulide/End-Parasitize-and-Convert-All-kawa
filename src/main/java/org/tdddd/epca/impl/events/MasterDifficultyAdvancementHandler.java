package org.tdddd.epca.impl.events;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.overworld.data.WorldDifficultyData;
import org.tdddd.epca.impl.overworld.difficulty.DifficultyLevel;
import org.tdddd.epca.impl.epca;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID)
public class MasterDifficultyAdvancementHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var server = player.getServer();
        if (server == null) return;

        
        boolean allowCommands = server.getWorldData().isAllowCommands();
        
        boolean isHardDifficulty = server.getLevel(player.level().dimension()).getDifficulty() == Difficulty.HARD;

        
        ServerLevel level = player.serverLevel();
        WorldDifficultyData difficultyData = WorldDifficultyData.get(level);
        boolean isMasterDifficulty = difficultyData.getDifficulty() == DifficultyLevel.MASTER;

        
        if (!allowCommands && isHardDifficulty && isMasterDifficulty) {
            
            AdvancementHolder advancement = server.getAdvancements().get(
                    ResourceLocation.fromNamespaceAndPath(epca.MODID, "master_difficulty")
            );
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }
}