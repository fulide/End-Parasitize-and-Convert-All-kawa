package org.tdddd.yawning_neko_api.data;

import java.util.Map.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Resolves which {@link DamageAdaptationConfig} applies to a living entity, combining the entity
 * mapping, priority rules and the base per-entity config.
 */
public class AdaptationConfigResolver {
    public AdaptationConfigResolver() {
    }

    public static ResourceLocation resolveConfigId(LivingEntity entity, Level level) {
        ResourceLocation ruleId = AdaptationPriorityRuleLoader.resolveConfigId(entity, level);
        if (ruleId != null) {
            return ruleId;
        } else {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (entityId == null) {
                return null;
            } else {
                ResourceLocation mappedId = EntityAdaptationMapping.getConfigForEntity(entityId);
                if (mappedId != null) {
                    return mappedId;
                } else {
                    return DamageAdaptationManager.hasConfig(entityId) ? entityId : null;
                }
            }
        }
    }

    public static DamageAdaptationConfig getConfig(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return null;
        } else {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            ResourceLocation baseId = null;
            DamageAdaptationConfig base = null;
            if (entityId != null) {
                ResourceLocation mapped = EntityAdaptationMapping.getConfigForEntity(entityId);
                baseId = mapped != null ? mapped : entityId;
                base = DamageAdaptationManager.getDirectConfig(baseId);
            }

            ResourceLocation ruleId = AdaptationPriorityRuleLoader.resolveConfigId(entity, entity.level());
            DamageAdaptationConfig rule = ruleId == null ? null : DamageAdaptationManager.getDirectConfig(ruleId);
            DamageAdaptationConfig resolved;
            String signature;
            if (rule == null) {
                resolved = base;
                signature = baseId == null ? null : baseId.toString();
            } else if (base == null) {
                resolved = rule;
                signature = ruleId.toString();
            } else if (entityId != null && EntityAdaptationMapping.isOverride(entityId)) {
                resolved = rule;
                signature = ruleId + "|override|" + baseId;
            } else {
                resolved = DamageAdaptationConfig.additive(base, rule);
                signature = baseId + "|add|" + ruleId;
            }

            if (resolved != null && signature != null) {
                String newSignature = signature;
                IAdaptationData data = IAdaptationData.get(entity);
                if (!newSignature.equals(data.getCurrentConfigId())) {
                    data.clearAdaptations();
                    data.setDeathCount(0);
                    data.setBrokenAdaptationEndTick(0L);
                    data.setCurrentConfigId(newSignature);
                }
                return resolved;
            } else {
                return null;
            }
        }
    }

    public static void refreshAllEntitiesConfig() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> {
                for (ServerLevel serverLevel : server.getAllLevels()) {
                    for (Entity entity : serverLevel.getAllEntities()) {
                        if (entity instanceof LivingEntity living) {
                            refreshEntityConfig(living);
                        }
                    }
                }
            });
        }
    }

    public static void refreshEntityConfig(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            ResourceLocation newConfigId = resolveConfigId(entity, entity.level());
            if (newConfigId != null) {
                DamageAdaptationConfig newConfig = DamageAdaptationManager.getDirectConfig(newConfigId);
                if (newConfig != null) {
                    IAdaptationData data = IAdaptationData.get(entity);
                    String oldConfigId = data.getCurrentConfigId();
                    String newConfigIdStr = newConfigId.toString();
                    if (!newConfigIdStr.equals(oldConfigId)) {
                        data.clearAdaptations();
                        data.setDeathCount(0);
                        data.setBrokenAdaptationEndTick(0L);
                        data.setCurrentConfigId(newConfigIdStr);
                        data.setMaxAdaptations(newConfig.getMaxAdaptations());
                    } else {
                        int oldMax = data.getMaxAdaptations();
                        int newMax = newConfig.getMaxAdaptations();
                        if (oldMax != newMax) {
                            data.setMaxAdaptations(newMax);

                            for (Entry<String, Integer> entry : data.getAllAdaptations().entrySet()) {
                                if (entry.getValue() > newMax) {
                                    data.setAdaptationLevel(entry.getKey(), newMax);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static int resolveMinimumKillCountUpperBound(LivingEntity entity) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        DamageAdaptationConfig base = null;
        if (entityId != null) {
            ResourceLocation mapped = EntityAdaptationMapping.getConfigForEntity(entityId);
            ResourceLocation baseId = mapped != null ? mapped : entityId;
            base = DamageAdaptationManager.getDirectConfig(baseId);
        }

        // Original implementation only consulted the base config; the rule lookup was dead code.
        return base == null ? 0 : base.getMinimumKillCount();
    }
}
