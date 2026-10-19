package org.tdddd.yawning_neko_api.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Loads {@code data/<namespace>/damage_adaptation_configs/*.json} and keeps the resulting configs.
 */
public class DamageAdaptationManager implements ResourceManagerReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, DamageAdaptationConfig> CONFIGS = new HashMap<>();

    public DamageAdaptationManager() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        CONFIGS.clear();
        AdaptationColorConfig.reload(resourceManager);
        resourceManager.listResources("damage_adaptation_configs", file -> file.getPath().endsWith(".json"))
                .forEach((resourceLocation, resource) -> {
                    try (InputStream stream = resource.open()) {
                        JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                        if (json == null) {
                            return;
                        }
                        ResourceLocation configId = jsonId(json, resourceLocation);
                        DamageAdaptationConfig config = DamageAdaptationConfig.fromJson(json);
                        CONFIGS.put(configId, config);
                    } catch (Exception ignored) {
                        // Matches the original loader, which silently skipped broken files.
                    }
                });
        if (FMLEnvironment.dist != Dist.CLIENT) {
            AdaptationConfigResolver.refreshAllEntitiesConfig();
        }
    }

    public static DamageAdaptationConfig getConfig(ResourceLocation entityId) {
        if (entityId == null) {
            return null;
        }
        return CONFIGS.get(entityId);
    }

    public static boolean hasConfig(ResourceLocation entityId) {
        if (entityId == null) {
            return false;
        }
        ResourceLocation configId = EntityAdaptationMapping.getConfigForEntity(entityId);
        return configId != null ? CONFIGS.containsKey(configId) : CONFIGS.containsKey(entityId);
    }

    public static DamageAdaptationConfig getDirectConfig(ResourceLocation configId) {
        if (configId == null) {
            return null;
        }
        return CONFIGS.get(configId);
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
}
