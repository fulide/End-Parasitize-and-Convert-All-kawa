package org.tdddd.yawning_neko_api.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;

/**
 * Loads {@code data/<namespace>/adaptation_color_models/*.json}: the tint colours used while an
 * adaptation effect is active, plus the entity allow-list the tint applies to.
 */
public final class AdaptationColorConfig {
    private static final Gson GSON = new Gson();

    /** 0xFF33E6FF. */
    public static final int DEFAULT_PARTIAL = -13375745;
    /** 0xFFE633E6. */
    public static final int DEFAULT_FULL = -1690650;
    public static final int DURATION_TICKS = 10;

    private static int partialColor = DEFAULT_PARTIAL;
    private static int fullColor = DEFAULT_FULL;
    private static final Set<ResourceLocation> EXPLICIT = new HashSet<>();
    private static final Set<String> NAMESPACES = new HashSet<>();

    private AdaptationColorConfig() {
    }

    public static void reload(ResourceManager resourceManager) {
        partialColor = DEFAULT_PARTIAL;
        fullColor = DEFAULT_FULL;
        EXPLICIT.clear();
        NAMESPACES.clear();
        resourceManager.listResources("adaptation_color_models", path -> path.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.open()) {
                        JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                        if (json != null) {
                            if (json.has("partial") && json.get("partial").isJsonObject()) {
                                partialColor = readColor(json.getAsJsonObject("partial"), partialColor);
                            }

                            if (json.has("full") && json.get("full").isJsonObject()) {
                                fullColor = readColor(json.getAsJsonObject("full"), fullColor);
                            }

                            if (json.has("entities") && json.get("entities").isJsonArray()) {
                                for (JsonElement element : json.getAsJsonArray("entities")) {
                                    if (element.isJsonPrimitive()) {
                                        String raw = element.getAsString();
                                        if (raw.endsWith(":*")) {
                                            NAMESPACES.add(raw.substring(0, raw.length() - 2));
                                        } else {
                                            ResourceLocation parsed = ResourceLocation.tryParse(raw);
                                            if (parsed != null) {
                                                EXPLICIT.add(parsed);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        // Matches the original loader: broken colour files are skipped.
                    }
                });
    }

    private static int readColor(JsonObject section, int fallback) {
        if (section.has("color") && section.get("color").isJsonPrimitive()) {
            String raw = section.get("color").getAsString().trim();
            if (raw.startsWith("#")) {
                raw = raw.substring(1);
            }

            try {
                long value = Long.parseLong(raw, 16);
                return raw.length() <= 6 ? (int) (4278190080L | value) : (int) value;
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        } else {
            return fallback;
        }
    }

    public static boolean applies(Entity entity) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return entityId == null
                ? false
                : EXPLICIT.contains(entityId) || NAMESPACES.contains(entityId.getNamespace());
    }

    /**
     * @param eventType the value from {@link DamageAdaptation#getActiveAdaptationEvent(net.minecraft.world.entity.Entity)}
     * @return ARGB colour, or {@code -1} when no tint should be applied
     */
    public static int colorFor(int eventType) {
        if (eventType < 0) {
            return -1;
        } else {
            return eventType != 0 && eventType != 1 ? fullColor : partialColor;
        }
    }
}
