package org.tdddd.yawning_neko_api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

/**
 * One entry of {@code minimum_damage/*.json}.
 */
public class MinDamageConfig {
    private final float minDamage;

    public MinDamageConfig(float minDamage) {
        this.minDamage = minDamage;
    }

    public float getMinDamage() {
        return this.minDamage;
    }

    public static MinDamageConfig fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            float minDamage = json.getAsFloat();
            return new MinDamageConfig(minDamage);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            float minDamage = GsonHelper.getAsFloat(obj, "min_damage", 0.0F);
            return new MinDamageConfig(minDamage);
        } else {
            return new MinDamageConfig(0.0F);
        }
    }
}
