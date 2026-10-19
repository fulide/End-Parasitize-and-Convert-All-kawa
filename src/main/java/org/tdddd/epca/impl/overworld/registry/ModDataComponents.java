package org.tdddd.epca.impl.overworld.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.tdddd.epca.impl.epca;

import java.util.List;
import java.util.function.Supplier;

/**
 * Data components that replace the 1.20.1 per-item {@code CompoundTag} state.
 *
 * <p>1.21.1 removed {@code ItemStack#getTag()}/{@code getOrCreateTag()}/{@code hasTag()}/{@code setTag()}; every piece
 * of per-stack state is now a registered {@link DataComponentType}. Each component is declared with a plain
 * {@link Codec} through {@link #DATA_COMPONENTS}, which NeoForge turns into a persistent <em>and</em>
 * network-synchronised component (the stream codec defaults to {@code ByteBufCodecs.fromCodecWithRegistries}).
 * Syncing matters here: the living-armor-box tooltip and the adaptation tooltip read these values on the client.</p>
 *
 * <p><b>Registration:</b> {@code DATA_COMPONENTS} must be attached to the mod event bus by {@code epca.java}
 * ({@code ModDataComponents.DATA_COMPONENTS.register(modEventBus);}). Without it every {@link #set} throws
 * "Registry Object not present".</p>
 *
 * <p>Old NBT keys are documented per component; they only matter for loading worlds saved by the 1.20.1 build (new
 * saves use the component ids below, prefixed with {@code epca:}).</p>
 */
public final class ModDataComponents {
    private ModDataComponents() {
    }

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, epca.MODID);

    /**
     * Adaptation stacks stored on a living-armor box or on an individual living-armor piece.
     * Old NBT key: {@code AdaptationCount} (int, both the box and the armor pieces used the same key).
     */
    public static final Supplier<DataComponentType<Integer>> ADAPTATION_COUNT =
            DATA_COMPONENTS.registerComponentType("adaptation_count", builder -> builder.persistent(Codec.INT));

    /**
     * Whether the living-armor box is currently "open" (its armor set is worn).
     * Old NBT key: {@code state} (boolean, living-armor box only).
     */
    public static final Supplier<DataComponentType<Boolean>> LIVING_ARMOR_BOX_STATE =
            DATA_COMPONENTS.registerComponentType("living_armor_box_state", builder -> builder.persistent(Codec.BOOL));

    /**
     * Biomass accumulated by the living-armor box (0..{@code LivingArmorBox.MAX_BIOMASS}).
     * Old NBT key: {@code Biomass} (int, living-armor box only).
     */
    public static final Supplier<DataComponentType<Integer>> LIVING_ARMOR_BOX_BIOMASS =
            DATA_COMPONENTS.registerComponentType("living_armor_box_biomass", builder -> builder.persistent(Codec.INT));

    /**
     * Modules stored inside the living-armor box. Each entry is a full {@link ItemStack} (count 1), exactly what the
     * old code wrote into the {@code StoredItems} list tag through {@code ItemStack#save}.
     * Old NBT key: {@code StoredItems} ({@code ListTag} of compound tags, living-armor box only).
     */
    public static final Supplier<DataComponentType<List<ItemStack>>> LIVING_ARMOR_BOX_STORED_ITEMS =
            DATA_COMPONENTS.registerComponentType("living_armor_box_stored_items",
                    builder -> builder.persistent(ItemStack.CODEC.listOf()));

    // ------------------------------------------------------------------------------------------------------------
    // Small typed helpers. They centralise the Supplier dereference so call sites read like the old tag accessors.
    // ------------------------------------------------------------------------------------------------------------

    /** Component value, or {@code fallback} when the component is absent. */
    public static <T> T get(ItemStack stack, Supplier<DataComponentType<T>> type, T fallback) {
        return stack.getOrDefault(type.get(), fallback);
    }

    /** Component value, or {@code null} when absent. */
    @Nullable
    public static <T> T get(ItemStack stack, Supplier<DataComponentType<T>> type) {
        return stack.get(type.get());
    }

    /** Writes (or, with {@code null}, removes) the component. */
    public static <T> void set(ItemStack stack, Supplier<DataComponentType<T>> type, @Nullable T value) {
        stack.set(type.get(), value);
    }

    /** {@code hasTag()}/{@code tag.contains(key)} equivalent. */
    public static boolean has(ItemStack stack, Supplier<? extends DataComponentType<?>> type) {
        return stack.has(type.get());
    }

    /** {@code tag.getInt(key)} with the old "absent means 0" semantics. */
    public static int getInt(ItemStack stack, Supplier<DataComponentType<Integer>> type) {
        return stack.getOrDefault(type.get(), 0);
    }

    /** {@code tag.putInt(key, value)}. */
    public static void setInt(ItemStack stack, Supplier<DataComponentType<Integer>> type, int value) {
        stack.set(type.get(), value);
    }

    /** {@code tag.getBoolean(key)} with the old "absent means false" semantics. */
    public static boolean getBoolean(ItemStack stack, Supplier<DataComponentType<Boolean>> type) {
        return stack.getOrDefault(type.get(), false);
    }

    /** {@code tag.putBoolean(key, value)}. */
    public static void setBoolean(ItemStack stack, Supplier<DataComponentType<Boolean>> type, boolean value) {
        stack.set(type.get(), value);
    }
}
