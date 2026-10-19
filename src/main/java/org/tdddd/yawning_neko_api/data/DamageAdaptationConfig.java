package org.tdddd.yawning_neko_api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * Parsed {@code damage_adaptation_configs/*.json} entry.
 *
 * <p>Damage type keys are either a {@link ResourceLocation} or a {@link TagKey} of damage types.
 * They are kept as {@code Object} for compatibility with the original API, which exposed
 * {@code Map<Object, Float>}.</p>
 */
public class DamageAdaptationConfig {
    private final Map<Object, Float> damageMultipliers;
    private final float adaptationProbability;
    private final int maxAdaptations;
    private final float adaptationPercentagePerInjury;
    private final int minimumKillDuration;
    private final int minimumAttackInterval;
    private final int minimumKillCount;
    private final float brokenAdaptationDuration;
    private final int maxAdaptableDamageTypes;
    private final Set<Object> nonAdaptableDamageTypes;
    private final float maxMultiplierAbs;

    public DamageAdaptationConfig(
            Map<Object, Float> damageMultipliers,
            float adaptationProbability,
            int maxAdaptations,
            float adaptationPercentagePerInjury,
            int minimumKillDuration,
            int minimumAttackInterval,
            int minimumKillCount,
            float brokenAdaptationDuration,
            int maxAdaptableDamageTypes) {
        this.damageMultipliers = damageMultipliers;
        this.adaptationProbability = adaptationProbability;
        this.maxAdaptations = maxAdaptations;
        this.adaptationPercentagePerInjury = adaptationPercentagePerInjury;
        this.minimumKillDuration = minimumKillDuration;
        this.minimumAttackInterval = minimumAttackInterval;
        this.minimumKillCount = minimumKillCount;
        this.brokenAdaptationDuration = brokenAdaptationDuration;
        this.maxAdaptableDamageTypes = maxAdaptableDamageTypes;
        float maxAbs = 0.0F;
        this.nonAdaptableDamageTypes = new HashSet<>();

        for (Entry<Object, Float> entry : damageMultipliers.entrySet()) {
            float multiplier = entry.getValue();
            if (Math.abs(multiplier) > maxAbs) {
                maxAbs = Math.abs(multiplier);
            }

            if (multiplier >= 1.0F || multiplier < 0.0F) {
                this.nonAdaptableDamageTypes.add(entry.getKey());
            }
        }

        this.maxMultiplierAbs = maxAbs;
    }

    /**
     * Combines a base config and a rule overlay: multiplier entries are summed, every other value
     * comes from the overlay.
     */
    public static DamageAdaptationConfig additive(DamageAdaptationConfig base, DamageAdaptationConfig overlay) {
        Map<Object, Float> multipliers = new HashMap<>(base.damageMultipliers);
        overlay.damageMultipliers.forEach((key, value) -> multipliers.merge(key, value, Float::sum));
        return new DamageAdaptationConfig(
                multipliers,
                overlay.adaptationProbability,
                overlay.maxAdaptations,
                overlay.adaptationPercentagePerInjury,
                overlay.minimumKillDuration,
                overlay.minimumAttackInterval,
                overlay.minimumKillCount,
                overlay.brokenAdaptationDuration,
                overlay.maxAdaptableDamageTypes);
    }

    public Map<Object, Float> getDamageMultipliers() {
        return this.damageMultipliers;
    }

    public float getAdaptationProbability() {
        return this.adaptationProbability;
    }

    public int getMaxAdaptations() {
        return this.maxAdaptations;
    }

    public float getAdaptationPercentagePerInjury() {
        return this.adaptationPercentagePerInjury;
    }

    public int getMinimumKillDuration() {
        return this.minimumKillDuration;
    }

    public int getMinimumAttackInterval() {
        return this.minimumAttackInterval;
    }

    public int getMinimumKillCount() {
        return this.minimumKillCount;
    }

    public float getBrokenAdaptationDuration() {
        return this.brokenAdaptationDuration;
    }

    public float getMaxMultiplierAbs() {
        return this.maxMultiplierAbs;
    }

    public int getMaxAdaptableDamageTypes() {
        return this.maxAdaptableDamageTypes;
    }

    public Set<Object> getNonAdaptableDamageTypes() {
        return this.nonAdaptableDamageTypes;
    }

    public boolean isDamageTypeAdaptable(Object damageType) {
        return !this.damageMultipliers.containsKey(damageType)
                ? true
                : !this.nonAdaptableDamageTypes.contains(damageType);
    }

    public boolean shouldTriggerBrokenAdaptation(Object damageType, float damageAmount) {
        if (!this.damageMultipliers.containsKey(damageType)) {
            return false;
        } else {
            Float multiplier = this.damageMultipliers.get(damageType);
            return multiplier == null
                    ? false
                    : multiplier != 0.0F
                            && Math.abs(multiplier) >= this.maxMultiplierAbs - 0.001F
                            && damageAmount > 0.0F;
        }
    }

    public static DamageAdaptationConfig fromJson(JsonObject json) {
        Map<Object, Float> damageMultipliers = new HashMap<>();
        if (json.has("damage") && json.get("damage").isJsonObject()) {
            JsonObject damageObj = json.getAsJsonObject("damage");

            for (Entry<String, JsonElement> entry : damageObj.entrySet()) {
                String damageType = entry.getKey();
                float multiplier = entry.getValue().getAsFloat();
                if (damageType.startsWith("#")) {
                    String tagName = damageType.substring(1);
                    damageMultipliers.put(
                            TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(tagName)), multiplier);
                } else {
                    damageMultipliers.put(ResourceLocation.parse(damageType), multiplier);
                }
            }
        }

        JsonObject adaptabilityObj = json.getAsJsonObject("damage_adaptability");
        if (adaptabilityObj == null) {
            throw new IllegalArgumentException("Missing 'damage_adaptability' section");
        }
        float probability = adaptabilityObj.get("damage_adaptability_probability").getAsFloat();
        int times = adaptabilityObj.get("damage_adaptability_times").getAsInt();
        float percentage = adaptabilityObj.get("adaptation_percentage_per_injury").getAsFloat();
        int killDuration = adaptabilityObj.get("minimum_kill_duration").getAsInt();
        int attackInterval = adaptabilityObj.get("minimum_attack_interval").getAsInt();
        int killCount = 0;
        if (adaptabilityObj.has("minimum_kill_count")) {
            killCount = adaptabilityObj.get("minimum_kill_count").getAsInt();
        }

        float brokenAdaptationDuration = 3.0F;
        if (adaptabilityObj.has("broken_adaptation_duration")) {
            brokenAdaptationDuration = adaptabilityObj.get("broken_adaptation_duration").getAsFloat();
        }

        int maxAdaptableDamageTypes = 0;
        if (adaptabilityObj.has("max_adaptable_damage_types")) {
            maxAdaptableDamageTypes = adaptabilityObj.get("max_adaptable_damage_types").getAsInt();
        }

        return new DamageAdaptationConfig(
                damageMultipliers,
                probability,
                times,
                percentage,
                killDuration,
                attackInterval,
                killCount,
                brokenAdaptationDuration,
                maxAdaptableDamageTypes);
    }
}
