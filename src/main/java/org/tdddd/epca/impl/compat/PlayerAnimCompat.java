package org.tdddd.epca.impl.compat;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.events.playeranimator.FirstPersonModifier;

/**
 * All Player Animator ({@code dev.kosmx.playerAnim.*}) usage of the mod, isolated in one class.
 *
 * <p>Player Animator is <b>optional</b>. Callers must ask
 * {@link PlayerAnimSupport#available()} first; only then may they touch this class. Keeping every
 * PAL type here means the JVM never resolves them on installations that do not ship the mod.</p>
 *
 * <p>The animation ids are {@code epca:stab} and {@code epca:kill_stick}; their JSON lives under
 * {@code assets/epca/player_animation/}. The factory registered per-player layer is what the
 * item/attack code then plays animations on.</p>
 */
public final class PlayerAnimCompat {

    public static final String STAB = "stab";
    public static final String KILL_STICK = "kill_stick";

    private PlayerAnimCompat() {
    }

    /** Registers the per-player animation layers. Called once from client setup. */
    public static void registerFactories() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                id(STAB), 50, PlayerAnimCompat::createLayer);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                id(KILL_STICK), 50, PlayerAnimCompat::createLayer);
    }

    private static IAnimation createLayer(AbstractClientPlayer player) {
        ModifierLayer<IAnimation> layer = new ModifierLayer<>();

        FirstPersonConfiguration config = new FirstPersonConfiguration()
                .setShowRightArm(true)
                .setShowLeftArm(true)
                .setShowRightItem(true)
                .setShowLeftItem(true);

        layer.addModifierBefore(new FirstPersonModifier(FirstPersonMode.THIRD_PERSON_MODEL, config));
        return layer;
    }

    /**
     * Plays a registered animation on the player's epca layer, if the layer and the animation exist.
     *
     * <p>Player Animator 2.x returns an {@link IPlayable} from the registry;
     * {@code IPlayable#playAnimation()} yields the same {@code KeyframeAnimationPlayer} the 1.x code
     * constructed directly.</p>
     */
    public static void play(AbstractClientPlayer player, String animationName) {
        ModifierLayer<IAnimation> layer = layer(player, animationName);
        if (layer == null) {
            return;
        }
        IPlayable playable = PlayerAnimationRegistry.getAnimation(id(animationName));
        if (playable != null) {
            layer.setAnimation(playable.playAnimation());
        }
    }

    /** Stops whatever is currently playing on that layer. */
    public static void clear(AbstractClientPlayer player, String animationName) {
        ModifierLayer<IAnimation> layer = layer(player, animationName);
        if (layer != null) {
            layer.setAnimation(null);
        }
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> layer(AbstractClientPlayer player, String animationName) {
        IAnimation data = PlayerAnimationAccess.getPlayerAssociatedData(player).get(id(animationName));
        return data instanceof ModifierLayer<?> layer ? (ModifierLayer<IAnimation>) layer : null;
    }

    private static ResourceLocation id(String animationName) {
        return ResourceLocation.fromNamespaceAndPath(epca.MODID, animationName);
    }
}
