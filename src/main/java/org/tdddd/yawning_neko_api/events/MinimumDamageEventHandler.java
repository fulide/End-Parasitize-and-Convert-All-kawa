package org.tdddd.yawning_neko_api.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.tdddd.yawning_neko_api.data.MinimumDamageManager;
import org.tdddd.yawning_neko_api.damages.ModDamageTypes;

/**
 * Minimum-damage handling.
 *
 * <p>Two behaviours are combined here:</p>
 * <ul>
 *   <li>Attacks made by a mob that has a {@code minimum_damage} config deal at least that amount of
 *       direct (armour-ignoring) damage. Replaces the old HIGH-priority {@code LivingAttackEvent}
 *       handler.</li>
 *   <li>Damage of type {@code yawning_neko_api:minimum} bypasses the vanilla damage pipeline and is
 *       subtracted from health directly. This replaces the old {@code LivingEntityMixin} (mixins are
 *       intentionally not ported).</li>
 * </ul>
 */
public final class MinimumDamageEventHandler {
    private MinimumDamageEventHandler() {
    }

    /**
     * Old {@code LivingAttackEvent} handler at {@code EventPriority.HIGH}.
     */
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        LivingEntity attacker = getAttacker(source.getEntity());
        if (attacker != null) {
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
                return;
            }

            MinimumDamageManager manager = MinimumDamageManager.getInstance();
            if (manager != null) {
                float minDamage = manager.getMinDamage(attacker);
                if (!(minDamage <= 0.0F)) {
                    float newHealth = Math.max(0.0F, target.getHealth() - minDamage);
                    target.setHealth(newHealth);
                    target.hurtDuration = 10;
                    target.hurtTime = target.hurtDuration;
                    target.invulnerableTime = 10;
                    if (newHealth <= 0.0F) {
                        target.die(source);
                    }
                }
            }
        }
    }

    /**
     * Old {@code LivingEntityMixin} behaviour for {@code yawning_neko_api:minimum}.
     */
    public static void onMinimumDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(ModDamageTypes.MINIMUM)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            event.setCanceled(true);
            return;
        }

        if (entity.isDeadOrDying()) {
            event.setCanceled(true);
            return;
        }

        float amount = event.getAmount();
        float newHealth = Math.max(0.0F, entity.getHealth() - amount);
        entity.setHealth(newHealth);
        entity.hurtDuration = 10;
        entity.hurtTime = entity.hurtDuration;
        entity.invulnerableTime = 10;
        if (newHealth <= 0.0F) {
            entity.die(event.getSource());
        }

        event.setCanceled(true);
    }

    private static LivingEntity getAttacker(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living;
        } else {
            if (entity instanceof Projectile projectile) {
                Entity owner = projectile.getOwner();
                if (owner instanceof LivingEntity living) {
                    return living;
                }
            }

            return null;
        }
    }
}
