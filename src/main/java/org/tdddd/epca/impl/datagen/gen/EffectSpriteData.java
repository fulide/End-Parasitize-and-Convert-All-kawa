package org.tdddd.epca.impl.datagen.gen;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;
import org.tdddd.epca.impl.epca;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public class EffectSpriteData extends SpriteSourceProvider {

    public EffectSpriteData(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                            ExistingFileHelper fileHelper) {
        super(output, lookupProvider, epca.MODID, fileHelper);
    }

    /**
     * In 1.21 the provider writes {@code assets/<namespace>/atlases/<atlas id>.json} through
     * {@link net.neoforged.neoforge.common.data.JsonCodecProvider}; the atlas id is a resource
     * location (the constants defined by {@link SpriteSourceProvider}), not a texture path.
     *
     * <p>The 1.20.1 code passed {@code textures/atlas/mob_effects.png}, which produced a file the game
     * never loads ({@code assets/minecraft/atlases/textures/atlas/mob_effects.png.json}). The sprite
     * sources now go into the real {@code minecraft:mob_effects} atlas.</p>
     */
    @Override
    protected void gather() {
        String[] effectTextures = {
                "bleeding", "viral", "fear", "coth",
                "contempt_inorganic", "corrosive", "rage", "needler",
                "deep_sneak", "solidify", "ender_erosion",
                "camouflage", "spirit", "soul_protection"
        };

        SourceList atlas = atlas(MOB_EFFECTS_ATLAS);
        for (String name : effectTextures) {
            ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(epca.MODID, "mob_effect/" + name);
            atlas.addSource(new SingleFile(textureLoc, Optional.empty()));
        }
    }
}
