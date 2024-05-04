package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ambient.EntityBat;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.animal.EntityFox;
import net.minecraft.world.entity.animal.EntityMushroomCow;
import net.minecraft.world.entity.animal.EntityOcelot;
import net.minecraft.world.entity.animal.EntityParrot;
import net.minecraft.world.entity.animal.EntityPolarBear;
import net.minecraft.world.entity.animal.EntityRabbit;
import net.minecraft.world.entity.animal.EntityTropicalFish;
import net.minecraft.world.entity.animal.EntityTurtle;
import net.minecraft.world.entity.animal.EntityWaterAnimal;
import net.minecraft.world.entity.animal.EntityWolf;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.EntityHorseSkeleton;
import net.minecraft.world.entity.animal.horse.EntityHorseZombie;
import net.minecraft.world.entity.monster.EntityDrowned;
import net.minecraft.world.entity.monster.EntityEndermite;
import net.minecraft.world.entity.monster.EntityGhast;
import net.minecraft.world.entity.monster.EntityGuardian;
import net.minecraft.world.entity.monster.EntityMagmaCube;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.monster.EntityMonsterPatrolling;
import net.minecraft.world.entity.monster.EntityPigZombie;
import net.minecraft.world.entity.monster.EntitySilverfish;
import net.minecraft.world.entity.monster.EntitySkeletonStray;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.entity.monster.EntityStrider;
import net.minecraft.world.entity.monster.EntityZombieHusk;
import net.minecraft.world.entity.monster.hoglin.EntityHoglin;
import net.minecraft.world.entity.monster.piglin.EntityPiglin;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.levelgen.HeightMap;

public class EntityPositionTypes {

    private static final Map<EntityTypes<?>, EntityPositionTypes.a> DATA_BY_TYPE = Maps.newHashMap();

    public EntityPositionTypes() {}

