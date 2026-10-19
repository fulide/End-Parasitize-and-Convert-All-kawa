package org.tdddd.epca.impl.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModBlocks;
import org.tdddd.epca.impl.overworld.registry.ModItems;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, epca.MODID);

    /**
     * 1.21.1: {@code ForgeRegistries.Keys.FLUID_TYPES} is gone — the fluid-type registry now lives in
     * {@link NeoForgeRegistries#FLUID_TYPES}.
     */
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, epca.MODID);

    
    public static final DeferredHolder<FluidType, FluidType> ACID_SOLUTION_FLUID_TYPE = FLUID_TYPES.register(
            "acid_solution",
            AcidSolutionType::new
    );

    
    public static final DeferredHolder<Fluid, FlowingFluid> ACID_SOLUTION = FLUIDS.register(
            "acid_solution",
            () -> new AcidSolutionFluid.Source(createAcidSolutionProperties())
    );

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_ACID_SOLUTION = FLUIDS.register(
            "flowing_acid_solution",
            () -> new AcidSolutionFluid.Flowing(createAcidSolutionProperties())
    );

    
    private static final BlockBehaviour.Properties ACID_SOLUTION_BLOCK_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .replaceable()
            .noCollission()
            .strength(100.0F)
            .pushReaction(PushReaction.DESTROY)
            .noLootTable()
            .liquid();

    
    private static final Item.Properties ACID_SOLUTION_BUCKET_PROPERTIES = new Item.Properties()
            .craftRemainder(net.minecraft.world.item.Items.BUCKET)
            .stacksTo(1);

    
    private static BaseFlowingFluid.Properties createAcidSolutionProperties() {
        return new BaseFlowingFluid.Properties(
                ACID_SOLUTION_FLUID_TYPE,
                ACID_SOLUTION,
                FLOWING_ACID_SOLUTION
        )
                .bucket(() -> ModItems.ACID_SOLUTION_BUCKET.get())
                .block(() -> ModBlocks.ACID_SOLUTION_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .explosionResistance(100F)
                .tickRate(20);
    }
}
