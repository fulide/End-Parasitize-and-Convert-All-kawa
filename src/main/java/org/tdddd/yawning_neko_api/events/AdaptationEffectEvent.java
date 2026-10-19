package org.tdddd.yawning_neko_api.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * Fired on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} when an entity adapts to a
 * damage type, or when its adaptation reaches its maximum level.
 *
 * <p>This event is not cancelable (same as the original 1.20.1 implementation).</p>
 */
public class AdaptationEffectEvent extends Event {
    private final LivingEntity entity;
    private final Type effectType;

    public AdaptationEffectEvent(LivingEntity entity, Type effectType) {
        this.entity = entity;
        this.effectType = effectType;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public Type getEffectType() {
        return this.effectType;
    }

    public enum Type {
        PARTIAL_ADAPTATION,
        FULL_ADAPTATION
    }
}
