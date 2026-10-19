package org.tdddd.yawning_neko_api.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Loads {@code adaptation_rules/*.json} and the named {@code entity_adaptation_mappings/*.json}
 * tables used by those rules.
 *
 * <p>Rules are sorted by ascending priority and evaluated in that order. Rules sharing a priority
 * must not be able to match the same entity; a conflict aborts the reload exactly like the original
 * implementation.</p>
 */
public class AdaptationPriorityRuleLoader implements ResourceManagerReloadListener {
    private static final Gson GSON = new Gson();
    private static final List<PriorityRule> RULES = new ArrayList<>();
    private static final Map<ResourceLocation, Map<ResourceLocation, ResourceLocation>> MAPPING_CACHE =
            new HashMap<>();

    public AdaptationPriorityRuleLoader() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        RULES.clear();
        MAPPING_CACHE.clear();
        resourceManager.listResources("adaptation_rules", path -> path.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.open();
                            InputStreamReader reader = new InputStreamReader(stream)) {
                        JsonObject root = GSON.fromJson(reader, JsonObject.class);
                        if (root == null) {
                            return;
                        }
                        if (root.has("rules") && root.get("rules").isJsonArray()) {
                            for (JsonElement elem : root.getAsJsonArray("rules")) {
                                PriorityRule rule = PriorityRule.fromJson(elem.getAsJsonObject());
                                if (rule != null) {
                                    RULES.add(rule);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        // Matches the original loader.
                    }
                });
        resourceManager.listResources("entity_adaptation_mappings", path -> path.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.open();
                            InputStreamReader reader = new InputStreamReader(stream)) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        if (json == null) {
                            return;
                        }
                        ResourceLocation mappingId = jsonId(json, id);
                        Map<ResourceLocation, ResourceLocation> entityToConfig = new HashMap<>();

                        for (Entry<String, JsonElement> entry : json.entrySet()) {
                            String entityKey = entry.getKey();
                            if (!entityKey.equals("id") && !entityKey.startsWith("_")) {
                                JsonElement configElement = entry.getValue();
                                ResourceLocation entityId = ResourceLocation.tryParse(entityKey);
                                if (entityId != null) {
                                    ResourceLocation configId = null;
                                    if (configElement.isJsonPrimitive()
                                            && configElement.getAsJsonPrimitive().isString()) {
                                        configId = ResourceLocation.tryParse(configElement.getAsString());
                                    } else if (configElement.isJsonObject()
                                            && configElement.getAsJsonObject().has("config")) {
                                        configId = ResourceLocation.tryParse(
                                                configElement.getAsJsonObject().get("config").getAsString());
                                    }

                                    if (configId != null) {
                                        entityToConfig.put(entityId, configId);
                                    }
                                }
                            }
                        }

                        if (!entityToConfig.isEmpty()) {
                            MAPPING_CACHE.put(mappingId, entityToConfig);
                        }
                    } catch (Exception ignored) {
                        // Matches the original loader.
                    }
                });
        RULES.sort(Comparator.comparingInt(r -> r.priority));
        checkConflicts();
    }

    private static void checkConflicts() {
        Map<Integer, List<PriorityRule>> groups = new HashMap<>();

        for (PriorityRule rule : RULES) {
            groups.computeIfAbsent(rule.priority, k -> new ArrayList<>()).add(rule);
        }

        for (List<PriorityRule> group : groups.values()) {
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    if (conditionsConflict(group.get(i).condition, group.get(j).condition)) {
                        throw new IllegalStateException(String.format(
                                "Conflict in priority %d: rules '%s' and '%s' can match the same entity",
                                group.get(i).priority,
                                group.get(i).configId != null
                                        ? group.get(i).configId
                                        : group.get(i).mappingId,
                                group.get(j).configId != null
                                        ? group.get(j).configId
                                        : group.get(j).mappingId));
                    }
                }
            }
        }
    }

    private static boolean conditionsConflict(Condition a, Condition b) {
        if (a != null && b != null) {
            Set<ResourceLocation> aTypes = a.entityTypes;
            Set<ResourceLocation> bTypes = b.entityTypes;
            if (aTypes != null && bTypes != null && Collections.disjoint(aTypes, bTypes)) {
                return false;
            } else {
                Set<ResourceKey<Level>> aDims = a.dimensions;
                Set<ResourceKey<Level>> bDims = b.dimensions;
                return aDims == null || bDims == null || !Collections.disjoint(aDims, bDims);
            }
        } else {
            return true;
        }
    }

    public static ResourceLocation resolveConfigId(LivingEntity entity, Level level) {
        for (PriorityRule rule : RULES) {
            if (rule.matches(entity, level)) {
                if (rule.mappingId != null) {
                    Map<ResourceLocation, ResourceLocation> mapping = MAPPING_CACHE.get(rule.mappingId);
                    if (mapping != null) {
                        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                        ResourceLocation configId = mapping.get(entityId);
                        if (configId != null) {
                            return configId;
                        }
                    }
                } else if (rule.configId != null) {
                    return rule.configId;
                }
            }
        }

        return null;
    }

    private static ResourceLocation jsonId(JsonObject json, ResourceLocation file) {
        if (json.has("id") && json.get("id").isJsonPrimitive()) {
            ResourceLocation explicit = ResourceLocation.tryParse(json.get("id").getAsString());
            if (explicit != null) {
                return explicit;
            }
        }

        String path = file.getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
        return ResourceLocation.fromNamespaceAndPath(file.getNamespace(), fileName);
    }

    private static class Condition {
        final Set<ResourceLocation> entityTypes;
        final Set<ResourceKey<Level>> dimensions;
        final String namePattern;
        final Map<String, String> nbt;

        Condition(
                Set<ResourceLocation> entityTypes,
                Set<ResourceKey<Level>> dimensions,
                String namePattern,
                Map<String, String> nbt) {
            this.entityTypes = entityTypes;
            this.dimensions = dimensions;
            this.namePattern = namePattern;
            this.nbt = nbt;
        }

        boolean matches(LivingEntity entity, Level level) {
            if (this.entityTypes != null && !this.entityTypes.isEmpty()) {
                ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                if (!this.entityTypes.contains(typeId)) {
                    return false;
                }
            }

            if (this.dimensions != null && !this.dimensions.isEmpty()) {
                ResourceKey<Level> dimKey = level.dimension();
                if (!this.dimensions.contains(dimKey)) {
                    return false;
                }
            }

            if (this.namePattern != null && !this.namePattern.isEmpty()) {
                String customName = entity.getCustomName() != null ? entity.getCustomName().getString() : "";
                if (!Pattern.matches(this.namePattern, customName)) {
                    return false;
                }
            }

            if (this.nbt != null && !this.nbt.isEmpty()) {
                Map<String, String> actual = flattenNbt(entity.saveWithoutId(new CompoundTag()));

                for (Entry<String, String> entry : this.nbt.entrySet()) {
                    if (!valueMatches(actual.get(entry.getKey()), entry.getValue())) {
                        return false;
                    }
                }
            }

            return true;
        }

        private static Map<String, String> flattenNbt(Tag tag) {
            Map<String, String> out = new HashMap<>();
            flattenNbt(tag, "", out);
            return out;
        }

        private static void flattenNbt(Tag tag, String prefix, Map<String, String> out) {
            if (tag instanceof CompoundTag compound) {
                for (String key : compound.getAllKeys()) {
                    flattenNbt(compound.get(key), prefix.isEmpty() ? key : prefix + "." + key, out);
                }
            } else if (tag != null) {
                out.put(prefix, tag.getAsString());
            }
        }

        private static boolean valueMatches(String actual, String expected) {
            if (actual == null) {
                return false;
            } else if (actual.equals(expected)) {
                return true;
            } else {
                try {
                    return Double.compare(Double.parseDouble(actual), Double.parseDouble(expected)) == 0;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }

        static Condition fromJson(JsonObject obj) {
            Set<ResourceLocation> types = null;
            if (obj.has("entity_type")) {
                types = new HashSet<>();
                JsonElement elem = obj.get("entity_type");
                if (elem.isJsonArray()) {
                    for (JsonElement e : elem.getAsJsonArray()) {
                        types.add(ResourceLocation.tryParse(e.getAsString()));
                    }
                } else {
                    types.add(ResourceLocation.tryParse(elem.getAsString()));
                }
            }

            Set<ResourceKey<Level>> dims = null;
            if (obj.has("dimension")) {
                dims = new HashSet<>();
                JsonElement elem = obj.get("dimension");
                if (elem.isJsonArray()) {
                    for (JsonElement e : elem.getAsJsonArray()) {
                        dims.add(ResourceKey.create(
                                Registries.DIMENSION, ResourceLocation.tryParse(e.getAsString())));
                    }
                } else {
                    dims.add(ResourceKey.create(
                            Registries.DIMENSION, ResourceLocation.tryParse(elem.getAsString())));
                }
            }

            String namePattern = obj.has("name_pattern") ? obj.get("name_pattern").getAsString() : null;
            Map<String, String> nbt = null;
            if (obj.has("nbt") && obj.get("nbt").isJsonObject()) {
                nbt = new HashMap<>();

                for (Entry<String, JsonElement> e : obj.getAsJsonObject("nbt").entrySet()) {
                    if (e.getValue().isJsonPrimitive()) {
                        nbt.put(e.getKey(), e.getValue().getAsString());
                    }
                }
            }

            return new Condition(types, dims, namePattern, nbt);
        }
    }

    public static class PriorityRule {
        public final int priority;
        public final Condition condition;
        public final ResourceLocation configId;
        public final ResourceLocation mappingId;

        private PriorityRule(int priority, Condition condition, ResourceLocation configId, ResourceLocation mappingId) {
            this.priority = priority;
            this.condition = condition;
            this.configId = configId;
            this.mappingId = mappingId;
        }

        public boolean matches(LivingEntity entity, Level level) {
            return this.condition.matches(entity, level);
        }

        static PriorityRule fromJson(JsonObject obj) {
            int priority = obj.get("priority").getAsInt();
            JsonObject condObj = obj.getAsJsonObject("condition");
            Condition condition = Condition.fromJson(condObj);
            ResourceLocation configId = null;
            ResourceLocation mappingId = null;
            if (obj.has("type") && obj.has("target")) {
                ResourceLocation target = ResourceLocation.tryParse(obj.get("target").getAsString());
                if ("mapping".equalsIgnoreCase(obj.get("type").getAsString())) {
                    mappingId = target;
                } else {
                    configId = target;
                }
            }

            if (obj.has("config")) {
                configId = ResourceLocation.tryParse(obj.get("config").getAsString());
            }

            if (obj.has("mapping")) {
                mappingId = ResourceLocation.tryParse(obj.get("mapping").getAsString());
            }

            if (configId == null && mappingId == null) {
                throw new JsonSyntaxException("Rule must have either 'config' or 'mapping'");
            } else {
                return new PriorityRule(priority, condition, configId, mappingId);
            }
        }
    }
}
