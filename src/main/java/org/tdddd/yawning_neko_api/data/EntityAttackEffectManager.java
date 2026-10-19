package org.tdddd.yawning_neko_api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Mob;

/**
 * Loads {@code entity_attack_effects/*.json}: base mob effects applied on attack plus per-level
 * upgrades.
 */
public class EntityAttackEffectManager implements ResourceManagerReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, EntityAttackConfig> ENTITY_ATTACK_CONFIGS = new HashMap<>();
    private static final Map<String, Map<String, Integer>> EFFECT_LEVEL_CACHE = new HashMap<>();

    public EntityAttackEffectManager() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ENTITY_ATTACK_CONFIGS.clear();
        EFFECT_LEVEL_CACHE.clear();
        resourceManager.listResources("entity_attack_effects", file -> file.getPath().endsWith(".json"))
                .forEach((resourceLocation, resource) -> {
                    try (InputStream stream = resource.open()) {
                        JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                        if (json != null) {
                            this.parseAttackConfig(json);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load entity attack effect data: " + resourceLocation);
                        e.printStackTrace();
                    }
                });
    }

    private void parseAttackConfig(JsonObject json) {
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            String entityId = entry.getKey();
            JsonElement configElement = entry.getValue();
            if (configElement.isJsonObject()) {
                JsonObject configObj = configElement.getAsJsonObject();
                List<AttackEffect> baseEffects = new ArrayList<>();
                if (configObj.has("base_effects") && configObj.get("base_effects").isJsonArray()) {
                    for (JsonElement effectElement : configObj.get("base_effects").getAsJsonArray()) {
                        if (effectElement.isJsonObject()) {
                            JsonObject effectObj = effectElement.getAsJsonObject();
                            AttackEffect effect = new AttackEffect(
                                    effectObj.get("effect").getAsString(),
                                    effectObj.get("duration").getAsInt(),
                                    effectObj.get("amplifier").getAsInt(),
                                    effectObj.get("ambient").getAsBoolean(),
                                    effectObj.has("visibleParticles")
                                            ? effectObj.get("visibleParticles").getAsBoolean()
                                            : true,
                                    effectObj.has("showIcon") ? effectObj.get("showIcon").getAsBoolean() : true,
                                    effectObj.has("probability")
                                            ? effectObj.get("probability").getAsFloat()
                                            : 1.0F);
                            baseEffects.add(effect);
                        } else if (effectElement.isJsonArray()) {
                            JsonArray effectArray = effectElement.getAsJsonArray();
                            if (effectArray.size() >= 6) {
                                AttackEffect effect = new AttackEffect(
                                        effectArray.get(0).getAsString(),
                                        effectArray.get(1).getAsInt(),
                                        effectArray.get(2).getAsInt(),
                                        effectArray.get(3).getAsBoolean(),
                                        effectArray.get(4).getAsBoolean(),
                                        effectArray.get(5).getAsBoolean(),
                                        effectArray.size() > 6 ? effectArray.get(6).getAsFloat() : 1.0F);
                                baseEffects.add(effect);
                            }
                        }
                    }
                }

                List<EffectUpgrade> upgrades = new ArrayList<>();
                if (configObj.has("upgrades") && configObj.get("upgrades").isJsonArray()) {
                    for (JsonElement upgradeElement : configObj.getAsJsonArray("upgrades")) {
                        if (upgradeElement.isJsonObject()) {
                            JsonObject upgradeObj = upgradeElement.getAsJsonObject();
                            String effectFilter = upgradeObj.has("effect_filter")
                                    ? upgradeObj.get("effect_filter").getAsString()
                                    : null;
                            Integer maxLevel =
                                    upgradeObj.has("max_level") ? upgradeObj.get("max_level").getAsInt() : null;
                            EffectUpgrade upgrade = new EffectUpgrade(
                                    upgradeObj.get("level").getAsInt(),
                                    upgradeObj.has("probability_multiplier")
                                            ? upgradeObj.get("probability_multiplier").getAsFloat()
                                            : 1.0F,
                                    upgradeObj.has("level_adjust")
                                            ? upgradeObj.get("level_adjust").getAsInt()
                                            : 0,
                                    upgradeObj.has("duration_multiplier")
                                            ? upgradeObj.get("duration_multiplier").getAsFloat()
                                            : 1.0F,
                                    effectFilter,
                                    maxLevel);
                            upgrades.add(upgrade);
                        }
                    }
                }

                EntityAttackConfig config = new EntityAttackConfig(baseEffects, upgrades);
                ENTITY_ATTACK_CONFIGS.put(entityId, config);
            }
        }
    }

    public static int getCachedEffectLevel(String entityId, String effectId) {
        return EFFECT_LEVEL_CACHE.getOrDefault(entityId, new HashMap<>()).getOrDefault(effectId, 0);
    }

    public static void setCachedEffectLevel(String entityId, String effectId, int level) {
        EFFECT_LEVEL_CACHE.computeIfAbsent(entityId, k -> new HashMap<>()).put(effectId, level);
    }

    public static void clearEntityCache(String entityId) {
        EFFECT_LEVEL_CACHE.remove(entityId);
    }

    private static String entityId(Mob entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id == null ? "" : id.toString();
    }

    public static int getMaxUpgradeLevel(Mob entity) {
        EntityAttackConfig config = ENTITY_ATTACK_CONFIGS.get(entityId(entity));
        return config != null ? config.getMaxUpgradeLevel() : 0;
    }

    public static int getMaxEffectLevel(Mob entity, String effectId) {
        EntityAttackConfig config = ENTITY_ATTACK_CONFIGS.get(entityId(entity));
        return config != null ? config.getMaxEffectLevel(effectId) : 0;
    }

    public static List<AttackEffect> getAttackEffects(Mob entity, int upgradeLevel) {
        return getAttackEffects(entityId(entity), upgradeLevel);
    }

    public static List<AttackEffect> getAttackEffects(String entityId, int upgradeLevel) {
        EntityAttackConfig config = ENTITY_ATTACK_CONFIGS.get(entityId);
        return config == null ? new ArrayList<>() : config.getEffectsForLevel(upgradeLevel, entityId);
    }

    public static List<AttackEffect> getBaseAttackEffects(Mob entity) {
        EntityAttackConfig config = ENTITY_ATTACK_CONFIGS.get(entityId(entity));
        return config != null ? config.getBaseEffects() : new ArrayList<>();
    }

    public static boolean hasAttackEffects(Mob entity) {
        return ENTITY_ATTACK_CONFIGS.containsKey(entityId(entity));
    }

    public static Map<String, EntityAttackConfig> getAllAttackConfigs() {
        return new HashMap<>(ENTITY_ATTACK_CONFIGS);
    }

    public static class AttackEffect {
        private final String effectId;
        private final int duration;
        private final int amplifier;
        private final boolean ambient;
        private final boolean visibleParticles;
        private final boolean showIcon;
        private final float probability;

        public AttackEffect(
                String effectId,
                int duration,
                int amplifier,
                boolean ambient,
                boolean visibleParticles,
                boolean showIcon) {
            this(effectId, duration, amplifier, ambient, visibleParticles, showIcon, 1.0F);
        }

        public AttackEffect(
                String effectId,
                int duration,
                int amplifier,
                boolean ambient,
                boolean visibleParticles,
                boolean showIcon,
                float probability) {
            this.effectId = effectId;
            this.duration = duration;
            this.amplifier = amplifier;
            this.ambient = ambient;
            this.visibleParticles = visibleParticles;
            this.showIcon = showIcon;
            this.probability = probability;
        }

        public String getEffectId() {
            return this.effectId;
        }

        public int getDuration() {
            return this.duration;
        }

        public int getAmplifier() {
            return this.amplifier;
        }

        public boolean isAmbient() {
            return this.ambient;
        }

        public boolean hasVisibleParticles() {
            return this.visibleParticles;
        }

        public boolean shouldShowIcon() {
            return this.showIcon;
        }

        public float getProbability() {
            return this.probability;
        }

        public AttackEffect applyUpgrade(EffectUpgrade upgrade) {
            if (!upgrade.appliesToEffect(this.effectId)) {
                return this;
            } else {
                int newDuration = (int) ((float) this.duration * upgrade.durationMultiplier);
                int newAmplifier = this.amplifier + upgrade.levelAdjust;
                if (upgrade.maxLevel != null && newAmplifier > upgrade.maxLevel) {
                    newAmplifier = upgrade.maxLevel;
                }

                float newProbability = this.probability * upgrade.probabilityMultiplier;
                return new AttackEffect(
                        this.effectId,
                        newDuration,
                        newAmplifier,
                        this.ambient,
                        this.visibleParticles,
                        this.showIcon,
                        Math.min(newProbability, 1.0F));
            }
        }
    }

    public static class EffectUpgrade {
        public final int level;
        public final float probabilityMultiplier;
        public final int levelAdjust;
        public final float durationMultiplier;
        public final String effectFilter;
        public final Integer maxLevel;

        public EffectUpgrade(
                int level,
                float probabilityMultiplier,
                int levelAdjust,
                float durationMultiplier,
                String effectFilter,
                Integer maxLevel) {
            this.level = level;
            this.probabilityMultiplier = probabilityMultiplier;
            this.levelAdjust = levelAdjust;
            this.durationMultiplier = durationMultiplier;
            this.effectFilter = effectFilter;
            this.maxLevel = maxLevel;
        }

        public boolean appliesToEffect(String effectId) {
            return this.effectFilter == null || this.effectFilter.equals(effectId);
        }
    }

    public static class EntityAttackConfig {
        private final List<AttackEffect> baseEffects;
        private final List<EffectUpgrade> upgrades;

        public EntityAttackConfig(List<AttackEffect> baseEffects, List<EffectUpgrade> upgrades) {
            this.baseEffects = baseEffects;
            this.upgrades = upgrades;
        }

        public List<AttackEffect> getEffectsForLevel(int upgradeLevel, String entityId) {
            List<AttackEffect> result = new ArrayList<>();

            for (AttackEffect baseEffect : this.baseEffects) {
                AttackEffect upgradedEffect = baseEffect;
                String effectId = baseEffect.getEffectId();
                int effectiveLevel = EntityAttackEffectManager.getCachedEffectLevel(entityId, effectId);
                if (effectiveLevel == 0) {
                    effectiveLevel = upgradeLevel;
                    EntityAttackEffectManager.setCachedEffectLevel(entityId, effectId, upgradeLevel);
                }

                for (EffectUpgrade upgrade : this.upgrades) {
                    if (upgrade.level <= effectiveLevel && upgrade.appliesToEffect(effectId)) {
                        upgradedEffect = upgradedEffect.applyUpgrade(upgrade);
                    }
                }

                result.add(upgradedEffect);
            }

            return result;
        }

        public static List<AttackEffect> getAttackEffects(Mob entity, int upgradeLevel) {
            String entityId = EntityAttackEffectManager.entityId(entity);
            EntityAttackConfig config = EntityAttackEffectManager.ENTITY_ATTACK_CONFIGS.get(entityId);
            return config == null ? new ArrayList<>() : config.getEffectsForLevel(upgradeLevel, entityId);
        }

        public List<AttackEffect> getBaseEffects() {
            return new ArrayList<>(this.baseEffects);
        }

        public int getMaxUpgradeLevel() {
            return this.upgrades.stream().mapToInt(upgrade -> upgrade.level).max().orElse(0);
        }

        public int getMaxEffectLevel(String effectId) {
            return this.upgrades
                    .stream()
                    .filter(upgrade -> upgrade.appliesToEffect(effectId) && upgrade.maxLevel != null)
                    .mapToInt(upgrade -> upgrade.maxLevel)
                    .max()
                    .orElse(0);
        }
    }
}
