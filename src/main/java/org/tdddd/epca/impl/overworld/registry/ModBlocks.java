package org.tdddd.epca.impl.overworld.registry;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.fluid.AcidSolutionBlock;
import org.tdddd.epca.impl.fluid.ModFluids;
import org.tdddd.epca.impl.overworld.registry.blocks.block.*;

public class ModBlocks {
    /**
     * 1.21.1 idiom: {@link DeferredRegister.Blocks} returns {@link net.neoforged.neoforge.registries.DeferredBlock}
     * holders, which also implement {@link net.minecraft.world.level.ItemLike}. The public field types are kept as
     * {@link DeferredHolder} so that every existing reference (e.g. {@code DeferredHolder<Block, Block> target =
     * ModBlocks.INFESTED_TALL_GRASS}) keeps compiling unchanged.
     */
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(epca.MODID);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static final DeferredHolder<Block, Block> INFESTED_DIRT = BLOCKS.register(
            "infested_dirt",
            () -> new InfestedDirt(BlockBehaviour.Properties.of().strength(0.7f, 0.7f).randomTicks().mapColor(DyeColor.BLACK))
    );

    public static final DeferredHolder<Block, Block> INFESTED_SAND = BLOCKS.register(
            "infested_sand",
            () -> new InfestedSand(BlockBehaviour.Properties.of().strength(0.7f, 0.7f).randomTicks().mapColor(DyeColor.WHITE))
    );

