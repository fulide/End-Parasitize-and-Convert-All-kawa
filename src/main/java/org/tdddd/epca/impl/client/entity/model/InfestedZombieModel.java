package org.tdddd.epca.impl.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.entities.entity.infested.InfestedZombie;
import software.bernie.geckolib.model.GeoModel;

public class InfestedZombieModel extends GeoModel<InfestedZombie> {
    @Override
    public ResourceLocation getModelResource(InfestedZombie entity) {
        return ResourceLocation.fromNamespaceAndPath(epca.MODID, "geo/entity/infested_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(InfestedZombie entity) {
        return ResourceLocation.fromNamespaceAndPath(epca.MODID, "textures/entity/infested_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(InfestedZombie entity) {
        return ResourceLocation.fromNamespaceAndPath(epca.MODID, "animations/infested_zombie.animation.json");
    }
}