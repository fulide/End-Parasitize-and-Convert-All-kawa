package org.tdddd.epca.impl.overworld.registry.items.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModItems;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Armor material of the living armor.
 *
 * <p>1.21.1 turned {@link ArmorMaterial} into a record that must live in {@code Registries.ARMOR_MATERIAL} and be
 * referenced through a {@link Holder}. The old class therefore no longer implements the interface; it now owns the
 * registration and exposes the {@link Holder} the {@link LivingArmorItem}s need.</p>
 *
 * <p>Numbers are carried over unchanged. The 1.20.1 implementation indexed its arrays with
 * {@code type.getSlot().getIndex()}, which in 1.21.1 is the <em>per-type</em> index (FEET 0, LEGS 1, CHEST 2, HEAD 3),
 * so the old arrays {@code PROTECTION_VALUES = {4, 8, 10, 4}} map to boots 4, leggings 8, chestplate 10, helmet 4 and
 * {@code BASE_DURABILITY = {858, 1056, 990, 726}} to boots 858, leggings 1056, chestplate 990, helmet 726. The living
 * armor is not damageable, so the durability values are informational only (kept verbatim for reference).</p>
 *
 * <p><b>Registration:</b> {@code epca.java} must call {@code LivingArmorMaterial.ARMOR_MATERIALS.register(modEventBus)}.
 * Passing the unresolved {@link DeferredHolder} to {@code ArmorItem} is safe: vanilla only dereferences the holder when
 * attribute modifiers / the equip sound are queried, long after registration finished.</p>
 */
public final class LivingArmorMaterial {
    /** Old per-slot durability table; unused by 1.21.1 (durability is a data component now) but kept for reference. */
    public static final int[] BASE_DURABILITY = new int[]{858, 1056, 990, 726};
    /** Old protection table, indexed by {@code ArmorItem.Type#getSlot()#getIndex()}. */
    public static final int[] PROTECTION_VALUES = new int[]{4, 8, 10, 4};

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, epca.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LIVING_ARMOR =
            ARMOR_MATERIALS.register("living_armor", LivingArmorMaterial::createLivingArmorMaterial);

    /**
     * No-op instance kept only so existing call sites written as {@code new LivingArmorMaterial()} keep compiling;
     * the actual material is the registered {@link #LIVING_ARMOR} value. Passing such an instance to
     * {@link LivingArmorItem} resolves to the same registered holder.
     */
    public LivingArmorMaterial() {
    }

    /** The holder the living armor items are built with. */
    public static Holder<ArmorMaterial> holder() {
        return LIVING_ARMOR;
    }

    private static ArmorMaterial createLivingArmorMaterial() {
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        // Old lookup: PROTECTION_VALUES[type.getSlot().getIndex()]
        defense.put(ArmorItem.Type.BOOTS, PROTECTION_VALUES[0]);
        defense.put(ArmorItem.Type.LEGGINGS, PROTECTION_VALUES[1]);
        defense.put(ArmorItem.Type.CHESTPLATE, PROTECTION_VALUES[2]);
        defense.put(ArmorItem.Type.HELMET, PROTECTION_VALUES[3]);
        // The old table would also have answered BODY with index 0; kept so the record is complete.
        defense.put(ArmorItem.Type.BODY, PROTECTION_VALUES[0]);

        return new ArmorMaterial(
                defense,
                10, // old getEnchantmentValue()
                SoundEvents.ARMOR_EQUIP_LEATHER, // old getEquipSound()
                // Lazy: ModItems may not be registered yet when this material is created.
                () -> Ingredient.of(ModItems.RESHAPE_SHELL.get()), // old getRepairIngredient()
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(epca.MODID, "living_armor"))),
                3.0F, // old getToughness()
                0.0F // old getKnockbackResistance()
        );
    }
}
