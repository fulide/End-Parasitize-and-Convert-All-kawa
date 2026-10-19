package org.tdddd.epca.impl.overworld.registry.items.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.tdddd.epca.impl.overworld.registry.items.armor.renderer.LivingArmorRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public final class LivingArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * 1.21.1 {@link ArmorItem} takes a {@link Holder} of the registered {@link ArmorMaterial}.
     *
     * <p>Passing the (still unresolved) {@link net.neoforged.neoforge.registries.DeferredHolder} is intentional and
     * safe: {@code ArmorItem} only stores it and dereferences it lazily for attribute modifiers / the equip sound.</p>
     */
    public LivingArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /**
     * Compatibility constructor for the pre-port call shape {@code new LivingArmorItem(new LivingArmorMaterial(), ...)}.
     * Maps to the registered {@link LivingArmorMaterial#LIVING_ARMOR} holder, which is the only living-armor material.
     */
    public LivingArmorItem(LivingArmorMaterial material, Type type, Properties properties) {
        this(LivingArmorMaterial.LIVING_ARMOR, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new LivingArmorRenderer();

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * 1.21.1: {@code isDamageable(ItemStack)} still exists (NeoForge {@code IItemExtension}); {@code canBeDepleted()}
     * was removed together with the old durability fields, so the override is gone. The item carries no
     * {@code MAX_DAMAGE} component, which has the same effect: it can never be depleted.
     */
    @Override
    public boolean isDamageable(ItemStack stack) {
        
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        
        entity.discard();
        return true;
    }
}
