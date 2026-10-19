package org.tdddd.yawning_neko_api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Loads {@code entity_immunity_effects/*.json}: mob effects that cannot be applied to an entity.
 */
public class EntityImmunityEffectManager implements ResourceManagerReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, List<String>> ENTITY_IMMUNITIES = new HashMap<>();

    public EntityImmunityEffectManager() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ENTITY_IMMUNITIES.clear();
        resourceManager.listResources("entity_immunity_effects", file -> file.getPath().endsWith(".json"))
                .forEach((resourceLocation, resource) -> {
                    try (InputStream stream = resource.open()) {
                        JsonElement element = GSON.fromJson(new InputStreamReader(stream), JsonElement.class);
                        if (element != null) {
                            this.parseImmunities(element);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load entity immunity data: " + resourceLocation);
                        e.printStackTrace();
                    }
                });
    }

    private void parseImmunities(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject root = element.getAsJsonObject();

            for (Entry<String, JsonElement> entry : root.entrySet()) {
                String entityId = entry.getKey();
                JsonElement value = entry.getValue();
                List<String> immunities = new ArrayList<>();
                if (value.isJsonArray()) {
                    for (JsonElement e : value.getAsJsonArray()) {
                        if (e.isJsonPrimitive()) {
                            immunities.add(e.getAsString());
                        }
                    }
                } else if (value.isJsonObject()) {
                    JsonObject configObj = value.getAsJsonObject();
                    if (configObj.has("immunities") && configObj.get("immunities").isJsonArray()) {
                        for (JsonElement ex : configObj.get("immunities").getAsJsonArray()) {
                            if (ex.isJsonPrimitive()) {
                                immunities.add(ex.getAsString());
                            }
                        }
                    } else if (configObj.has("base_effects") && configObj.get("base_effects").isJsonArray()) {
                        for (JsonElement exx : configObj.get("base_effects").getAsJsonArray()) {
                            if (exx.isJsonObject()) {
                                JsonObject effectObj = exx.getAsJsonObject();
                                if (effectObj.has("effect") && effectObj.get("effect").isJsonPrimitive()) {
                                    immunities.add(effectObj.get("effect").getAsString());
                                }
                            } else if (exx.isJsonPrimitive()) {
                                immunities.add(exx.getAsString());
                            } else if (exx.isJsonArray()) {
                                JsonArray arr = exx.getAsJsonArray();
                                if (arr.size() > 0 && arr.get(0).isJsonPrimitive()) {
                                    immunities.add(arr.get(0).getAsString());
                                }
                            }
                        }
                    }
                }

                if (!immunities.isEmpty()) {
                    ENTITY_IMMUNITIES.put(entityId, immunities);
                }
            }
        }
    }

    private static String entityId(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id == null ? "" : id.toString();
    }

    public static boolean isImmune(LivingEntity entity, String effectId) {
        List<String> immunities = ENTITY_IMMUNITIES.get(entityId(entity));
        return immunities != null && immunities.contains(effectId);
    }

    public static List<String> getImmunities(Mob entity) {
        return ENTITY_IMMUNITIES.getOrDefault(entityId(entity), Collections.emptyList());
    }

    public static boolean hasImmunities(Mob entity) {
        return ENTITY_IMMUNITIES.containsKey(entityId(entity));
    }

    public static Map<String, List<String>> getAllImmunities() {
        return Collections.unmodifiableMap(ENTITY_IMMUNITIES);
    }
}
