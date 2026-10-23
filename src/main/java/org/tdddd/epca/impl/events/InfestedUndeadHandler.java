package org.tdddd.epca.impl.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModTags;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID)
public class InfestedUndeadHandler {

    /** 每个受害者上一次广播的时间，避免 AoE 伤害把整片虫群每 tick 刷一遍。 */
    private static final java.util.WeakHashMap<LivingEntity, Long> LAST_BROADCAST =
            new java.util.WeakHashMap<>();

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        if (level.isClientSide) return;

        
        if (!victim.getType().is(ModTags.INFESTED_UNDEAD)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        long now = level.getGameTime();
        Long last = LAST_BROADCAST.get(victim);
        if (last != null && now - last < org.tdddd.epca.impl.ModConfig.getAttackBroadcastCooldownTicks()) {
            return;
        }
        LAST_BROADCAST.put(victim, now);

        double followRange = victim.getAttributeValue(Attributes.FOLLOW_RANGE);
        double radius = followRange * 2.0;
        double radiusSqr = radius * radius;

        level.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(radius), e ->
                e != victim &&
                        e.getType().is(ModTags.INFESTED_UNDEAD) &&
                        e instanceof Mob
        ).forEach(e -> {
            Mob nearby = (Mob) e;
            if (nearby.distanceToSqr(victim) <= radiusSqr) {
                nearby.setTarget(attacker);
            }
        });
    }
}