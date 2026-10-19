package org.tdddd.epca.impl.events;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import net.neoforged.api.distmarker.Dist;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.events.playeranimator.FirstPersonModifier;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class PlayerAnimator {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ResourceLocation.fromNamespaceAndPath(epca.MODID, "stab"),
                50,
                PlayerAnimator::registerPlayerAnimation);

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ResourceLocation.fromNamespaceAndPath(epca.MODID, "kill_stick"),
                50,
                PlayerAnimator::registerPlayerAnimation);
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        ModifierLayer<IAnimation> layer = new ModifierLayer<>();

        FirstPersonConfiguration config = new FirstPersonConfiguration()
                .setShowRightArm(true)
                .setShowLeftArm(true)
                .setShowRightItem(true)
                .setShowLeftItem(true);

        layer.addModifierBefore(new FirstPersonModifier(
                FirstPersonMode.THIRD_PERSON_MODEL,
                config
        ));

        return layer;
    }
}
