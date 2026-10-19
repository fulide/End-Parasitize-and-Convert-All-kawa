package org.tdddd.epca.impl.datagen.gen;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModParticles;


public class ParticleData extends ParticleDescriptionProvider {

    public ParticleData(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper);
    }

    @Override
    protected void addDescriptions() {
        
        addParticle(ModParticles.SPLASHI, "splashi");
        addParticle(ModParticles.COTH, "coth");
        addParticle(ModParticles.BLEEDING, "bleeding");
        addParticle(ModParticles.LIVING_FLESH, "living_flesh");
        addParticle(ModParticles.WAVE, "wave");
        addParticle(ModParticles.WAVE_SMALL, "wave_small");
        addParticle(ModParticles.INFESTIVE_GAS, "infestive_gas");
        addParticle(ModParticles.BIOMASS, "biomass");
        addParticle(ModParticles.BIOMASS_BOOM_SMALL, "biomass_boom_small");
        addParticle(ModParticles.BIOMASS_BOOM_MEDI, "biomass_boom_medi");
        addParticle(ModParticles.F_ADAPTATION, "f_adaptation");
        addParticle(ModParticles.P_ADAPTATION, "p_adaptation");
    }

    private void addParticle(DeferredHolder<ParticleType<?>, SimpleParticleType> particle, String textureName) {
        sprite(particle.get(), ResourceLocation.fromNamespaceAndPath(epca.MODID, textureName));
    }
}