    private static <T extends EntityInsentient> void register(EntityTypes<T> entitytypes, SpawnPlacementType spawnplacementtype, HeightMap.Type heightmap_type, EntityPositionTypes.b<T> entitypositiontypes_b) {
        EntityPositionTypes.a entitypositiontypes_a = (EntityPositionTypes.a) EntityPositionTypes.DATA_BY_TYPE.put(entitytypes, new EntityPositionTypes.a(heightmap_type, spawnplacementtype, entitypositiontypes_b));

        if (entitypositiontypes_a != null) {
            throw new IllegalStateException("Duplicate registration for type " + String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entitytypes)));
        }
    }

    public static SpawnPlacementType getPlacementType(EntityTypes<?> entitytypes) {
        EntityPositionTypes.a entitypositiontypes_a = (EntityPositionTypes.a) EntityPositionTypes.DATA_BY_TYPE.get(entitytypes);

        return entitypositiontypes_a == null ? SpawnPlacementTypes.NO_RESTRICTIONS : entitypositiontypes_a.placement;
    }

    public static boolean isSpawnPositionOk(EntityTypes<?> entitytypes, IWorldReader iworldreader, BlockPosition blockposition) {
        return getPlacementType(entitytypes).isSpawnPositionOk(iworldreader, blockposition, entitytypes);
    }

    public static HeightMap.Type getHeightmapType(@Nullable EntityTypes<?> entitytypes) {
        EntityPositionTypes.a entitypositiontypes_a = (EntityPositionTypes.a) EntityPositionTypes.DATA_BY_TYPE.get(entitytypes);

        return entitypositiontypes_a == null ? HeightMap.Type.MOTION_BLOCKING_NO_LEAVES : entitypositiontypes_a.heightMap;
    }

    public static <T extends Entity> boolean checkSpawnRules(EntityTypes<T> entitytypes, WorldAccess worldaccess, EnumMobSpawn enummobspawn, BlockPosition blockposition, RandomSource randomsource) {
        EntityPositionTypes.a entitypositiontypes_a = (EntityPositionTypes.a) EntityPositionTypes.DATA_BY_TYPE.get(entitytypes);

        return entitypositiontypes_a == null || entitypositiontypes_a.predicate.test(entitytypes, worldaccess, enummobspawn, blockposition, randomsource);
    }

    static {
        register(EntityTypes.AXOLOTL, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, Axolotl::checkAxolotlSpawnRules);
        register(EntityTypes.COD, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        register(EntityTypes.DOLPHIN, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        register(EntityTypes.DROWNED, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityDrowned::checkDrownedSpawnRules);
        register(EntityTypes.GUARDIAN, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityGuardian::checkGuardianSpawnRules);
        register(EntityTypes.PUFFERFISH, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        register(EntityTypes.SALMON, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        register(EntityTypes.SQUID, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        register(EntityTypes.TROPICAL_FISH, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityTropicalFish::checkTropicalFishSpawnRules);
        register(EntityTypes.ARMADILLO, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, Armadillo::checkArmadilloSpawnRules);
        register(EntityTypes.BAT, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityBat::checkBatSpawnRules);
        register(EntityTypes.BLAZE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkAnyLightMonsterSpawnRules);
        register(EntityTypes.BOGGED, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.CAVE_SPIDER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.CHICKEN, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.COW, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.CREEPER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.DONKEY, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.ENDERMAN, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.ENDERMITE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityEndermite::checkEndermiteSpawnRules);
        register(EntityTypes.ENDER_DRAGON, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
        register(EntityTypes.FROG, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, Frog::checkFrogSpawnRules);
        register(EntityTypes.GHAST, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityGhast::checkGhastSpawnRules);
        register(EntityTypes.GIANT, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.GLOW_SQUID, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, GlowSquid::checkGlowSquidSpawnRules);
        register(EntityTypes.GOAT, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, Goat::checkGoatSpawnRules);
        register(EntityTypes.HORSE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.HUSK, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityZombieHusk::checkHuskSpawnRules);
        register(EntityTypes.IRON_GOLEM, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
        register(EntityTypes.LLAMA, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.MAGMA_CUBE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMagmaCube::checkMagmaCubeSpawnRules);
        register(EntityTypes.MOOSHROOM, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMushroomCow::checkMushroomSpawnRules);
        register(EntityTypes.MULE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.OCELOT, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING, EntityOcelot::checkOcelotSpawnRules);
        register(EntityTypes.PARROT, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING, EntityParrot::checkParrotSpawnRules);
        register(EntityTypes.PIG, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.HOGLIN, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityHoglin::checkHoglinSpawnRules);
        register(EntityTypes.PIGLIN, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityPiglin::checkPiglinSpawnRules);
        register(EntityTypes.PILLAGER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonsterPatrolling::checkPatrollingMonsterSpawnRules);
        register(EntityTypes.POLAR_BEAR, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityPolarBear::checkPolarBearSpawnRules);
        register(EntityTypes.RABBIT, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityRabbit::checkRabbitSpawnRules);
        register(EntityTypes.SHEEP, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.SILVERFISH, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySilverfish::checkSilverfishSpawnRules);
        register(EntityTypes.SKELETON, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.SKELETON_HORSE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityHorseSkeleton::checkSkeletonHorseSpawnRules);
        register(EntityTypes.SLIME, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySlime::checkSlimeSpawnRules);
        register(EntityTypes.SNOW_GOLEM, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
        register(EntityTypes.SPIDER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.STRAY, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySkeletonStray::checkStraySpawnRules);
        register(EntityTypes.STRIDER, SpawnPlacementTypes.IN_LAVA, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityStrider::checkStriderSpawnRules);
        register(EntityTypes.TURTLE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityTurtle::checkTurtleSpawnRules);
        register(EntityTypes.VILLAGER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
        register(EntityTypes.WITCH, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.WITHER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.WITHER_SKELETON, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.WOLF, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWolf::checkWolfSpawnRules);
        register(EntityTypes.ZOMBIE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.ZOMBIE_HORSE, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityHorseZombie::checkZombieHorseSpawnRules);
        register(EntityTypes.ZOMBIFIED_PIGLIN, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityPigZombie::checkZombifiedPiglinSpawnRules);
        register(EntityTypes.ZOMBIE_VILLAGER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.CAT, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.ELDER_GUARDIAN, SpawnPlacementTypes.IN_WATER, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityGuardian::checkGuardianSpawnRules);
        register(EntityTypes.EVOKER, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.FOX, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityFox::checkFoxSpawnRules);
        register(EntityTypes.ILLUSIONER, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.PANDA, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.PHANTOM, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
        register(EntityTypes.RAVAGER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.SHULKER, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
        register(EntityTypes.TRADER_LLAMA, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityAnimal::checkAnimalSpawnRules);
        register(EntityTypes.VEX, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.VINDICATOR, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMonster::checkMonsterSpawnRules);
        register(EntityTypes.WANDERING_TRADER, SpawnPlacementTypes.ON_GROUND, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
        register(EntityTypes.WARDEN, SpawnPlacementTypes.NO_RESTRICTIONS, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, EntityInsentient::checkMobSpawnRules);
    }

    private static record a(HeightMap.Type heightMap, SpawnPlacementType placement, EntityPositionTypes.b<?> predicate) {

    }

    @FunctionalInterface
    public interface b<T extends Entity> {

        boolean test(EntityTypes<T> entitytypes, WorldAccess worldaccess, EnumMobSpawn enummobspawn, BlockPosition blockposition, RandomSource randomsource);
    }
}
