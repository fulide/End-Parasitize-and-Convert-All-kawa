package org.tdddd.epca.impl.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.data.organ.NestLeaderOrganCarry;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 *  GUI STAGE 2 + STAGE 3 /
 *
 * <p>STAGE 2  /
 * {@link NestLeaderOrganCarry}
 *  {@code ServerPlayer} </p>
 *
 * <p>STAGE 3  / <b></b>STAGE A  7  9
 *  {@code entity_gravity}
 * {@link NestLeaderOrganDamageHandler#clear}
 * {@code NestLeaderOrganEffects#clear}
 *
 * {@code epca#onPlayerTick}
 *  {@code addTransientModifier}  transient
 *  UUID </p>
 *
 * <p> {@code ClientHandlerI}  {@code @EventBusSubscriber}
 * <b></b> {@code value = Dist.CLIENT}
 * </p>
 */
@EventBusSubscriber(modid = epca.MODID)
public final class NestLeaderOrganEvents {

    private NestLeaderOrganEvents() {
    }

    /** +  STAGE 3  */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        NestLeaderOrganCarry.returnToPlayer(player);
        NestLeaderOrganDamageHandler.clear(player);
    }

    /**  tick  */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        NestLeaderOrganCarry.onDimensionChanged(player);
        NestLeaderOrganDamageHandler.clear(player);
    }

    /**
     *
     *
     * <p> {@code epca#onPlayerTick}
     * {@code NestLeaderOrganEffects#notLeader}
     *
     *
     *  tick  {@code notLeader} </p>
     *
     * <p> tick  {@code NestLeaderOrganEffects#tick}
     * </p>
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        NestLeaderOrganDamageHandler.clear(player);
    }
}

