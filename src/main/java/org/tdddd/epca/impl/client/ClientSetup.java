package org.tdddd.epca.impl.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.client.organ.NestLeaderOrganKeys;
import org.tdddd.epca.impl.client.render.EpcaRenderClient;
import org.tdddd.epca.impl.overworld.registry.ModMenus;
import org.tdddd.epca.impl.overworld.registry.gui.menus.SwallowCystScreen;
import org.tdddd.epca.impl.overworld.registry.ModItems;
import org.tdddd.epca.impl.overworld.registry.particles.AdaptationParticleProvider;
import org.tdddd.epca.impl.overworld.registry.ModParticles;
import org.tdddd.epca.impl.overworld.registry.particles.partices.*;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup1(FMLClientSetupEvent event) {
        //  shader
        EpcaRenderClient.onClientSetup(event);

        // Player Animator is optional: only touch its API (and therefore its classes) when the mod
        // is actually installed. A direct reference from this @EventBusSubscriber class would make
        // FML's automatic subscriber injection fail with NoClassDefFoundError without it.
        if (org.tdddd.epca.impl.compat.PlayerAnimSupport.available()) {
            org.tdddd.epca.impl.compat.PlayerAnimCompat.registerFactories();
        }
        
        event.enqueueWork(() -> {
            Item bloodyClock = ModItems.BLOODY_CLOCK.get();
            ItemProperties.register(bloodyClock, ResourceLocation.parse("stage"),
                    (stack, level, entity, seed) -> {
                        Level world = level;
                        if (entity instanceof LivingEntity) world = entity.level();
                        if (world == null) return 0f;   
                        int stage = ClientEvolutionData.getStageForDimension(world);
                        
                        return (float)(stage + 2);
                    });
        });
    }

    /**
     * Menu screens are no longer registered through the (now private) {@code MenuScreens#register};
     * NeoForge 1.21.1 exposes the mod-bus {@link RegisterMenuScreensEvent} for this.
     */
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SWALLOW_CYST.get(), SwallowCystScreen::new);
    }

    /**
     *  GUI SPEC  1  B1 <b>H</b> -
     *
     * <p> Forge  {@link RegisterKeyMappingsEvent}mod
     * {@code ClientHandlerI}  {@code InputEvent.Key}  GLFW
     *  SPEC " H"</p>
     *
     * <p> {@code key.categories.epca}lang
     *  {@code LangDataCN}/{@code LangDataEN}</p>
     *
     * <p>STAGE B  {@link NestLeaderOrganKeys#TELEPORT}
     * SPEC  2 <b></b>
     *  H  - </p>
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(NestLeaderOrganKeys.OPEN_ORGANS);
        event.register(NestLeaderOrganKeys.TELEPORT);
        //  NestLeaderOrganKeys#DECOMPOSE_PARASITE
        event.register(NestLeaderOrganKeys.DECOMPOSE_PARASITE);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                ModParticles.SPLASHI.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new SplashiParticle.Provider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.COTH.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new CothParticle.CothParticleProvider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.BLEEDING.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new BleedingParticle.BleedingParticleProvider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.LIVING_FLESH.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new LivingFleshParticle.Provider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.WAVE.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new WaveParticle.WaveParticleProvider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.WAVE_SMALL.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new WaveSmallParticle.WaveSmallParticleProvider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.INFESTIVE_GAS.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new InfestiveGasParticle.InfestiveGasParticleProvider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.BIOMASS.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new BiomassParticle.BiomassParticleProvider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.BIOMASS_BOOM_SMALL.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new BiomassBoomSmallParticle.BiomassBoomSmallParticleProvider(spriteSet);
                });

        event.registerSpriteSet(
                ModParticles.BIOMASS_BOOM_MEDI.get(),
                spriteSet -> {
                    if (spriteSet == null) {
                        return null;
                    }
                    return new BiomassBoomMediParticle.BiomassBoomMediParticleProvider(spriteSet);
                });

        event.registerSpriteSet(ModParticles.F_ADAPTATION.get(), AdaptationParticleProvider::new);
        event.registerSpriteSet(ModParticles.P_ADAPTATION.get(), AdaptationParticleProvider::new);
    }
}