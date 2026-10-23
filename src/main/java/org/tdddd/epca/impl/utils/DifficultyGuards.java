package org.tdddd.epca.impl.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.core.BlockPos;

/**
 * 让本模组的生物"像原版敌对生物一样守规矩"。
 *
 * <p>原版敌对生物在和平难度下不自然生成（{@code Monster#checkMonsterSpawnRules}），
 * 已经存在的会由 {@code Mob#checkDespawn} + {@code shouldDespawnInPeaceful()} 直接移除；
 * 而本模组大量使用自定义刷怪规则 / 方块实体刷怪器，绕过了这两道门，导致和平难度下照样冒虫。
 * 这里把同样的判定集中起来，供刷怪规则与刷怪器调用。</p>
 */
public final class DifficultyGuards {

    private DifficultyGuards() {
    }

    public static boolean isPeaceful(Level level) {
        return level != null && level.getDifficulty() == Difficulty.PEACEFUL;
    }

    public static boolean isPeaceful(ServerLevelAccessor accessor) {
        return accessor != null && isPeaceful(accessor.getLevel());
    }

    public static boolean isPeaceful(ServerLevel level) {
        return level != null && level.getDifficulty() == Difficulty.PEACEFUL;
    }

    /**
     * 生成敌对生物前的统一检查：和平难度不刷，{@code doMobSpawning} 关掉也不刷。
     * （原版刷怪笼/自然生成同样遵守这两条。）
     */
    public static boolean canSpawnHostiles(Level level) {
        return level != null
                && level.getDifficulty() != Difficulty.PEACEFUL
                && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
    }

    /** 把原版的"和平难度不刷怪"包到自定义 {@code SpawnPredicate} 外面。 */
    public static <T extends Entity> SpawnPlacements.SpawnPredicate<T> peacefulGuard(
            SpawnPlacements.SpawnPredicate<T> delegate) {
        return (EntityType<T> type, ServerLevelAccessor level, MobSpawnType spawnType,
                BlockPos pos, RandomSource random) ->
                !isPeaceful(level.getLevel())
                        && delegate.test(type, level, spawnType, pos, random);
    }
}