    public static final DeferredHolder<Block, Block> INFESTED_LOG = BLOCKS.register(
            "infested_log",
            () -> new InfestedLog(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_WOOD = BLOCKS.register(
            "infested_wood",
            () -> new InfestedWood(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STRIPPED_LOG = BLOCKS.register(
            "infested_stripped_log",
            () -> new InfestedStrippedLog(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STRIPPED_WOOD = BLOCKS.register(
            "infested_stripped_wood",
            () -> new InfestedStrippedWood(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_PLANKS = BLOCKS.register(
            "infested_planks",
            () -> new InfestedPlanks(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_PLANKS_SLAB = BLOCKS.register(
            "infested_planks_slab",
            () -> new InfestedPlanksSlab(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).noOcclusion().randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_PLANKS_FENCE = BLOCKS.register(
            "infested_planks_fence",
            () -> new InfestedPlanksFence(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).noOcclusion().randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_PLANKS_STAIRS = BLOCKS.register(
            "infested_planks_stairs",
            () -> new InfestedPlanksStairs(BlockBehaviour.Properties.of().strength(1.0f, 2.0f).noOcclusion().randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE = BLOCKS.register(
            "infested_stone",
            () -> new InfestedStone(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE_SLAB = BLOCKS.register(
            "infested_stone_slab",
            () -> new InfestedStoneSlab(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE_STAIRS = BLOCKS.register(
            "infested_stone_stairs",
            () -> new InfestedStoneStairs(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE_WALL = BLOCKS.register(
            "infested_stone_wall",
            () -> new InfestedStoneWall(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops().dynamicShape())
    );

    public static final DeferredHolder<Block, Block> INFESTED_COBBLESTONE = BLOCKS.register(
            "infested_cobblestone",
            () -> new InfestedCobblestone(BlockBehaviour.Properties.of().strength(2.0f, 6.0f).randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_COBBLESTONE_SLAB = BLOCKS.register(
            "infested_cobblestone_slab",
            () -> new InfestedCobblestoneSlab(BlockBehaviour.Properties.of().strength(2.0f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_COBBLESTONE_STAIRS = BLOCKS.register(
            "infested_cobblestone_stairs",
            () -> new InfestedCobblestoneStairs(BlockBehaviour.Properties.of().strength(2.0f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_COBBLESTONE_WALL = BLOCKS.register(
            "infested_cobblestone_wall",
            () -> new InfestedCobblestoneWall(BlockBehaviour.Properties.of().strength(2.0f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops().dynamicShape())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE_BRICKS = BLOCKS.register(
            "infested_stone_bricks",
            () -> new InfestedStoneBricks(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE_BRICKS_SLAB = BLOCKS.register(
            "infested_stone_bricks_slab",
            () -> new InfestedStoneBricksSlab(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE_BRICKS_STAIRS = BLOCKS.register(
            "infested_stone_bricks_stairs",
            () -> new InfestedStoneBricksStairs(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_STONE_BRICKS_WALL = BLOCKS.register(
            "infested_stone_bricks_wall",
            () -> new InfestedStoneBricksWall(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops().dynamicShape())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CRACKED_STONE_BRICKS = BLOCKS.register(
            "infested_cracked_stone_bricks",
            () -> new InfestedCrackedStoneBricks(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CHISELED_STONE_BRICKS = BLOCKS.register(
            "infested_chiseled_stone_bricks",
            () -> new InfestedChiseledStoneBricks(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_POLISHED_STONE = BLOCKS.register(
            "infested_polished_stone",
            () -> new InfestedPolishedStone(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_POLISHED_STONE_SLAB = BLOCKS.register(
            "infested_polished_stone_slab",
            () -> new InfestedPolishedStoneSlab(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_POLISHED_STONE_STAIRS = BLOCKS.register(
            "infested_polished_stone_stairs",
            () -> new InfestedPolishedStoneStairs(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().randomTicks().mapColor(DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SANDSTONE = BLOCKS.register(
            "infested_sandstone",
            () -> new InfestedSandstone(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SANDSTONE_SLAB = BLOCKS.register(
            "infested_sandstone_slab",
            () -> new InfestedSandstoneSlab(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).noOcclusion().randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SANDSTONE_STAIRS = BLOCKS.register(
            "infested_sandstone_stairs",
            () -> new InfestedSandstoneStairs(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).noOcclusion().randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SANDSTONE_WALL = BLOCKS.register(
            "infested_sandstone_wall",
            () -> new InfestedSandstoneWall(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).noOcclusion().randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops().dynamicShape())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CHISELED_RED_SANDSTONE = BLOCKS.register(
            "infested_chiseled_red_sandstone",
            () -> new InfestedChiseledRedSandstone(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CHISELED_SANDSTONE = BLOCKS.register(
            "infested_chiseled_sandstone",
            () -> new InfestedChiseledSandstone(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SMOOTH_SANDSTONE = BLOCKS.register(
            "infested_smooth_sandstone",
            () -> new InfestedSmoothSandstone(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SMOOTH_SANDSTONE_SLAB = BLOCKS.register(
            "infested_smooth_sandstone_slab",
            () -> new InfestedSmoothSandstoneSlab(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).noOcclusion().randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SMOOTH_SANDSTONE_STAIRS = BLOCKS.register(
            "infested_smooth_sandstone_stairs",
            () -> new InfestedSmoothSandstoneStairs(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).noOcclusion().randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CUT_SANDSTONE = BLOCKS.register(
            "infested_cut_sandstone",
            () -> new InfestedCutSandstone(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CUT_SANDSTONE_SLAB = BLOCKS.register(
            "infested_cut_sandstone_slab",
            () -> new InfestedCutSandstoneSlab(BlockBehaviour.Properties.of().strength(0.8f, 0.8f).noOcclusion().randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_COAL_ORE = BLOCKS.register(
            "infested_coal_ore",
            () -> new InfestedCoalOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.BLACK).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_COPPER_ORE = BLOCKS.register(
            "infested_copper_ore",
            () -> new InfestedCopperOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.ORANGE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_IRON_ORE = BLOCKS.register(
            "infested_iron_ore",
            () -> new InfestedIronOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_GOLD_ORE = BLOCKS.register(
            "infested_gold_ore",
            () -> new InfestedGoldOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.YELLOW).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_LAPIS_ORE = BLOCKS.register(
            "infested_lapis_ore",
            () -> new InfestedLapisOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.BLUE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_REDSTONE_ORE = BLOCKS.register(
            "infested_redstone_ore",
            () -> new InfestedRedstoneOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.RED).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_EMERALD_ORE = BLOCKS.register(
            "infested_emerald_ore",
            () -> new InfestedEmeraldOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.GREEN).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_DIAMOND_ORE = BLOCKS.register(
            "infested_diamond_ore",
            () -> new InfestedDiamondOre(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).randomTicks().mapColor(DyeColor.CYAN).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SNOW = BLOCKS.register(
            "infested_snow",
            () -> new InfestedSnow(BlockBehaviour.Properties.of().strength(0.1f, 0.1f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SNOW_BLOCK = BLOCKS.register(
            "infested_snow_block",
            () -> new InfestedSnowBlock(BlockBehaviour.Properties.of().strength(0.1f, 0.1f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_GRASS = BLOCKS.register(
            "infested_grass",
            () -> new InfestedGrass(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SHORT_GRASS).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_FERN = BLOCKS.register(
            "infested_fern",
            () -> new InfestedFern(BlockBehaviour.Properties.ofLegacyCopy(Blocks.FERN).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SWEET_BERRY_BUSH = BLOCKS.register(
            "infested_sweet_berry_bush",
            () -> new InfestedSweetBerryBush(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SWEET_BERRY_BUSH).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_REMAINS_SMALL = BLOCKS.register(
            "infested_remains_small",
            InfestedRemainsSmall::new
    );

    public static final DeferredHolder<Block, Block> INFESTED_REMAINS_MEDIUM = BLOCKS.register(
            "infested_remains_medium",
            InfestedRemainsMedium::new
    );

    public static final DeferredHolder<Block, Block> INFESTED_REMAINS_LARGE = BLOCKS.register(
            "infested_remains_large",
            InfestedRemainsLarge::new
    );

    public static final DeferredHolder<Block, Block> INFESTED_RESIDUE = BLOCKS.register(
            "infested_residue",
            InfestedResidue::new
    );

    public static final DeferredHolder<Block, Block> INFESTED_VINE = BLOCKS.register(
            "infested_vine",
            InfestedVine::new);

    public static final DeferredHolder<Block, Block> SWALLOW_CYST = BLOCKS.register(
            "swallow_cyst",
            SwallowCyst::new
    );

    public static final DeferredHolder<Block, Block> INFESTED_LEAVES = BLOCKS.register("infested_leaves",
            () -> new InfestedLeaves(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SPRUCE_LEAVES)
                    .strength(0.2F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, entity) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .mapColor(DyeColor.PINK)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> INFESTED_FLOWERING_LEAVES = BLOCKS.register("infested_flowering_leaves",
            () -> new InfestedFloweringLeaves(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SPRUCE_LEAVES)
                    .strength(0.2F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, entity) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .mapColor(DyeColor.PINK)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, LiquidBlock> ACID_SOLUTION_BLOCK = BLOCKS.register(
            "acid_solution",
            () -> new AcidSolutionBlock(
                    ModFluids.ACID_SOLUTION.get(),
                    BlockBehaviour.Properties.ofLegacyCopy(Blocks.LAVA)
                            .mapColor(net.minecraft.world.level.material.MapColor.COLOR_GREEN)
                            .lightLevel(state -> 0)
            )
    );

    public static final DeferredHolder<Block, Block> INFESTED_INFESTED_COBBLESTONE = BLOCKS.register(
            "infested_infested_cobblestone",
            () -> new InfestedInfestedCobblestone(BlockBehaviour.Properties.of().strength(1.0f, 0.75f).randomTicks().mapColor(DyeColor.LIGHT_GRAY))
    );

    public static final DeferredHolder<Block, Block> INFESTED_INFESTED_STONE = BLOCKS.register(
            "infested_infested_stone",
            () -> new InfestedInfestedStone(BlockBehaviour.Properties.of().strength(0.75f, 0.75f).randomTicks().mapColor(DyeColor.LIGHT_GRAY))
    );

    public static final DeferredHolder<Block, Block> INFESTED_INFESTED_STONE_BRICKS = BLOCKS.register(
            "infested_infested_stone_bricks",
            () -> new InfestedInfestedStoneBricks(BlockBehaviour.Properties.of().strength(0.75f, 0.75f).randomTicks().mapColor(DyeColor.LIGHT_GRAY))
    );

    public static final DeferredHolder<Block, Block> INFESTED_INFESTED_CRACKED_STONE_BRICKS = BLOCKS.register(
            "infested_infested_cracked_stone_bricks",
            () -> new InfestedInfestedCrackedStoneBricks(BlockBehaviour.Properties.of().strength(0.75f, 0.75f).randomTicks().mapColor(DyeColor.LIGHT_GRAY))
    );

    public static final DeferredHolder<Block, Block> INFESTED_INFESTED_CHISELED_STONE_BRICKS = BLOCKS.register(
            "infested_infested_chiseled_stone_bricks",
            () -> new InfestedInfestedChiseledStoneBricks(BlockBehaviour.Properties.of().strength(0.75f, 0.75f).randomTicks().mapColor(DyeColor.LIGHT_GRAY))
    );

    public static final DeferredHolder<Block, Block> INFESTED_NETHERSEA_BRAND_GROWN = BLOCKS.register(
            "infested_nethersea_brand_grown",
            () -> new InfestedNetherseaBrandGrown(BlockBehaviour.Properties.of().strength(0.75f, 2.0f).randomTicks().mapColor(DyeColor.PURPLE).pushReaction(PushReaction.DESTROY).noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_NETHERSEA_BRAND_SOLID = BLOCKS.register(
            "infested_nethersea_brand_solid",
            () -> new InfestedNetherseaBrandSolid(BlockBehaviour.Properties.of().strength(2.0f, 5.0f).randomTicks().mapColor(DyeColor.PURPLE))
    );

    public static final DeferredHolder<Block, Block> INFESTED_POINTED_DRIPSTONE = BLOCKS.register(
            "infested_pointed_dripstone",
            () -> new InfestedPointedDripstone(BlockBehaviour.Properties.ofLegacyCopy(Blocks.POINTED_DRIPSTONE).mapColor(DyeColor.LIGHT_GRAY))
    );

    

    public static final DeferredHolder<Block, Block> HIVE_NODE = BLOCKS.register(
            "hive_node",
            () -> new HiveNode(BlockBehaviour.Properties.of().strength(15.0F, 25.0F).sound(SoundType.MUDDY_MANGROVE_ROOTS).mapColor(DyeColor.GREEN).requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> BECKON_CORE = BLOCKS.register(
            "beckon_core",
            () -> new BeckonCore(BlockBehaviour.Properties.of().strength(4.0F, 6.0F).sound(SoundType.MUDDY_MANGROVE_ROOTS).mapColor(DyeColor.GREEN)));

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_STONE = BLOCKS.register(
            "infested_heavy_stone",
            () -> new InfestedHeavyStone(BlockBehaviour.Properties.of().strength(3.0f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_INFESTED_HEAVY_STONE = BLOCKS.register(
            "infested_infested_heavy_stone",
            () -> new InfestedInfestedHeavyStone(BlockBehaviour.Properties.of().strength(1.5f, 0.75f).randomTicks().mapColor(DyeColor.GRAY))
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_COAL_ORE = BLOCKS.register(
            "infested_heavy_coal_ore",
            () -> new InfestedHeavyCoalOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.BLACK).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_COPPER_ORE = BLOCKS.register(
            "infested_heavy_copper_ore",
            () -> new InfestedHeavyCopperOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.ORANGE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_IRON_ORE = BLOCKS.register(
            "infested_heavy_iron_ore",
            () -> new InfestedHeavyIronOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.WHITE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_GOLD_ORE = BLOCKS.register(
            "infested_heavy_gold_ore",
            () -> new InfestedHeavyGoldOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.YELLOW).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_LAPIS_ORE = BLOCKS.register(
            "infested_heavy_lapis_ore",
            () -> new InfestedHeavyLapisOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.BLUE).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_REDSTONE_ORE = BLOCKS.register(
            "infested_heavy_redstone_ore",
            () -> new InfestedHeavyRedstoneOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.RED).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_EMERALD_ORE = BLOCKS.register(
            "infested_heavy_emerald_ore",
            () -> new InfestedHeavyEmeraldOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.GREEN).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_DIAMOND_ORE = BLOCKS.register(
            "infested_heavy_diamond_ore",
            () -> new InfestedHeavyDiamondOre(BlockBehaviour.Properties.of().strength(4.5f, 3.0f).randomTicks().mapColor(DyeColor.CYAN).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_DUSTLIKE = BLOCKS.register(
            "infested_dustlike",
            () -> new InfestedDustlike(BlockBehaviour.Properties.of().strength(1.0f, 3.0f).randomTicks().mapColor(DyeColor.CYAN))
    );

    public static final DeferredHolder<Block, Block> INFESTED_PLANKSLIKE = BLOCKS.register(
            "infested_plankslike",
            () -> new InfestedPlankslike(BlockBehaviour.Properties.of().strength(2.0f, 4.0f).randomTicks().mapColor(DyeColor.GREEN))
    );

    public static final DeferredHolder<Block, Block> INFESTED_ROCKLIKE = BLOCKS.register(
            "infested_rocklike",
            () -> new InfestedRocklike(BlockBehaviour.Properties.of().strength(3.0f, 6.0f).randomTicks().mapColor(DyeColor.BLUE))
    );

    public static final DeferredHolder<Block, Block> INFESTED_METALLIKE = BLOCKS.register(
            "infested_metallike",
            () -> new InfestedMetallike(BlockBehaviour.Properties.of().strength(10.0f, 12.0f).randomTicks().mapColor(DyeColor.PURPLE))
    );

    public static final DeferredHolder<Block, Block> INFESTED_HARDLIKE = BLOCKS.register(
            "infested_hardlike",
            () -> new InfestedHardlike(BlockBehaviour.Properties.of().strength(60.0f, 1000.0f).randomTicks().mapColor(DyeColor.PURPLE))
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_COBBLESTONE = BLOCKS.register(
            "infested_heavy_cobblestone",
            () -> new InfestedHeavyCobblestone(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_COBBLESTONE_STAIRS = BLOCKS.register(
            "infested_heavy_cobblestone_stairs",
            () -> new InfestedHeavyCobblestoneStairs(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_COBBLESTONE_SLAB = BLOCKS.register(
            "infested_heavy_cobblestone_slab",
            () -> new InfestedHeavyCobblestoneSlab(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_COBBLESTONE_WALL = BLOCKS.register(
            "infested_heavy_cobblestone_wall",
            () -> new InfestedHeavyCobblestoneWall(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CHISELED_DEEPSLATE = BLOCKS.register(
            "infested_chiseled_deepslate",
            () -> new InfestedChiseledDeepslate(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_POLISHED_HEAVY_STONE = BLOCKS.register(
            "infested_polished_heavy_stone",
            () -> new InfestedPolishedHeavyStone(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_POLISHED_HEAVY_STONE_STAIRS = BLOCKS.register(
            "infested_polished_heavy_stone_stairs",
            () -> new InfestedPolishedHeavyStoneStairs(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_POLISHED_HEAVY_STONE_SLAB = BLOCKS.register(
            "infested_polished_heavy_stone_slab",
            () -> new InfestedPolishedHeavyStoneSlab(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_POLISHED_HEAVY_STONE_WALL = BLOCKS.register(
            "infested_polished_heavy_stone_wall",
            () -> new InfestedPolishedHeavyStoneWall(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_LILY_PAD = BLOCKS.register("infested_lily_pad",
            () -> new InfestedLilyPad(BlockBehaviour.Properties.ofLegacyCopy(Blocks.LILY_PAD).instabreak().pushReaction(PushReaction.DESTROY).randomTicks().noOcclusion().mapColor(DyeColor.LIGHT_GRAY)));

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_BRICKS = BLOCKS.register(
            "infested_heavy_bricks",
            () -> new InfestedHeavyBricks(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CRACKED_HEAVY_BRICKS = BLOCKS.register(
            "infested_cracked_heavy_bricks",
            () -> new InfestedCrackedHeavyBricks(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_BRICKS_STAIRS = BLOCKS.register(
            "infested_heavy_bricks_stairs",
            () -> new InfestedHeavyBricksStairs(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_BRICKS_SLAB = BLOCKS.register(
            "infested_heavy_bricks_slab",
            () -> new InfestedHeavyBricksSlab(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_BRICKS_WALL = BLOCKS.register(
            "infested_heavy_bricks_wall",
            () -> new InfestedHeavyBricksWall(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CARVED_PUMPKIN = BLOCKS.register(
            "infested_carved_pumpkin",
            () -> new InfestedCarvedPumpkin(BlockBehaviour.Properties.of().strength(1.0f, 1.0f).randomTicks().mapColor(DyeColor.BLUE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_PUMPKIN = BLOCKS.register(
            "infested_pumpkin",
            () -> new InfestedPumpkin(BlockBehaviour.Properties.of().strength(1.0f, 1.0f).randomTicks().mapColor(DyeColor.BLUE).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_MUDDY_MANGROVE_ROOTS = BLOCKS.register(
            "infested_muddy_mangrove_roots",
            () -> new InfestedMuddyMangroveRoots(BlockBehaviour.Properties.of().strength(0.7f, 0.7f).randomTicks().mapColor(DyeColor.PURPLE))
    );

    public static final DeferredHolder<Block, Block> INFESTED_TALL_GRASS = BLOCKS.register("infested_tall_grass", () -> new InfestedDoublePlantBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.LARGE_FERN).sound(Blocks.SHORT_GRASS.defaultBlockState().getSoundType())));

    public static final DeferredHolder<Block, Block> INFESTED_TALL_FERN = BLOCKS.register("infested_tall_fern", () -> new InfestedDoublePlantBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.LARGE_FERN).sound(Blocks.SHORT_GRASS.defaultBlockState().getSoundType())));

    public static final DeferredHolder<Block, Block> INFESTED_SHORT_GRASS = BLOCKS.register(
            "infested_short_grass",
            () -> new InfestedShortGrass(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SHORT_GRASS).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CACTUS = BLOCKS.register(
            "infested_cactus",
            () -> new InfestedCactus(BlockBehaviour.Properties.ofLegacyCopy(Blocks.CACTUS).strength(0.4f, 0.4f).randomTicks().mapColor(DyeColor.PURPLE).noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SUGAR_CANE = BLOCKS.register(
            "infested_sugar_cane",
            () -> new InfestedSugarCane(BlockBehaviour.Properties.of().instabreak().randomTicks().mapColor(DyeColor.PURPLE).noOcclusion().ignitedByLava())
    );

    public static final DeferredHolder<Block, InfestedSpiderWeb> INFESTED_SPIDER_WEB = BLOCKS.register("infested_spider_web",
            () -> new InfestedSpiderWeb(BlockBehaviour.Properties.of().strength(4.0F, 0.0F).randomTicks().mapColor(DyeColor.GREEN).noOcclusion().noCollission().isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).ignitedByLava()));

    public static final DeferredHolder<Block, InfestedSpiderWebBlood> INFESTED_SPIDER_WEB_BLOOD = BLOCKS.register("infested_spider_web_blood",
            () -> new InfestedSpiderWebBlood(BlockBehaviour.Properties.of().strength(4.0F, 0.0F).randomTicks().mapColor(DyeColor.RED).noOcclusion().noCollission().isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).ignitedByLava()));

    public static final DeferredHolder<Block, InfestedCaveSpiderWeb> INFESTED_CAVE_SPIDER_WEB = BLOCKS.register("infested_cave_spider_web",
            () -> new InfestedCaveSpiderWeb(BlockBehaviour.Properties.of().strength(4.0F, 0.0F).randomTicks().mapColor(DyeColor.CYAN).noOcclusion().noCollission().isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).ignitedByLava()));

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_TILES = BLOCKS.register(
            "infested_heavy_tiles",
            () -> new InfestedHeavyTiles(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_CRACKED_HEAVY_TILES = BLOCKS.register(
            "infested_cracked_heavy_tiles",
            () -> new InfestedCrackedHeavyTiles(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_TILES_STAIRS = BLOCKS.register(
            "infested_heavy_tiles_stairs",
            () -> new InfestedHeavyTilesStairs(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_TILES_SLAB = BLOCKS.register(
            "infested_heavy_tiles_slab",
            () -> new InfestedHeavyTilesSlab(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_HEAVY_TILES_WALL = BLOCKS.register(
            "infested_heavy_tiles_wall",
            () -> new InfestedHeavyTilesWall(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).randomTicks().mapColor(DyeColor.GRAY).requiresCorrectToolForDrops().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_MANGROVE_ROOTS = BLOCKS.register(
            "infested_mangrove_roots",
            () -> new InfestedMangroveRoots(BlockBehaviour.Properties.of().strength(0.7f, 0.7f).randomTicks().mapColor(DyeColor.PURPLE).ignitedByLava().noOcclusion())
    );

    public static final DeferredHolder<Block, Block> INFESTED_DEAD_BUSH = BLOCKS.register(
            "infested_dead_bush",
            () -> new InfestedDeadBush(BlockBehaviour.Properties.ofLegacyCopy(Blocks.DEAD_BUSH).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_MOSS_BLOCK = BLOCKS.register(
            "infested_moss_block",
            () -> new InfestedMossBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.MOSS_BLOCK).randomTicks())
    );

    public static final DeferredHolder<Block, Block> INFESTED_MOSS_CARPET = BLOCKS.register(
            "infested_moss_carpet",
            () -> new InfestedMossCarpet(BlockBehaviour.Properties.ofLegacyCopy(Blocks.MOSS_CARPET).randomTicks())
    );

    public static final DeferredHolder<Block, Block> INFESTED_BUSH = BLOCKS.register(
            "infested_bush",
            () -> new InfestedBush(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SHORT_GRASS).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_SHORT_DRY_GRASS = BLOCKS.register(
            "infested_short_dry_grass",
            () -> new InfestedShortDryGrass(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SHORT_GRASS).ignitedByLava())
    );

    public static final DeferredHolder<Block, Block> INFESTED_TALL_DRY_GRASS = BLOCKS.register(
            "infested_tall_dry_grass",
            () -> new InfestedTallDryGrass(BlockBehaviour.Properties.ofLegacyCopy(Blocks.SHORT_GRASS).ignitedByLava())
    );
}