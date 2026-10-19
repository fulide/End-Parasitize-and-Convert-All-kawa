package org.tdddd.epca.impl.overworld.registry.capability;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.tdddd.epca.impl.epca;

/**
 * Central registry for every EPCA data attachment.
 *
 * <p>1.20.1 used Forge capabilities ({@code Capability}, {@code ICapabilitySerializable}, {@code LazyOptional},
 * {@code AttachCapabilitiesEvent}); none of those classes exist in NeoForge 1.21.1, where the replacement is the
 * <b>data attachment</b> system. This class is the single entry point for all of it:</p>
 *
 * <pre>{@code
 * // instead of entity.getCapability(LifetimeCapability.LIFETIME).ifPresent(cap -> cap.setRemainingTicks(n))
 * LifetimeData data = entity.getData(ModAttachments.LIFETIME);
 * data.setRemainingTicks(n);
 * }</pre>
 *
 * <p>Registration: call {@link #register(IEventBus)} from the mod constructor (see {@code epca.java}).</p>
 */
public final class ModAttachments {

    /** Attachment types are a NeoForge registry, not a vanilla one. */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, epca.MODID);

    /**
     * Lifetime countdown (formerly {@code ILifetimeCapability}/{@code LifetimeCapability}).
     * Serialized NBT layout is unchanged: {@code {RemainingTicks: int}}.
     */
    public static final Supplier<AttachmentType<LifetimeData>> LIFETIME =
            ATTACHMENT_TYPES.register("lifetime",
                    () -> AttachmentType.<CompoundTag, LifetimeData>serializable((Supplier<LifetimeData>) LifetimeData::new)
                            .build());

    /**
     * Shield pool (formerly {@code IShieldCapability}/{@code ShieldCapability}).
     * Serialized NBT layout: {@code {Shield: float}}.
     */
    public static final Supplier<AttachmentType<ShieldData>> SHIELD =
            ATTACHMENT_TYPES.register("shield",
                    () -> AttachmentType.<CompoundTag, ShieldData>serializable((Supplier<ShieldData>) ShieldData::new)
                            .build());

    private ModAttachments() {
    }

    /** Registers {@link #ATTACHMENT_TYPES} on the mod event bus and hooks the lifetime countdown. */
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        // Registered explicitly (instead of relying on @EventBusSubscriber scanning of a nested class) so the ticker
        // is only active when the attachments themselves are registered.
        NeoForge.EVENT_BUS.addListener(LifetimeTicker::onEntityTick);
    }

    /**
     * Drives the {@link #LIFETIME} countdown once per entity tick and discards the entity when it reaches zero.
     *
     * <p>The old capability registered a {@code LivingEvent.LivingTickEvent} listener for this; in 1.21.1 that event
     * is {@link EntityTickEvent.Post} and the listener only has to filter for {@link LivingEntity}. Only entities that
     * actually have the attachment set are touched, so no data is created for every mob in the world.</p>
     */
    public static final class LifetimeTicker {
        private LifetimeTicker() {
        }

        public static void onEntityTick(EntityTickEvent.Post event) {
            Entity entity = event.getEntity();
            if (!(entity instanceof LivingEntity living)) {
                return;
            }
            LifetimeData data = living.getExistingDataOrNull(LIFETIME);
            if (data != null) {
                data.tick(living);
            }
        }
    }
}
