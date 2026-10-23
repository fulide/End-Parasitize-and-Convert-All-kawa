package org.tdddd.epca.impl.events;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.tdddd.epca.impl.overworld.registry.entities.EpcaEntityManager;
import org.tdddd.epca.impl.overworld.registry.entities.entity.special.BioTortIncarnation;
import org.tdddd.epca.impl.overworld.registry.entities.entity.special.Nullthing;
import org.tdddd.epca.impl.overworld.registry.entities.entity.special.YawningNya;
import org.tdddd.epca.impl.overworld.registry.entities.entity.infested.*;
import org.tdddd.epca.impl.overworld.registry.entities.entity.link.StageIBeckon;
import org.tdddd.epca.impl.overworld.registry.entities.entity.link.StageIIBeckon;
import org.tdddd.epca.impl.overworld.registry.entities.entity.onesent.*;
import org.tdddd.epca.impl.overworld.registry.entities.entity.poverty.*;
import org.tdddd.epca.impl.overworld.registry.entities.entity.reshape.ReshapeLongarms;
import org.tdddd.epca.impl.overworld.registry.entities.entity.reshape.ReshapeYelloweye;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.ModEntities;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = epca.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityEvents {

    /**
     * All entity attributes are now routed through EpcaEntityManager.
     * ModEntities.registerMobAttributes() adds each type  EpcaEntityManager.registerMobNoRender()
     * stores the attribute supplier. Then this single call processes them all.
     */
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        EpcaEntityManager.createAttributes(event);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.CURBUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(Curbug::checkBuglinSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.RIPPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(Ripper::checkRupterSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.SMALL_INCOMPLETE_FORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(SmallIncompleteForm::checkSmallIncompleteFormSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedZombie::checkInfestedZombieSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_ZOMBIE_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingZombieHead::checkWalkingZombieHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_HUSK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedHusk::checkInfestedHuskSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_HUSK_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingHuskHead::checkWalkingHuskHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_DROWNED_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingDrownedHead::checkWalkingDrownedHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_DROWNED.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedDrowned::checkInfestedDrownedSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.MEDIUM_INCOMPLETE_FORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(MediumIncompleteForm::checkMediumIncompleteFormSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.BIOMASS_SMALL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(BiomassSmall::checkBiomassSmallSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.STAGE_I_BECKON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(StageIBeckon::checkStageIBeckonSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_PILLAGER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedPillager::checkInfestedPillagerSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_PILLAGER_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingPillagerHead::checkWalkingPillagerHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_VINDICATOR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedVindicator::checkInfestedVindicatorSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_VINDICATOR_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingVindicatorHead::checkWalkingVindicatorHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_VILLAGER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedVillager::checkInfestedVillagerSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_VILLAGER_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingVillagerHead::checkWalkingVillagerHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_ZOMBIE_VILLAGER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedZombieVillager::checkInfestedZombieVillagerSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_ZOMBIE_VILLAGER_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingZombieVillagerHead::checkWalkingZombieVillagerHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.FINS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(Fins::checkFinsSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_PIG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedPig::checkInfestedPigSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_PIG_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingPigHead::checkWalkingPigHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_SHEEP.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedSheep::checkInfestedSheepSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_SHEEP_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingSheepHead::checkWalkingSheepHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LARGE_INCOMPLETE_FORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(LargeIncompleteForm::checkLargeIncompleteFormSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedCow::checkInfestedCowSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_COW_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingCowHead::checkWalkingCowHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.NULLTHING.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(Nullthing::checkNullthingSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.MOZZIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(Mozzie::checkGnatSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.YAWNING_NYA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(YawningNya::checkNyaSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.BIO_TORT_INCARNATION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(BioTortIncarnation::checkBioTortSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_SLIME_SIZE0.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedSlimeSize0::checkInfestedSlimeSize0SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_SLIME_SIZE1.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedSlimeSize1::checkInfestedSlimeSize1SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_SLIME_SIZE3.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedSlimeSize3::checkInfestedSlimeSize3SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LIVING_FLESH_SIZE0.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(LivingFleshSize0::checkLivingFleshSize0SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LIVING_FLESH_SIZE1.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(LivingFleshSize1::checkLivingFleshSize1SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LIVING_FLESH_SIZE2.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(LivingFleshSize2::checkLivingFleshSize2SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LIVING_FLESH_SIZE3.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(LivingFleshSize3::checkLivingFleshSize3SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LIVING_FLESH_SIZE4.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(LivingFleshSize4::checkLivingFleshSize4SpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_WOLF.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedWolf::checkInfestedWolfSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_WOLF_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingWolfHead::checkWalkingWolfHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.RESHAPE_LONGARMS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(ReshapeLongarms::checkReshapeLongarmsSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.BIOMASS_MEDIUM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(BiomassMedium::checkBiomassMediumSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.STAGE_II_BECKON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(StageIIBeckon::checkStageIIBeckonSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_CHICKEN_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingChickenHead::checkWalkingChickenHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_CHICKEN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedChicken::checkInfestedChickenSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.FLYING_CARRIER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(FlyingCarrier::checkFlyingCarrierSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_ENDERMAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedEnderman::checkInfestedEndermanSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_ENDERMAN_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingEndermanHead::checkWalkingEndermanHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_ENDERMITE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedEndermite::checkInfestedEndermiteSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_SILVERFISH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedSilverfish::checkInfestedSilverfishSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.LIGHT_CARRIER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(LightCarrier::checkLightCarrierSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_SKELETON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedSkeleton::checkInfestedSkeletonSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_SKELETON_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingSkeletonHead::checkWalkingSkeletonHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.RESHAPE_YELLOWEYE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(ReshapeYelloweye::checkReshapeYelloweyeSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_FOX.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedFox::checkInfestedFoxSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_FOX_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingFoxHead::checkWalkingFoxHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_BAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedBat::checkInfestedBatSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.INFESTED_SPIDER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(InfestedSpider::checkInfestedSpiderSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WALKING_SPIDER_HEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, org.tdddd.epca.impl.utils.DifficultyGuards.peacefulGuard(WalkingSpiderHead::checkWalkingSpiderHeadSpawnRules), RegisterSpawnPlacementsEvent.Operation.OR);
    }
}

