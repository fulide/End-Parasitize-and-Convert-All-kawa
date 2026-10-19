package org.tdddd.yawning_neko_api.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Loads {@code data/<namespace>/entity_adaptation_mappings/*.json}, mapping entity type ids to
 * damage adaptation config ids (optionally in {@code override} mode).
 */
public class EntityAdaptationMapping implements ResourceManagerReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, ResourceLocation> ENTITY_TO_CONFIG_MAP = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> ENTITY_OVERRIDE_MAP = new HashMap<>();

    public EntityAdaptationMapping() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ENTITY_TO_CONFIG_MAP.clear();
        ENTITY_OVERRIDE_MAP.clear();
        resourceManager.listResources("entity_adaptation_mappings", file -> file.getPath().endsWith(".json"))
                .forEach((resourceLocation, resource) -> {
                    try (InputStream stream = resource.open()) {
                        JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                        if (json == null) {
                            return;
                        }
                        String sourceNamespace = resourceLocation.getNamespace();
                        this.parseMappingsFromJson(json, sourceNamespace);
                    } catch (Exception ignored) {
                        // Matches the original loader.
                    }
                });
    }

    private void parseMappingsFromJson(JsonObject jsonObject, String sourceNamespace) {
        for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String entityKey = entry.getKey();
            if (!entityKey.equals("id") && !entityKey.startsWith("_")) {
                JsonElement configElement = entry.getValue();
                ResourceLocation entityId = ResourceLocation.tryParse(entityKey);
                if (entityId != null) {
                    String configName;
                    if (configElement.isJsonPrimitive() && configElement.getAsJsonPrimitive().isString()) {
                        configName = configElement.getAsString();
                    } else {
                        if (!configElement.isJsonObject()) {
                            continue;
                        }

                        JsonElement nestedConfig = configElement.getAsJsonObject().get("config");
                        if (nestedConfig == null || !nestedConfig.isJsonPrimitive()) {
                            continue;
                        }

                        configName = nestedConfig.getAsString();
                        JsonElement modeElement = configElement.getAsJsonObject().get("mode");
                        if (modeElement != null && modeElement.isJsonPrimitive()) {
                            ENTITY_OVERRIDE_MAP.put(
                                    entityId, "override".equalsIgnoreCase(modeElement.getAsString()));
                        }
                    }

                    ResourceLocation configId;
                    if (configName.contains(":")) {
                        configId = ResourceLocation.tryParse(configName);
                    } else {
                        configId = ResourceLocation.fromNamespaceAndPath(sourceNamespace, configName);
                    }

                    if (configId != null) {
                        ENTITY_TO_CONFIG_MAP.put(entityId, configId);
                    }
                }
            }
        }
    }

    public static ResourceLocation getConfigForEntity(ResourceLocation entityId) {
        return ENTITY_TO_CONFIG_MAP.get(entityId);
    }

    public static boolean hasMapping(ResourceLocation entityId) {
        return ENTITY_TO_CONFIG_MAP.containsKey(entityId);
    }

    public static boolean isOverride(ResourceLocation entityId) {
        return ENTITY_OVERRIDE_MAP.getOrDefault(entityId, Boolean.FALSE);
    }

    public static void forgetOverride(ResourceLocation entityId) {
        ENTITY_OVERRIDE_MAP.remove(entityId);
    }
}
