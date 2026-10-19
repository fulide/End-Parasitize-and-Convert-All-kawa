package org.tdddd.yawning_neko_api.damages;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

/**
 * Damage types added by yawning_neko_api. The damage type itself is declared by the datapack at
 * {@code data/yawning_neko_api/damage_type/minimum.json}.
 */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> MINIMUM =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("yawning_neko_api", "minimum"));

    private ModDamageTypes() {
    }
}
