package org.tdddd.yawning_neko_api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Mob;

/**
 * Loads {@code entity_effect_upgrades/*.json}: random upgrade levels and per-effect level-up
 * chances. The computed level is stored in the vanilla persistent data tag
 * {@code AttackUpgradeLevel} (no capability was involved in 1.20.1 either).
 */
public class EntityUpgradeManager implements ResourceManagerReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Random RANDOM = new Random();
    private static final Map<String, UpgradeConfig> ENTITY_UPGRADE_CONFIGS = new HashMap<>();
    private static final Map<String, Map<String, Integer>> EFFECT_SPECIFIC_LEVELS = new HashMap<>();

    public EntityUpgradeManager() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ENTITY_UPGRADE_CONFIGS.clear();
        EFFECT_SPECIFIC_LEVELS.clear();
        resourceManager.listResources("entity_effect_upgrades", file -> file.getPath().endsWith(".json"))
                .forEach((resourceLocation, resource) -> {
                    try (InputStream stream = resource.open()) {
                        JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                        if (json != null) {
                            this.parseGlobalUpgradeConfig(json);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load entity upgrade data: " + resourceLocation);
                        e.printStackTrace();
                    }
                });
    }

    private void parseGlobalUpgradeConfig(JsonObject json) {
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            String entityId = entry.getKey();
            JsonElement configElement = entry.getValue();
            if (configElement.isJsonObject()) {
                JsonObject configObj = configElement.getAsJsonObject();
                UpgradeConfig upgradeConfig = this.parseSingleUpgradeConfig(configObj);
                ENTITY_UPGRADE_CONFIGS.put(entityId, upgradeConfig);
            }
        }
    }

    private UpgradeConfig parseSingleUpgradeConfig(JsonObject configObj) {
        Map<String, Float> levelUpChances = new HashMap<>();
        if (configObj.has("level_up_chances") && configObj.get("level_up_chances").isJsonObject()) {
            JsonObject chancesObj = configObj.get("level_up_chances").getAsJsonObject();

            for (Entry<String, JsonElement> entry : chancesObj.entrySet()) {
                levelUpChances.put(entry.getKey(), entry.getValue().getAsFloat());
            }
        }

        return new UpgradeConfig(
                configObj.has("min_level") ? configObj.get("min_level").getAsInt() : 0,
                configObj.has("max_level") ? configObj.get("max_level").getAsInt() : 0,
                configObj.has("default_level") ? configObj.get("default_level").getAsInt() : 0,
                configObj.has("random_chance") ? configObj.get("random_chance").getAsFloat() : 0.0F,
                configObj.has("level_conditions")
                        ? this.parseConditions(configObj.get("level_conditions").getAsJsonObject())
                        : new LevelConditions(),
                levelUpChances);
    }

    private LevelConditions parseConditions(JsonObject conditionsObj) {
        return new LevelConditions(
                conditionsObj.has("difficulty") ? conditionsObj.get("difficulty").getAsString() : "any",
                conditionsObj.has("biome") ? conditionsObj.get("biome").getAsString() : "any",
                conditionsObj.has("time") ? conditionsObj.get("time").getAsString() : "any",
                conditionsObj.has("moon_phase") ? conditionsObj.get("moon_phase").getAsInt() : -1);
    }

    private static String entityId(Mob entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id == null ? "" : id.toString();
    }

    public static void setupEntityUpgradeLevel(Mob entity) {
        UpgradeConfig config = ENTITY_UPGRADE_CONFIGS.get(entityId(entity));
        if (config != null) {
            int level = calculateUpgradeLevel(entity, config);
            entity.getPersistentData().putInt("AttackUpgradeLevel", level);
        } else {
            entity.getPersistentData().putInt("AttackUpgradeLevel", 0);
        }
    }

    private static int calculateUpgradeLevel(Mob entity, UpgradeConfig config) {
        if (config.randomChance > 0.0F && RANDOM.nextFloat() < config.randomChance) {
            return RANDOM.nextInt(config.maxLevel - config.minLevel + 1) + config.minLevel;
        } else {
            return checkLevelConditions(entity, config.levelConditions) ? config.defaultLevel : config.minLevel;
        }
    }

    public static int getEffectSpecificLevel(Mob entity, String effectId) {
        return EFFECT_SPECIFIC_LEVELS.getOrDefault(entityId(entity), new HashMap<>()).getOrDefault(effectId, 0);
    }

    public static void setEffectSpecificLevel(Mob entity, String effectId, int level) {
        String id = entityId(entity);
        EFFECT_SPECIFIC_LEVELS.computeIfAbsent(id, k -> new HashMap<>()).put(effectId, level);
        EntityAttackEffectManager.setCachedEffectLevel(id, effectId, level);
    }

    public static void increaseEffectSpecificLevel(Mob entity, String effectId, int maxLevel) {
        int currentLevel = getEffectSpecificLevel(entity, effectId);
        if (currentLevel < maxLevel) {
            setEffectSpecificLevel(entity, effectId, currentLevel + 1);
        }
    }

    private static boolean checkLevelConditions(Mob entity, LevelConditions conditions) {
        // Original 1.20.1 implementation always returned true; conditions were parsed but unused.
        return true;
    }

    public static int getEntityUpgradeLevel(Mob entity) {
        return entity.getPersistentData().getInt("AttackUpgradeLevel");
    }

    public static UpgradeConfig getUpgradeConfig(String entityId) {
        return ENTITY_UPGRADE_CONFIGS.get(entityId);
    }

    public static void setEntityUpgradeLevel(Mob entity, int level) {
        entity.getPersistentData().putInt("AttackUpgradeLevel", level);
    }

    public static class LevelConditions {
        public final String difficulty;
        public final String biome;
        public final String time;
        public final int moonPhase;

        public LevelConditions() {
            this("any", "any", "any", -1);
        }

        public LevelConditions(String difficulty, String biome, String time, int moonPhase) {
            this.difficulty = difficulty;
            this.biome = biome;
            this.time = time;
            this.moonPhase = moonPhase;
        }
    }

    public static class UpgradeConfig {
        public final int minLevel;
        public final int maxLevel;
        public final int defaultLevel;
        public final float randomChance;
        public final LevelConditions levelConditions;
        public final Map<String, Float> levelUpChances;

        public UpgradeConfig(
                int minLevel,
                int maxLevel,
                int defaultLevel,
                float randomChance,
                LevelConditions levelConditions,
                Map<String, Float> levelUpChances) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.defaultLevel = defaultLevel;
            this.randomChance = randomChance;
            this.levelConditions = levelConditions;
            this.levelUpChances = levelUpChances;
        }
    }
}
