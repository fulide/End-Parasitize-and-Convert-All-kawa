package org.tdddd.epca.impl.overworld.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.fluid.ModFluids;
import org.tdddd.epca.impl.overworld.registry.blocks.block.*;
import org.tdddd.epca.impl.overworld.registry.items.item.*;
import org.tdddd.epca.impl.overworld.registry.items.item.InfestedCarvedPumpkin;

import java.util.function.Supplier;

public class ModItems {
    /**
     * 1.21.1 idiom: {@link DeferredRegister.Items} returns {@link DeferredItem} holders. Public fields stay typed as
     * {@link DeferredHolder} so existing call sites keep compiling; {@code DeferredItem} is a {@code DeferredHolder}
     * subtype.
     */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(epca.MODID);

    

    
    private static DeferredHolder<Item, Item> simpleItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    
    private static <T extends Item> DeferredItem<T> customItem(String name, Supplier<T> supplier) {
        return ITEMS.register(name, supplier);
    }

    
    private static DeferredHolder<Item, Item> spawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> entitySupplier) {
        return ITEMS.register(name + "_spawn_egg",
                () -> new DeferredSpawnEggItem(entitySupplier, -1, -1, new Item.Properties()));
    }

    
    private static DeferredHolder<Item, Item> blockItem(String name, Supplier<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    
    private static <T extends BlockItem> DeferredItem<T> customBlockItem(String name, Supplier<T> supplier) {
        return ITEMS.register(name, supplier);
    }

    

    
    public static final DeferredHolder<Item, Item> CLUSTER = customItem("cluster", () -> new Cluster(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PARASITE_VISCERA = customItem("parasite_viscera", () -> new ParasiteViscera(new Item.Properties()));
    public static final DeferredHolder<Item, Item> FINS_FIN = customItem("fins_fin", () -> new FinsFin(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_BONE = customItem("infested_bone", () -> new InfestedBone(new Item.Properties()));
    public static final DeferredHolder<Item, Item> WEIRD_MINCED_FLESH = customItem("weird_minced_flesh", () -> new WeirdMincedFlesh(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_FLESH = customItem("infested_flesh", () -> new InfestedFlesh(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RESHAPE_FLESH = customItem("reshape_flesh", () -> new ReshapeFlesh(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RESHAPE_SHELL = customItem("reshape_shell", () -> new ReshapeShell(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TWISTED_BONE = customItem("twisted_bone", () -> new TwistedBone(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TIGHT_TENDONS = customItem("tight_tendons", () -> new TightTendons(new Item.Properties()));
    public static final DeferredHolder<Item, Item> GASBAG_DEBRIS = customItem("gasbag_debris", () -> new GasbagDebris(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BECKON_MEMBRANE = customItem("beckon_membrane", () -> new BeckonMembrane(new Item.Properties()));
    public static final DeferredHolder<Item, Item> DISEASED_HEART = customItem("diseased_heart", () -> new DiseasedHeart(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_COAL = customItem("infested_coal", () -> new InfestedCoal(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_RAW_COPPER = customItem("infested_raw_copper", () -> new InfestedRawCopper(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_RAW_IRON = customItem("infested_raw_iron", () -> new InfestedRawIron(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_RAW_GOLD = customItem("infested_raw_gold", () -> new InfestedRawGold(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_LAPIS_LAZULI = customItem("infested_lapis_lazuli", () -> new InfestedLapisLazuli(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_EMERALD = customItem("infested_emerald", () -> new InfestedEmerald(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_REDSTONE = customItem("infested_redstone", () -> new InfestedRedstone(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_DIAMOND = customItem("infested_diamond", () -> new InfestedDiamond(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_RUBBISH = customItem("infested_rubbish", () -> new InfestedRubbish(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_STICK = customItem("infested_stick", () -> new InfestedStick(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_SLIME_BALL = customItem("infested_slime_ball", () -> new InfestedSlimeBall(new Item.Properties()));
    public static final DeferredHolder<Item, Item> EPCA_ICON = customItem("epca_icon", () -> new EPCAIcon(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_NETHERSEA_BRAND_MOR = customItem("infested_nethersea_brand_mor", () -> new InfestedNetherseaBrandMor(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_NETHERSEA_ICECREAM = customItem("infested_nethersea_icecream", () -> new InfestedNetherseaIcecream(new Item.Properties().stacksTo(16).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> INFESTED_SWEET_BERRIES = customItem("infested_sweet_berries", () -> new InfestedSweetBerries(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COPPER_NUGGET = customItem("copper_nugget", () -> new CopperNugget(new Item.Properties()));

    
    public static final DeferredHolder<Item, Item> BLOODY_CLOCK = ITEMS.register("erosion_clock",
            () -> new BloodyClock(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> KILL_STICK = ITEMS.register("endless_wand",
            () -> new KillStick(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> INFESTED_ENDER_PEARL = ITEMS.register("infested_ender_pearl",
            () -> new InfestedEnderPearl(new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, Item> FEEDING_MODULE_I = ITEMS.register("feeding_module_i",
            () -> new AutomaticFeedingModuleI(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> AFTERIMAGE_MODULE = ITEMS.register("afterimage_module",
            () -> new AfterimageModule(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> FLESH_ARMOR_MODULE_I = ITEMS.register("flesh_armor_module_i",
            () -> new FleshArmorModuleI(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> NETHERITE_MODULE_I = ITEMS.register("netherite_module_i",
            () -> new NetheriteModuleI(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> FLIGHT_MODULE_I = ITEMS.register("flight_module_i",
            () -> new FlightModuleI(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> LIVING_ARMOR_BOX = ITEMS.register("living_armor_box",
            () -> new LivingArmorBox(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> EPCA_NOTE = ITEMS.register("epca_note",
            () -> new EPCANote(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> BIOMASS_COUNT_ICON = ITEMS.register("biomass_count_icon",
            () -> new BiomassCountIcon(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ENDER_BLADE_SCRAP = ITEMS.register("ender_blade_scrap",
            () -> new EnderBladeScrap(new Item.Properties().rarity(Rarity.UNCOMMON)));

    
    public static final DeferredHolder<Item, Item> WOODEN_SPEAR = ITEMS.register("wooden_spear",
            () -> new WoodenSpear(new Item.Properties().stacksTo(1).durability(64)));
    public static final DeferredHolder<Item, Item> STONE_SPEAR = ITEMS.register("stone_spear",
            () -> new StoneSpear(new Item.Properties().stacksTo(1).durability(131)));
    public static final DeferredHolder<Item, Item> FLINT_SPEAR = ITEMS.register("flint_spear",
            () -> new FlintSpear(new Item.Properties().stacksTo(1).durability(145)));
    public static final DeferredHolder<Item, Item> COPPER_SPEAR = ITEMS.register("copper_spear",
            () -> new CopperSpear(new Item.Properties().stacksTo(1).durability(190)));
    public static final DeferredHolder<Item, Item> IRON_SPEAR = ITEMS.register("iron_spear",
            () -> new IronSpear(new Item.Properties().stacksTo(1).durability(250)));
    public static final DeferredHolder<Item, Item> GOLDEN_SPEAR = ITEMS.register("golden_spear",
            () -> new GoldenSpear(new Item.Properties().stacksTo(1).durability(99)));
    public static final DeferredHolder<Item, Item> DIAMOND_SPEAR = ITEMS.register("diamond_spear",
            () -> new DiamondSpear(new Item.Properties().stacksTo(1).durability(1561)));
    public static final DeferredHolder<Item, Item> NETHERITE_SPEAR = ITEMS.register("netherite_spear",
            () -> new NetheriteSpear(new Item.Properties().stacksTo(1).durability(2031)));

    
    public static final DeferredHolder<Item, LivingArmorItem> LIVING_HELMET = ITEMS.register("living_helmet",
            () -> new LivingArmorItem(new LivingArmorMaterial(), ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredHolder<Item, LivingArmorItem> LIVING_CHESTPLATE = ITEMS.register("living_chestplate",
            () -> new LivingArmorItem(new LivingArmorMaterial(), ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredHolder<Item, LivingArmorItem> LIVING_LEGGINGS = ITEMS.register("living_leggings",
            () -> new LivingArmorItem(new LivingArmorMaterial(), ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredHolder<Item, LivingArmorItem> LIVING_BOOTS = ITEMS.register("living_boots",
            () -> new LivingArmorItem(new LivingArmorMaterial(), ArmorItem.Type.BOOTS, new Item.Properties()));

    
    public static final DeferredHolder<Item, Item> ACID_SOLUTION_BUCKET = ITEMS.register("acid_bucket",
            // 1.21.1: BucketItem takes the Fluid instance directly (it no longer accepts a Supplier).
            () -> new BucketItem(ModFluids.ACID_SOLUTION.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> INFESTED_SPIDER_WEB_PROJECTILE =
            ITEMS.register("infested_spider_web_projectile",
                    () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_SPIDER_WEB_BLOOD_PROJECTILE =
            ITEMS.register("infested_spider_web_blood_projectile",
                    () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_CAVE_SPIDER_WEB_PROJECTILE =
            ITEMS.register("infested_cave_spider_web_projectile",
                    () -> new Item(new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> BUGLIN_SPAWN_EGG = spawnEgg("curbug", ModEntities.CURBUG);
    public static final DeferredHolder<Item, Item> YAWNING_NYA_SPAWN_EGG = spawnEgg("yawning_nya", ModEntities.YAWNING_NYA);
    public static final DeferredHolder<Item, Item> BIO_TORT_INCARNATION_SPAWN_EGG = spawnEgg("bio_tort_incarnation", ModEntities.BIO_TORT_INCARNATION);
    public static final DeferredHolder<Item, Item> RUPTER_SPAWN_EGG = spawnEgg("ripper", ModEntities.RIPPER);
    public static final DeferredHolder<Item, Item> SMALL_INCOMPLETE_FORM_SPAWN_EGG = spawnEgg("small_incomplete_form", ModEntities.SMALL_INCOMPLETE_FORM);
    public static final DeferredHolder<Item, Item> MEDIUM_INCOMPLETE_FORM_SPAWN_EGG = spawnEgg("medium_incomplete_form", ModEntities.MEDIUM_INCOMPLETE_FORM);
    public static final DeferredHolder<Item, Item> INFESTED_ZOMBIE_SPAWN_EGG = spawnEgg("infested_zombie", ModEntities.INFESTED_ZOMBIE);
    public static final DeferredHolder<Item, Item> WALKING_ZOMBIE_HEAD_SPAWN_EGG = spawnEgg("walking_zombie_head", ModEntities.WALKING_ZOMBIE_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_HUSK_SPAWN_EGG = spawnEgg("infested_husk", ModEntities.INFESTED_HUSK);
    public static final DeferredHolder<Item, Item> WALKING_HUSK_HEAD_SPAWN_EGG = spawnEgg("walking_husk_head", ModEntities.WALKING_HUSK_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_DROWNED_SPAWN_EGG = spawnEgg("infested_drowned", ModEntities.INFESTED_DROWNED);
    public static final DeferredHolder<Item, Item> WALKING_DROWNED_HEAD_SPAWN_EGG = spawnEgg("walking_drowned_head", ModEntities.WALKING_DROWNED_HEAD);
    public static final DeferredHolder<Item, Item> BIOMASS_SMALL_SPAWN_EGG = spawnEgg("biomass_small", ModEntities.BIOMASS_SMALL);
    public static final DeferredHolder<Item, Item> INFESTED_PILLAGER_SPAWN_EGG = spawnEgg("infested_pillager", ModEntities.INFESTED_PILLAGER);
    public static final DeferredHolder<Item, Item> WALKING_PILLAGER_HEAD_SPAWN_EGG = spawnEgg("walking_pillager_head", ModEntities.WALKING_PILLAGER_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_VINDICATOR_SPAWN_EGG = spawnEgg("infested_vindicator", ModEntities.INFESTED_VINDICATOR);
    public static final DeferredHolder<Item, Item> WALKING_VINDICATOR_HEAD_SPAWN_EGG = spawnEgg("walking_vindicator_head", ModEntities.WALKING_VINDICATOR_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_VILLAGER_SPAWN_EGG = spawnEgg("infested_villager", ModEntities.INFESTED_VILLAGER);
    public static final DeferredHolder<Item, Item> WALKING_VILLAGER_HEAD_SPAWN_EGG = spawnEgg("walking_villager_head", ModEntities.WALKING_VILLAGER_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_ZOMBIE_VILLAGER_SPAWN_EGG = spawnEgg("infested_zombie_villager", ModEntities.INFESTED_ZOMBIE_VILLAGER);
    public static final DeferredHolder<Item, Item> WALKING_ZOMBIE_VILLAGER_HEAD_SPAWN_EGG = spawnEgg("walking_zombie_villager_head", ModEntities.WALKING_ZOMBIE_VILLAGER_HEAD);
    public static final DeferredHolder<Item, Item> FINS_SPAWN_EGG = spawnEgg("fins", ModEntities.FINS);
    public static final DeferredHolder<Item, Item> INFESTED_PIG_SPAWN_EGG = spawnEgg("infested_pig", ModEntities.INFESTED_PIG);
    public static final DeferredHolder<Item, Item> WALKING_PIG_HEAD_SPAWN_EGG = spawnEgg("walking_pig_head", ModEntities.WALKING_PIG_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_SHEEP_SPAWN_EGG = spawnEgg("infested_sheep", ModEntities.INFESTED_SHEEP);
    public static final DeferredHolder<Item, Item> WALKING_SHEEP_HEAD_SPAWN_EGG = spawnEgg("walking_sheep_head", ModEntities.WALKING_SHEEP_HEAD);
    public static final DeferredHolder<Item, Item> LARGE_INCOMPLETE_FORM_SPAWN_EGG = spawnEgg("large_incomplete_form", ModEntities.LARGE_INCOMPLETE_FORM);
    public static final DeferredHolder<Item, Item> INFESTED_COW_SPAWN_EGG = spawnEgg("infested_cow", ModEntities.INFESTED_COW);
    public static final DeferredHolder<Item, Item> WALKING_COW_HEAD_SPAWN_EGG = spawnEgg("walking_cow_head", ModEntities.WALKING_COW_HEAD);
    public static final DeferredHolder<Item, Item> NULLTHING_SPAWN_EGG = spawnEgg("nullthing", ModEntities.NULLTHING);
    public static final DeferredHolder<Item, Item> MOZZIE_SPAWN_EGG = spawnEgg("mozzie", ModEntities.MOZZIE);
    public static final DeferredHolder<Item, Item> INFESTED_WOLF_SPAWN_EGG = spawnEgg("infested_wolf", ModEntities.INFESTED_WOLF);
    public static final DeferredHolder<Item, Item> WALKING_WOLF_HEAD_SPAWN_EGG = spawnEgg("walking_wolf_head", ModEntities.WALKING_WOLF_HEAD);
    public static final DeferredHolder<Item, Item> RESHAPE_LONGARMS_SPAWN_EGG = spawnEgg("reshape_longarms", ModEntities.RESHAPE_LONGARMS);
    public static final DeferredHolder<Item, Item> BIOMASS_MEDIUM_SPAWN_EGG = spawnEgg("biomass_medium", ModEntities.BIOMASS_MEDIUM);
    public static final DeferredHolder<Item, Item> WALKING_CHICKEN_HEAD_SPAWN_EGG = spawnEgg("walking_chicken_head", ModEntities.WALKING_CHICKEN_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_CHICKEN_SPAWN_EGG = spawnEgg("infested_chicken", ModEntities.INFESTED_CHICKEN);
    public static final DeferredHolder<Item, Item> FLYING_CARRIER_SPAWN_EGG = spawnEgg("flying_carrier", ModEntities.FLYING_CARRIER);
    public static final DeferredHolder<Item, Item> INFESTED_ENDERMAN_SPAWN_EGG = spawnEgg("infested_enderman", ModEntities.INFESTED_ENDERMAN);
    public static final DeferredHolder<Item, Item> WALKING_ENDERMAN_HEAD_SPAWN_EGG = spawnEgg("walking_enderman_head", ModEntities.WALKING_ENDERMAN_HEAD);
    public static final DeferredHolder<Item, Item> INFESTED_ENDERMITE_SPAWN_EGG = spawnEgg("infested_endermite", ModEntities.INFESTED_ENDERMITE);
    public static final DeferredHolder<Item, Item> INFESTED_SILVERFISH_SPAWN_EGG = spawnEgg("infested_silverfish", ModEntities.INFESTED_SILVERFISH);
    public static final DeferredHolder<Item, Item> LIGHT_CARRIER_SPAWN_EGG = spawnEgg("light_carrier", ModEntities.LIGHT_CARRIER);
    public static final DeferredHolder<Item, Item> INFESTED_SKELETON_SPAWN_EGG = spawnEgg("infested_skeleton", ModEntities.INFESTED_SKELETON);
    public static final DeferredHolder<Item, Item> WALKING_SKELETON_HEAD_SPAWN_EGG = spawnEgg("walking_skeleton_head", ModEntities.WALKING_SKELETON_HEAD);
    public static final DeferredHolder<Item, Item> WALKING_FOX_HEAD_SPAWN_EGG = spawnEgg("walking_fox_head", ModEntities.WALKING_FOX_HEAD);
    public static final DeferredHolder<Item, Item> RESHAPE_YELLOWEYE_SPAWN_EGG = spawnEgg("reshape_yelloweye", ModEntities.RESHAPE_YELLOWEYE);
    public static final DeferredHolder<Item, Item> INFESTED_FOX_SPAWN_EGG = spawnEgg("infested_fox", ModEntities.INFESTED_FOX);
    public static final DeferredHolder<Item, Item> INFESTED_SLIME_SPAWN_EGG = ITEMS.register("infested_slime_spawn_egg", () -> new InfestedSlimeSpawnEgg(new Item.Properties()));
    public static final DeferredHolder<Item, Item> STAGE_I_BECKON_SPAWN_EGG = customItem("stage_i_beckon_spawn_egg", () -> new StageIBeckonSpawnEgg(new Item.Properties()));
    public static final DeferredHolder<Item, Item> STAGE_II_BECKON_SPAWN_EGG = customItem("stage_ii_beckon_spawn_egg", () -> new StageIIBeckonSpawnEgg(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LIVING_FLESH_SPAWN_EGG =
            ITEMS.register("living_flesh_spawn_egg",
                    () -> new DeferredSpawnEggItem(() -> ModEntities.LIVING_FLESH_SIZE0.get(), -1, -1, new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_BAT_SPAWN_EGG = spawnEgg("infested_bat", ModEntities.INFESTED_BAT);
    // Spiders: FOUR eggs, one per supplied texture. The cave forms are a synced Variant on the same
    // entity type, so each egg is a VariantSpawnEgg that writes its own variant onto the mob it
    // creates (a plain DeferredSpawnEggItem can only produce the default variant).
    // Variant ordinals: 0 = DEFAULT, 1 = BLOOD, 2 = CAVE.
    // The RegistryObject is passed ITSELF, never .get(): ModItems is populated during the ITEM
    // registration phase, which runs BEFORE entity types are registered, so resolving an entity
    // holder here throws "Registry Object not present: epca:..." and the mod fails to load.
    //
    // Variant rules (user spec):
    //   plain spider / plain head egg : 30 % BLOOD, otherwise DEFAULT, NEVER CAVE -> weights {55,30,0}
    //   cave spider / cave head egg   : always CAVE                              -> pinned ordinal 2
    /** Weights for the plain eggs, indexed by variant ordinal: DEFAULT 55, BLOOD 30, CAVE 0. */
    private static final int[] PLAIN_SPIDER_EGG_WEIGHTS = {55, 30, 0};

    public static final DeferredHolder<Item, Item> INFESTED_SPIDER_SPAWN_EGG =
            customItem("infested_spider_spawn_egg",
                    () -> new VariantSpawnEgg(ModEntities.INFESTED_SPIDER, PLAIN_SPIDER_EGG_WEIGHTS, new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_CAVE_SPIDER_SPAWN_EGG =
            customItem("infested_cave_spider_spawn_egg",
                    () -> new VariantSpawnEgg(ModEntities.INFESTED_SPIDER, 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> WALKING_SPIDER_HEAD_SPAWN_EGG =
            customItem("walking_spider_head_spawn_egg",
                    () -> new VariantSpawnEgg(ModEntities.WALKING_SPIDER_HEAD, PLAIN_SPIDER_EGG_WEIGHTS, new Item.Properties()));
    public static final DeferredHolder<Item, Item> WALKING_CAVE_SPIDER_HEAD_SPAWN_EGG =
            customItem("walking_cave_spider_head_spawn_egg",
                    () -> new VariantSpawnEgg(ModEntities.WALKING_SPIDER_HEAD, 2, new Item.Properties()));

    

    public static final DeferredHolder<Item, Item> INFESTED_REMAINS_SMALL = blockItem("infested_remains_small", ModBlocks.INFESTED_REMAINS_SMALL);
    public static final DeferredHolder<Item, Item> INFESTED_REMAINS_MEDIUM = blockItem("infested_remains_medium", ModBlocks.INFESTED_REMAINS_MEDIUM);
    public static final DeferredHolder<Item, Item> INFESTED_REMAINS_LARGE = blockItem("infested_remains_large", ModBlocks.INFESTED_REMAINS_LARGE);
    public static final DeferredHolder<Item, Item> INFESTED_DIRT = blockItem("infested_dirt", ModBlocks.INFESTED_DIRT);
    public static final DeferredHolder<Item, Item> INFESTED_SAND = blockItem("infested_sand", ModBlocks.INFESTED_SAND);
    public static final DeferredHolder<Item, Item> INFESTED_GRASS = blockItem("infested_grass", ModBlocks.INFESTED_GRASS);
    public static final DeferredHolder<Item, Item> INFESTED_FERN = blockItem("infested_fern", ModBlocks.INFESTED_FERN);
    public static final DeferredHolder<Item, Item> INFESTED_SWEET_BERRY_BUSH = blockItem("infested_sweet_berry_bush", ModBlocks.INFESTED_SWEET_BERRY_BUSH);
    public static final DeferredHolder<Item, Item> INFESTED_RESIDUE = blockItem("infested_residue", ModBlocks.INFESTED_RESIDUE);
    public static final DeferredHolder<Item, Item> INFESTED_LEAVES = blockItem("infested_leaves", ModBlocks.INFESTED_LEAVES);
    public static final DeferredHolder<Item, Item> INFESTED_FLOWERING_LEAVES = blockItem("infested_flowering_leaves", ModBlocks.INFESTED_FLOWERING_LEAVES);
    public static final DeferredHolder<Item, Item> INFESTED_VINE = blockItem("infested_vine", ModBlocks.INFESTED_VINE);
    public static final DeferredHolder<Item, Item> INFESTED_STONE = blockItem("infested_stone", ModBlocks.INFESTED_STONE);
    public static final DeferredHolder<Item, Item> INFESTED_STONE_SLAB = blockItem("infested_stone_slab", ModBlocks.INFESTED_STONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_STONE_STAIRS = blockItem("infested_stone_stairs", ModBlocks.INFESTED_STONE_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_STONE_WALL = blockItem("infested_stone_wall", ModBlocks.INFESTED_STONE_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_COBBLESTONE = blockItem("infested_cobblestone", ModBlocks.INFESTED_COBBLESTONE);
    public static final DeferredHolder<Item, Item> INFESTED_COBBLESTONE_SLAB = blockItem("infested_cobblestone_slab", ModBlocks.INFESTED_COBBLESTONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_COBBLESTONE_STAIRS = blockItem("infested_cobblestone_stairs", ModBlocks.INFESTED_COBBLESTONE_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_COBBLESTONE_WALL = blockItem("infested_cobblestone_wall", ModBlocks.INFESTED_COBBLESTONE_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_STONE_BRICKS = blockItem("infested_stone_bricks", ModBlocks.INFESTED_STONE_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_STONE_BRICKS_SLAB = blockItem("infested_stone_bricks_slab", ModBlocks.INFESTED_STONE_BRICKS_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_STONE_BRICKS_STAIRS = blockItem("infested_stone_bricks_stairs", ModBlocks.INFESTED_STONE_BRICKS_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_STONE_BRICKS_WALL = blockItem("infested_stone_bricks_wall", ModBlocks.INFESTED_STONE_BRICKS_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_CRACKED_STONE_BRICKS = blockItem("infested_cracked_stone_bricks", ModBlocks.INFESTED_CRACKED_STONE_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_CHISELED_STONE_BRICKS = blockItem("infested_chiseled_stone_bricks", ModBlocks.INFESTED_CHISELED_STONE_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_POLISHED_STONE = blockItem("infested_polished_stone", ModBlocks.INFESTED_POLISHED_STONE);
    public static final DeferredHolder<Item, Item> INFESTED_POLISHED_STONE_SLAB = blockItem("infested_polished_stone_slab", ModBlocks.INFESTED_POLISHED_STONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_POLISHED_STONE_STAIRS = blockItem("infested_polished_stone_stairs", ModBlocks.INFESTED_POLISHED_STONE_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_SANDSTONE = blockItem("infested_sandstone", ModBlocks.INFESTED_SANDSTONE);
    public static final DeferredHolder<Item, Item> INFESTED_SANDSTONE_SLAB = blockItem("infested_sandstone_slab", ModBlocks.INFESTED_SANDSTONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_SANDSTONE_STAIRS = blockItem("infested_sandstone_stairs", ModBlocks.INFESTED_SANDSTONE_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_SANDSTONE_WALL = blockItem("infested_sandstone_wall", ModBlocks.INFESTED_SANDSTONE_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_CHISELED_RED_SANDSTONE = blockItem("infested_chiseled_red_sandstone", ModBlocks.INFESTED_CHISELED_RED_SANDSTONE);
    public static final DeferredHolder<Item, Item> INFESTED_CHISELED_SANDSTONE = blockItem("infested_chiseled_sandstone", ModBlocks.INFESTED_CHISELED_SANDSTONE);
    public static final DeferredHolder<Item, Item> INFESTED_SMOOTH_SANDSTONE = blockItem("infested_smooth_sandstone", ModBlocks.INFESTED_SMOOTH_SANDSTONE);
    public static final DeferredHolder<Item, Item> INFESTED_SMOOTH_SANDSTONE_SLAB = blockItem("infested_smooth_sandstone_slab", ModBlocks.INFESTED_SMOOTH_SANDSTONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_SMOOTH_SANDSTONE_STAIRS = blockItem("infested_smooth_sandstone_stairs", ModBlocks.INFESTED_SMOOTH_SANDSTONE_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_CUT_SANDSTONE = blockItem("infested_cut_sandstone", ModBlocks.INFESTED_CUT_SANDSTONE);
    public static final DeferredHolder<Item, Item> INFESTED_CUT_SANDSTONE_SLAB = blockItem("infested_cut_sandstone_slab", ModBlocks.INFESTED_CUT_SANDSTONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_COAL_ORE = blockItem("infested_coal_ore", ModBlocks.INFESTED_COAL_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_COPPER_ORE = blockItem("infested_copper_ore", ModBlocks.INFESTED_COPPER_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_IRON_ORE = blockItem("infested_iron_ore", ModBlocks.INFESTED_IRON_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_GOLD_ORE = blockItem("infested_gold_ore", ModBlocks.INFESTED_GOLD_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_LAPIS_ORE = blockItem("infested_lapis_ore", ModBlocks.INFESTED_LAPIS_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_REDSTONE_ORE = blockItem("infested_redstone_ore", ModBlocks.INFESTED_REDSTONE_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_EMERALD_ORE = blockItem("infested_emerald_ore", ModBlocks.INFESTED_EMERALD_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_DIAMOND_ORE = blockItem("infested_diamond_ore", ModBlocks.INFESTED_DIAMOND_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_SNOW = blockItem("infested_snow", ModBlocks.INFESTED_SNOW);
    public static final DeferredHolder<Item, Item> INFESTED_SNOW_BLOCK = blockItem("infested_snow_block", ModBlocks.INFESTED_SNOW_BLOCK);
    public static final DeferredHolder<Item, Item> INFESTED_INFESTED_COBBLESTONE = blockItem("infested_infested_cobblestone", ModBlocks.INFESTED_INFESTED_COBBLESTONE);
    public static final DeferredHolder<Item, Item> INFESTED_INFESTED_STONE = blockItem("infested_infested_stone", ModBlocks.INFESTED_INFESTED_STONE);
    public static final DeferredHolder<Item, Item> INFESTED_INFESTED_STONE_BRICKS = blockItem("infested_infested_stone_bricks", ModBlocks.INFESTED_INFESTED_STONE_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_INFESTED_CRACKED_STONE_BRICKS = blockItem("infested_infested_cracked_stone_bricks", ModBlocks.INFESTED_INFESTED_CRACKED_STONE_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_INFESTED_CHISELED_STONE_BRICKS = blockItem("infested_infested_chiseled_stone_bricks", ModBlocks.INFESTED_INFESTED_CHISELED_STONE_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_NETHERSEA_BRAND_GROWN = blockItem("infested_nethersea_brand_grown", ModBlocks.INFESTED_NETHERSEA_BRAND_GROWN);
    public static final DeferredHolder<Item, Item> INFESTED_NETHERSEA_BRAND_SOLID = blockItem("infested_nethersea_brand_solid", ModBlocks.INFESTED_NETHERSEA_BRAND_SOLID);
    public static final DeferredHolder<Item, Item> SWALLOW_CYST = blockItem("swallow_cyst", ModBlocks.SWALLOW_CYST);
    public static final DeferredHolder<Item, Item> INFESTED_POINTED_DRIPSTONE = blockItem("infested_pointed_dripstone", ModBlocks.INFESTED_POINTED_DRIPSTONE);
    public static final DeferredHolder<Item, Item> BECKON_CORE = blockItem("beckon_core", ModBlocks.BECKON_CORE);
    public static final DeferredHolder<Item, Item> HIVE_NODE = blockItem("hive_node", ModBlocks.HIVE_NODE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_STONE = blockItem("infested_heavy_stone", ModBlocks.INFESTED_HEAVY_STONE);
    public static final DeferredHolder<Item, Item> INFESTED_INFESTED_HEAVY_STONE = blockItem("infested_infested_heavy_stone", ModBlocks.INFESTED_INFESTED_HEAVY_STONE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_COAL_ORE = blockItem("infested_heavy_coal_ore", ModBlocks.INFESTED_HEAVY_COAL_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_COPPER_ORE = blockItem("infested_heavy_copper_ore", ModBlocks.INFESTED_HEAVY_COPPER_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_IRON_ORE = blockItem("infested_heavy_iron_ore", ModBlocks.INFESTED_HEAVY_IRON_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_GOLD_ORE = blockItem("infested_heavy_gold_ore", ModBlocks.INFESTED_HEAVY_GOLD_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_LAPIS_ORE = blockItem("infested_heavy_lapis_ore", ModBlocks.INFESTED_HEAVY_LAPIS_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_REDSTONE_ORE = blockItem("infested_heavy_redstone_ore", ModBlocks.INFESTED_HEAVY_REDSTONE_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_EMERALD_ORE = blockItem("infested_heavy_emerald_ore", ModBlocks.INFESTED_HEAVY_EMERALD_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_DIAMOND_ORE = blockItem("infested_heavy_diamond_ore", ModBlocks.INFESTED_HEAVY_DIAMOND_ORE);
    public static final DeferredHolder<Item, Item> INFESTED_DUSTLIKE = blockItem("infested_dustlike", ModBlocks.INFESTED_DUSTLIKE);
    public static final DeferredHolder<Item, Item> INFESTED_PLANKSLIKE = blockItem("infested_plankslike", ModBlocks.INFESTED_PLANKSLIKE);
    public static final DeferredHolder<Item, Item> INFESTED_ROCKLIKE = blockItem("infested_rocklike", ModBlocks.INFESTED_ROCKLIKE);
    public static final DeferredHolder<Item, Item> INFESTED_METALLIKE = blockItem("infested_metallike", ModBlocks.INFESTED_METALLIKE);
    public static final DeferredHolder<Item, Item> INFESTED_HARDLIKE = blockItem("infested_hardlike", ModBlocks.INFESTED_HARDLIKE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_COBBLESTONE = blockItem("infested_heavy_cobblestone", ModBlocks.INFESTED_HEAVY_COBBLESTONE);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_COBBLESTONE_STAIRS = blockItem("infested_heavy_cobblestone_stairs", ModBlocks.INFESTED_HEAVY_COBBLESTONE_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_COBBLESTONE_SLAB = blockItem("infested_heavy_cobblestone_slab", ModBlocks.INFESTED_HEAVY_COBBLESTONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_COBBLESTONE_WALL = blockItem("infested_heavy_cobblestone_wall", ModBlocks.INFESTED_HEAVY_COBBLESTONE_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_CHISELED_DEEPSLATE = blockItem("infested_chiseled_deepslate", ModBlocks.INFESTED_CHISELED_DEEPSLATE);
    public static final DeferredHolder<Item, Item> INFESTED_POLISHED_HEAVY_STONE = blockItem("infested_polished_heavy_stone", ModBlocks.INFESTED_POLISHED_HEAVY_STONE);
    public static final DeferredHolder<Item, Item> INFESTED_POLISHED_HEAVY_STONE_STAIRS = blockItem("infested_polished_heavy_stone_stairs", ModBlocks.INFESTED_POLISHED_HEAVY_STONE_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_POLISHED_HEAVY_STONE_SLAB = blockItem("infested_polished_heavy_stone_slab", ModBlocks.INFESTED_POLISHED_HEAVY_STONE_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_POLISHED_HEAVY_STONE_WALL = blockItem("infested_polished_heavy_stone_wall", ModBlocks.INFESTED_POLISHED_HEAVY_STONE_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_LILY_PAD = ITEMS.register("infested_lily_pad", () -> new BlockItem(ModBlocks.INFESTED_LILY_PAD.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_CRACKED_HEAVY_BRICKS = blockItem("infested_cracked_heavy_bricks", ModBlocks.INFESTED_CRACKED_HEAVY_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_BRICKS = blockItem("infested_heavy_bricks", ModBlocks.INFESTED_HEAVY_BRICKS);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_BRICKS_STAIRS = blockItem("infested_heavy_bricks_stairs", ModBlocks.INFESTED_HEAVY_BRICKS_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_BRICKS_SLAB = blockItem("infested_heavy_bricks_slab", ModBlocks.INFESTED_HEAVY_BRICKS_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_BRICKS_WALL = blockItem("infested_heavy_bricks_wall", ModBlocks.INFESTED_HEAVY_BRICKS_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_CARVED_PUMPKIN = ITEMS.register("infested_carved_pumpkin", () -> new InfestedCarvedPumpkin(ModBlocks.INFESTED_CARVED_PUMPKIN.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_PUMPKIN = blockItem("infested_pumpkin", ModBlocks.INFESTED_PUMPKIN);
    public static final DeferredHolder<Item, Item> INFESTED_TALL_GRASS = ITEMS.register("infested_tall_grass", () -> new BlockItem(ModBlocks.INFESTED_TALL_GRASS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_TALL_FERN = ITEMS.register("infested_tall_fern", () -> new BlockItem(ModBlocks.INFESTED_TALL_FERN.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFESTED_SHORT_GRASS = blockItem("infested_short_grass", ModBlocks.INFESTED_SHORT_GRASS);
    public static final DeferredHolder<Item, Item> INFESTED_CACTUS = blockItem("infested_cactus", ModBlocks.INFESTED_CACTUS);
    public static final DeferredHolder<Item, Item> INFESTED_SUGAR_CANE = blockItem("infested_sugar_cane", ModBlocks.INFESTED_SUGAR_CANE);
    public static final DeferredHolder<Item, Item> INFESTED_SPIDER_WEB = blockItem("infested_spider_web", ModBlocks.INFESTED_SPIDER_WEB);
    public static final DeferredHolder<Item, Item> INFESTED_SPIDER_WEB_BLOOD = blockItem("infested_spider_web_blood", ModBlocks.INFESTED_SPIDER_WEB_BLOOD);
    public static final DeferredHolder<Item, Item> INFESTED_CAVE_SPIDER_WEB = blockItem("infested_cave_spider_web", ModBlocks.INFESTED_CAVE_SPIDER_WEB);
    public static final DeferredHolder<Item, Item> INFESTED_CRACKED_HEAVY_TILES = blockItem("infested_cracked_heavy_tiles", ModBlocks.INFESTED_CRACKED_HEAVY_TILES);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_TILES = blockItem("infested_heavy_tiles", ModBlocks.INFESTED_HEAVY_TILES);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_TILES_STAIRS = blockItem("infested_heavy_tiles_stairs", ModBlocks.INFESTED_HEAVY_TILES_STAIRS);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_TILES_SLAB = blockItem("infested_heavy_tiles_slab", ModBlocks.INFESTED_HEAVY_TILES_SLAB);
    public static final DeferredHolder<Item, Item> INFESTED_HEAVY_TILES_WALL = blockItem("infested_heavy_tiles_wall", ModBlocks.INFESTED_HEAVY_TILES_WALL);
    public static final DeferredHolder<Item, Item> INFESTED_MUDDY_MANGROVE_ROOTS = blockItem("infested_muddy_mangrove_roots", ModBlocks.INFESTED_MUDDY_MANGROVE_ROOTS);
    public static final DeferredHolder<Item, Item> INFESTED_DEAD_BUSH = blockItem("infested_dead_bush", ModBlocks.INFESTED_DEAD_BUSH);
    public static final DeferredHolder<Item, Item> INFESTED_MOSS_BLOCK = blockItem("infested_moss_block", ModBlocks.INFESTED_MOSS_BLOCK);
    public static final DeferredHolder<Item, Item> INFESTED_MOSS_CARPET = blockItem("infested_moss_carpet", ModBlocks.INFESTED_MOSS_CARPET);
    public static final DeferredHolder<Item, Item> INFESTED_BUSH = blockItem("infested_bush", ModBlocks.INFESTED_BUSH);
    public static final DeferredHolder<Item, Item> INFESTED_SHORT_DRY_GRASS = blockItem("infested_short_dry_grass", ModBlocks.INFESTED_SHORT_DRY_GRASS);
    public static final DeferredHolder<Item, Item> INFESTED_TALL_DRY_GRASS = blockItem("infested_tall_dry_grass", ModBlocks.INFESTED_TALL_DRY_GRASS);

    
    public static final DeferredHolder<Item, InfestedLog.InfestedLogItem> INFESTED_LOG =
            customBlockItem("infested_log", () -> new InfestedLog.InfestedLogItem(ModBlocks.INFESTED_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedWood.InfestedWoodItem> INFESTED_WOOD =
            customBlockItem("infested_wood", () -> new InfestedWood.InfestedWoodItem(ModBlocks.INFESTED_WOOD.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedStrippedLog.InfestedStrippedLogItem> INFESTED_STRIPPED_LOG =
            customBlockItem("infested_stripped_log", () -> new InfestedStrippedLog.InfestedStrippedLogItem(ModBlocks.INFESTED_STRIPPED_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedStrippedWood.InfestedStrippedWoodItem> INFESTED_STRIPPED_WOOD =
            customBlockItem("infested_stripped_wood", () -> new InfestedStrippedWood.InfestedStrippedWoodItem(ModBlocks.INFESTED_STRIPPED_WOOD.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedPlanks.InfestedPlanksItem> INFESTED_PLANKS =
            customBlockItem("infested_planks", () -> new InfestedPlanks.InfestedPlanksItem(ModBlocks.INFESTED_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedPlanksSlab.InfestedPlanksSlabItem> INFESTED_PLANKS_SLAB =
            customBlockItem("infested_planks_slab", () -> new InfestedPlanksSlab.InfestedPlanksSlabItem(ModBlocks.INFESTED_PLANKS_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedPlanksStairs.InfestedPlanksStairsItem> INFESTED_PLANKS_STAIRS =
            customBlockItem("infested_planks_stairs", () -> new InfestedPlanksStairs.InfestedPlanksStairsItem(ModBlocks.INFESTED_PLANKS_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedPlanksFence.InfestedPlanksItem> INFESTED_PLANKS_FENCE =
            customBlockItem("infested_planks_fence", () -> new InfestedPlanksFence.InfestedPlanksItem(ModBlocks.INFESTED_PLANKS_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, InfestedMangroveRoots.InfestedMangroveRootsItem> INFESTED_MANGROVE_ROOTS =
            customBlockItem("infested_mangrove_roots", () -> new InfestedMangroveRoots.InfestedMangroveRootsItem(ModBlocks.INFESTED_MANGROVE_ROOTS.get(), new Item.Properties()));
}