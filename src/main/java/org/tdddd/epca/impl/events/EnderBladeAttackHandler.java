package org.tdddd.epca.impl.events;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.epca;
import org.tdddd.yawning_neko_api.damages.ModDamageTypes;

import java.util.Collection;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID)
public class EnderBladeAttackHandler {
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        Level level = event.getEntity().level();
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;
        if (!(source.getDirectEntity() instanceof LivingEntity)) return;
        LivingEntity target = event.getEntity();

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;
        
        CustomData data = weapon.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.copyTag().getBoolean("epca:ender_blade")) return;
        
        float baseAttack = getWeaponAttackDamage(weapon);
        float extra = (baseAttack + 1) * 0.25f;

        Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        Holder<DamageType> holder = registry.getHolderOrThrow(ModDamageTypes.MINIMUM);
        DamageSource minimumSource = new DamageSource(holder);
        target.hurt(minimumSource, extra);
    }

    private static float getWeaponAttackDamage(ItemStack stack) {
        final double[] total = { 0.0D };
        stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                total[0] += modifier.amount();
            }
        });
        return (float) total[0];
    }
}