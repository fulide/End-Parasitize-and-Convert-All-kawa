package org.tdddd.yawning_neko_api;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.tdddd.yawning_neko_api.client.ClientPacketHandlers;
import org.tdddd.yawning_neko_api.data.AdaptationDataCapability;
import org.tdddd.yawning_neko_api.data.DamageAdaptation;
import org.tdddd.yawning_neko_api.data.EntityAttackEffectManager;
import org.tdddd.yawning_neko_api.data.EntityImmunityEffectManager;
import org.tdddd.yawning_neko_api.data.EntityUpgradeManager;
import org.tdddd.yawning_neko_api.data.IAdaptationData;
import org.tdddd.yawning_neko_api.data.MinimumDamageManager;
import org.tdddd.yawning_neko_api.events.EffectImmunityHandler;
import org.tdddd.yawning_neko_api.events.EntityAttackEffectHandler;
import org.tdddd.yawning_neko_api.events.MinimumDamageEventHandler;
import org.tdddd.yawning_neko_api.network.ModNetwork;

/**
 * Bootstrap entry point of the yawning_neko_api library.
 *
 * <p>This library is compiled into the epca jar, therefore there is deliberately no {@code @Mod}
 * annotation. The owning mod calls {@link #init(IEventBus, ModContainer)} once from its
 * constructor.</p>
 */
public final class YawningNekoApi {
    public static final String MODID = "yawning_neko_api";

    /**
     * Data attachment replacing the old Forge {@code IAdaptationData} capability.
     *
     * <p>Registered for every {@link net.minecraft.world.entity.Entity}; access it through
     * {@link IAdaptationData#get(net.minecraft.world.entity.LivingEntity)}.</p>
     */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IAdaptationData>> ADAPTATION_DATA =
            ATTACHMENT_TYPES.register("adaptation_data",
                    () -> AttachmentType.<CompoundTag, IAdaptationData>serializable(
                            AdaptationDataCapability::new).build());

    private static final MinimumDamageManager MIN_DAMAGE_MANAGER = new MinimumDamageManager();

    private YawningNekoApi() {
    }

    /**
     * Registers everything the library needs. Safe to call once during mod construction.
     *
     * @param modEventBus  the mod event bus
     * @param modContainer the owning mod container (currently unused, kept for the shared bootstrap signature)
     */
    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        ATTACHMENT_TYPES.register(modEventBus);

        // Payload registration is a mod-bus event in NeoForge 1.21.1.
        modEventBus.addListener(ModNetwork::register);

        // Datapack reload listeners (server resources).
        NeoForge.EVENT_BUS.addListener(DamageAdaptation::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(YawningNekoApi::onAddReloadListeners);

        // Game events (replacement for Forge's LivingHurtEvent / LivingAttackEvent etc.).
        NeoForge.EVENT_BUS.addListener(DamageAdaptation::onLivingIncomingDamage);
        NeoForge.EVENT_BUS.addListener(DamageAdaptation::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(DamageAdaptation::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(DamageAdaptation::onEntityLeaveLevel);

        // yawning_neko_api:minimum damage bypasses the vanilla damage pipeline (old LivingEntityMixin).
        // HIGHEST so it runs before (and cancels ahead of) the configured-attacker minimum damage below.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, MinimumDamageEventHandler::onMinimumDamage);
        // Entity-attack minimum damage is applied before damage adaptation, like the old HIGH priority handler.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, MinimumDamageEventHandler::onLivingAttack);

        NeoForge.EVENT_BUS.addListener(EffectImmunityHandler::onEffectApplicable);

        EntityAttackEffectHandler attackEffectHandler = new EntityAttackEffectHandler();
        NeoForge.EVENT_BUS.addListener(attackEffectHandler::onEntitySpawn);
        NeoForge.EVENT_BUS.addListener(attackEffectHandler::onEntityAttack);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Kept in a separate class so that dedicated servers never resolve client-only types.
            ClientPacketHandlers.registerClientEvents();
        }
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(MIN_DAMAGE_MANAGER);
        event.addListener(new EntityAttackEffectManager());
        event.addListener(new EntityUpgradeManager());
        event.addListener(new EntityImmunityEffectManager());
    }
}
