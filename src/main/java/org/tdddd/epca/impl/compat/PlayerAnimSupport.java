package org.tdddd.epca.impl.compat;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Dependency probe for Player Animator.
 *
 * <p>Deliberately contains <b>no</b> reference to {@code dev.kosmx.playerAnim.*}: it may be called on
 * any side, with or without the mod installed. All actual API usage lives in
 * {@link PlayerAnimCompat}, which must only be touched when {@link #available()} is {@code true}.
 * That split is what keeps {@code epca} loadable when Player Animator is absent — a direct
 * reference from an {@code @EventBusSubscriber} class makes FML's automatic subscriber injection
 * fail with {@code NoClassDefFoundError} and the whole mod fails to construct.</p>
 */
public final class PlayerAnimSupport {

    private PlayerAnimSupport() {
    }

    public static boolean available() {
        return FMLEnvironment.dist.isClient() && ModList.get().isLoaded("playeranimator");
    }
}
