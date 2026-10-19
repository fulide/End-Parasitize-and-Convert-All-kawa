package org.tdddd.epca.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tdddd.epca.impl.overworld.data.*;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganEffects;
import org.tdddd.epca.impl.overworld.data.organ.stats.OrganStatManager;
import org.tdddd.epca.impl.overworld.registry.blocks.ModBlockEntities;
import org.tdddd.epca.impl.overworld.registry.blocks.block.InfestedPumpkinBehaviour;
import org.tdddd.epca.impl.overworld.registry.ModBlocks;
import org.tdddd.epca.impl.overworld.registry.ModDataComponents;
import org.tdddd.epca.impl.overworld.registry.capability.ModAttachments;
import org.tdddd.epca.impl.overworld.registry.items.item.LivingArmorMaterial;
import org.tdddd.epca_physics.structure.SubLevelRegistry;
import org.tdddd.epca.impl.client.ClientSetup;
import org.tdddd.epca.impl.commands.*;
import org.tdddd.epca.impl.overworld.registry.ModEffects;
import org.tdddd.epca.impl.overworld.registry.ModEntities;
import org.tdddd.epca.impl.events.EvolutionStageEvents;
import org.tdddd.epca.impl.events.PendingConversionManager;
import org.tdddd.epca.impl.fluid.ModFluids;
import org.tdddd.epca.impl.overworld.registry.ModMenus;
import org.tdddd.epca.impl.overworld.registry.items.ModCreativeTabs;
import org.tdddd.epca.impl.overworld.registry.ModItems;
import org.tdddd.epca.impl.overworld.registry.items.item.LivingArmorBox;
import org.tdddd.epca.impl.overworld.registry.items.item.WingChestManager;
import org.tdddd.epca.impl.network.ModNetwork;
import org.tdddd.epca.impl.overworld.registry.ModParticles;
import org.tdddd.epca.impl.overworld.registry.ModSoundEvents;
import org.tdddd.yawning_neko_api.YawningNekoApi;

@Mod(epca.MODID)
public class epca {
    public static final String MODID = "epca";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /**
     * NeoForge 1.21.1 injects the mod event bus and the mod container into the mod constructor;
     * {@code FMLJavaModLoadingContext} no longer exists.
     */
    public epca(IEventBus modEventBus, ModContainer modContainer) {
        // Mixin configurations are declared in META-INF/neoforge.mods.toml ([[mixins]]), so no manual
        // Mixins.addConfiguration(...) call is required.

        ModNetwork.register(modEventBus);

        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModParticles.REGISTRY.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModEffects.register(modEventBus);
        ModSoundEvents.SOUNDS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        LivingArmorMaterial.ARMOR_MATERIALS.register(modEventBus);
        ModAttachments.register(modEventBus);

        // yawning_neko_api (bundled source port): attachments, payloads, datapack listeners.
        YawningNekoApi.init(modEventBus, modContainer);

        ModConfig.register(modContainer);
        WingChestManager.init();

        // Physicalized blocks (epca_physics compatibility shim in this port).
        SubLevelRegistry.registerBehaviour(InfestedPumpkinBehaviour.ID,
                data -> new InfestedPumpkinBehaviour());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.register(ClientSetup.class);
            // shader / render layer bootstrap for the client
            org.tdddd.epca.impl.client.render.EpcaRenderClient.init(modEventBus);
        }

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Game (NeoForge) bus: instance handlers must be registered explicitly in NeoForge.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(EvolutionStageEvents.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // reserved for post-registry setup
        });
    }

    private void clientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("jade")) {
            // Jade integration is optional; nothing to do here in 1.21.1 (see compat package).
        }
    }

    @SubscribeEvent
     public void onRegisterCommands(RegisterCommandsEvent event) {
        EvolutionCommand.register(event.getDispatcher());
        NegativeDamageCommand.register(event.getDispatcher());
        event.getDispatcher().register(ParasiteSummonCommand.register());
        event.getDispatcher().register(ParasiteSummonCommand.registerSetParasite());
        NestLeaderCommand.register(event);
    }

    @SubscribeEvent
     public void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        EvolutionDataStorage.get(overworld);
    }

    /**
     * Server tick hook: pending conversions.
     *
     * <p>1.20.1 {@code TickEvent.ServerTickEvent} was replaced by {@code ServerTickEvent.Post} in
     * NeoForge 1.21.1.</p>
     */
    @SubscribeEvent
     public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        PendingConversionManager.tick(server);
    }

    /**
     * Player tick hook: biomass effects + nest leader organ effects.
     *
     * <p>1.20.1 {@code TickEvent.PlayerTickEvent} (END phase) was replaced by
     * {@code PlayerTickEvent.Post}.</p>
     */
    @SubscribeEvent
     public void onPlayerTick(PlayerTickEvent.Post event) {
        LivingArmorBox.applyBiomassEffects(event.getEntity());

        // STAGE 3 NestLeaderOrganEffects
        if (!event.getEntity().level().isClientSide()) {
            if (NestLeaderManager.isNestLeader(event.getEntity().getUUID())) {
                NestLeaderOrganEffects.tick(event.getEntity());
            } else {
                NestLeaderOrganEffects.notLeader(event.getEntity());
            }
        }
    }

    @SubscribeEvent
     public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EntityConversionManager());
        event.addListener(new EntityIntegrationManager());
        event.addListener(new EntityKillCountManager());
        event.addListener(CarryConfigManager.INSTANCE);
        event.addListener(new BiomassSpawnManager());
        // data/epca/organ_stats/<item>.json + _defaults.json handled by OrganStatManager
        event.addListener(new OrganStatManager());
    }

    // Attribute registration is handled by ModEntityEvents.onEntityAttributeCreation().

    public static final GameRules.Key<GameRules.BooleanValue> DO_INFESTED_FALLBACK =
            GameRules.register("epca_hardnessConversionBlock", GameRules.Category.MISC,
                    GameRules.BooleanValue.create(true));
}
