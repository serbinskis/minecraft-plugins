package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public enum TrialSpawnerState implements INamable {

    INACTIVE("inactive", 0, TrialSpawnerState.b.NONE, -1.0D, false), WAITING_FOR_PLAYERS("waiting_for_players", 4, TrialSpawnerState.b.SMALL_FLAMES, 200.0D, true), ACTIVE("active", 8, TrialSpawnerState.b.FLAMES_AND_SMOKE, 1000.0D, true), WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, TrialSpawnerState.b.SMALL_FLAMES, -1.0D, false), EJECTING_REWARD("ejecting_reward", 8, TrialSpawnerState.b.SMALL_FLAMES, -1.0D, false), COOLDOWN("cooldown", 0, TrialSpawnerState.b.SMOKE_INSIDE_AND_TOP_FACE, -1.0D, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = MathHelper.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final TrialSpawnerState.b particleEmission;
    private final boolean isCapableOfSpawning;

    private TrialSpawnerState(final String s, final int i, final TrialSpawnerState.b trialspawnerstate_b, final double d0, final boolean flag) {
        this.name = s;
        this.lightLevel = i;
        this.particleEmission = trialspawnerstate_b;
        this.spinningMobSpeed = d0;
        this.isCapableOfSpawning = flag;
    }

    TrialSpawnerState tickAndGetNext(BlockPosition blockposition, TrialSpawner trialspawner, WorldServer worldserver) {
        TrialSpawnerData trialspawnerdata = trialspawner.getData();
        TrialSpawnerConfig trialspawnerconfig = trialspawner.getConfig();
        TrialSpawnerState trialspawnerstate;

        switch (this.ordinal()) {
            case 0:
                trialspawnerstate = trialspawnerdata.getOrCreateDisplayEntity(trialspawner, worldserver, TrialSpawnerState.WAITING_FOR_PLAYERS) == null ? this : TrialSpawnerState.WAITING_FOR_PLAYERS;
                break;
            case 1:
                if (!trialspawner.canSpawnInLevel(worldserver)) {
                    trialspawnerdata.reset();
                    trialspawnerstate = this;
                } else if (!trialspawnerdata.hasMobToSpawn(trialspawner, worldserver.random)) {
                    trialspawnerstate = TrialSpawnerState.INACTIVE;
                } else {
                    trialspawnerdata.tryDetectPlayers(worldserver, blockposition, trialspawner);
                    trialspawnerstate = trialspawnerdata.detectedPlayers.isEmpty() ? this : TrialSpawnerState.ACTIVE;
                }
                break;
            case 2:
                if (!trialspawner.canSpawnInLevel(worldserver)) {
                    trialspawnerdata.reset();
                    trialspawnerstate = TrialSpawnerState.WAITING_FOR_PLAYERS;
                } else if (!trialspawnerdata.hasMobToSpawn(trialspawner, worldserver.random)) {
                    trialspawnerstate = TrialSpawnerState.INACTIVE;
                } else {
                    int i = trialspawnerdata.countAdditionalPlayers(blockposition);

                    trialspawnerdata.tryDetectPlayers(worldserver, blockposition, trialspawner);
                    if (trialspawner.isOminous()) {
                        this.spawnOminousOminousItemSpawner(worldserver, blockposition, trialspawner);
                    }

                    if (trialspawnerdata.hasFinishedSpawningAllMobs(trialspawnerconfig, i)) {
                        if (trialspawnerdata.haveAllCurrentMobsDied()) {
                            trialspawnerdata.cooldownEndsAt = worldserver.getGameTime() + (long) trialspawner.getTargetCooldownLength();
                            trialspawnerdata.totalMobsSpawned = 0;
                            trialspawnerdata.nextMobSpawnsAt = 0L;
                            trialspawnerstate = TrialSpawnerState.WAITING_FOR_REWARD_EJECTION;
                            break;
                        }
                    } else if (trialspawnerdata.isReadyToSpawnNextMob(worldserver, trialspawnerconfig, i)) {
                        trialspawner.spawnMob(worldserver, blockposition).ifPresent((uuid) -> {
                            trialspawnerdata.currentMobs.add(uuid);
                            ++trialspawnerdata.totalMobsSpawned;
                            trialspawnerdata.nextMobSpawnsAt = worldserver.getGameTime() + (long) trialspawnerconfig.ticksBetweenSpawn();
                            trialspawnerconfig.spawnPotentialsDefinition().getRandom(worldserver.getRandom()).ifPresent((weightedentry_b) -> {
                                trialspawnerdata.nextSpawnData = Optional.of((MobSpawnerData) weightedentry_b.data());
                                trialspawner.markUpdated();
                            });
                        });
                    }

                    trialspawnerstate = this;
                }
                break;
            case 3:
                if (trialspawnerdata.isReadyToOpenShutter(worldserver, 40.0F, trialspawner.getTargetCooldownLength())) {
                    worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.TRIAL_SPAWNER_OPEN_SHUTTER, SoundCategory.BLOCKS);
                    trialspawnerstate = TrialSpawnerState.EJECTING_REWARD;
                } else {
                    trialspawnerstate = this;
                }
                break;
            case 4:
                if (!trialspawnerdata.isReadyToEjectItems(worldserver, (float) TrialSpawnerState.TIME_BETWEEN_EACH_EJECTION, trialspawner.getTargetCooldownLength())) {
                    trialspawnerstate = this;
                } else if (trialspawnerdata.detectedPlayers.isEmpty()) {
                    worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundCategory.BLOCKS);
                    trialspawnerdata.ejectingLootTable = Optional.empty();
                    trialspawnerstate = TrialSpawnerState.COOLDOWN;
                } else {
                    if (trialspawnerdata.ejectingLootTable.isEmpty()) {
                        trialspawnerdata.ejectingLootTable = trialspawnerconfig.lootTablesToEject().getRandomValue(worldserver.getRandom());
                    }

                    trialspawnerdata.ejectingLootTable.ifPresent((resourcekey) -> {
                        trialspawner.ejectReward(worldserver, blockposition, resourcekey);
                    });
                    trialspawnerdata.detectedPlayers.remove(trialspawnerdata.detectedPlayers.iterator().next());
                    trialspawnerstate = this;
                }
                break;
            case 5:
                trialspawnerdata.tryDetectPlayers(worldserver, blockposition, trialspawner);
                if (!trialspawnerdata.detectedPlayers.isEmpty()) {
                    trialspawnerdata.totalMobsSpawned = 0;
                    trialspawnerdata.nextMobSpawnsAt = 0L;
                    trialspawnerstate = TrialSpawnerState.ACTIVE;
                } else if (trialspawnerdata.isCooldownFinished(worldserver)) {
                    trialspawner.removeOminous(worldserver, blockposition);
                    trialspawnerdata.reset();
                    trialspawnerstate = TrialSpawnerState.WAITING_FOR_PLAYERS;
                } else {
                    trialspawnerstate = this;
                }
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return trialspawnerstate;
    }

    private void spawnOminousOminousItemSpawner(WorldServer worldserver, BlockPosition blockposition, TrialSpawner trialspawner) {
        TrialSpawnerData trialspawnerdata = trialspawner.getData();
        TrialSpawnerConfig trialspawnerconfig = trialspawner.getConfig();
        ItemStack itemstack = (ItemStack) trialspawnerdata.getDispensingItems(worldserver, trialspawnerconfig, blockposition).getRandomValue(worldserver.random).orElse(ItemStack.EMPTY);

        if (!itemstack.isEmpty()) {
            if (this.timeToSpawnItemSpawner(worldserver, trialspawnerdata)) {
                calculatePositionToSpawnSpawner(worldserver, blockposition, trialspawner, trialspawnerdata).ifPresent((vec3d) -> {
                    OminousItemSpawner ominousitemspawner = OminousItemSpawner.create(worldserver, itemstack);

                    ominousitemspawner.moveTo(vec3d);
                    worldserver.addFreshEntity(ominousitemspawner);
                    float f = (worldserver.getRandom().nextFloat() - worldserver.getRandom().nextFloat()) * 0.2F + 1.0F;

                    worldserver.playSound((EntityHuman) null, BlockPosition.containing(vec3d), SoundEffects.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundCategory.BLOCKS, 1.0F, f);
                    trialspawnerdata.cooldownEndsAt = worldserver.getGameTime() + trialspawner.getOminousConfig().ticksBetweenItemSpawners();
                });
            }

        }
    }

    private static Optional<Vec3D> calculatePositionToSpawnSpawner(WorldServer worldserver, BlockPosition blockposition, TrialSpawner trialspawner, TrialSpawnerData trialspawnerdata) {
        Stream stream = trialspawnerdata.detectedPlayers.stream();

        Objects.requireNonNull(worldserver);
        List<EntityHuman> list = stream.map(worldserver::getPlayerByUUID).filter(Objects::nonNull).filter((entityhuman) -> {
            return !entityhuman.isCreative() && !entityhuman.isSpectator() && entityhuman.isAlive() && entityhuman.distanceToSqr(blockposition.getCenter()) <= (double) MathHelper.square(trialspawner.getRequiredPlayerRange());
        }).toList();

        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            Entity entity = selectEntityToSpawnItemAbove(list, trialspawnerdata.currentMobs, trialspawner, blockposition, worldserver);

            return entity == null ? Optional.empty() : calculatePositionAbove(entity, worldserver);
        }
    }

    private static Optional<Vec3D> calculatePositionAbove(Entity entity, WorldServer worldserver) {
        Vec3D vec3d = entity.position();
        Vec3D vec3d1 = vec3d.relative(EnumDirection.UP, (double) (entity.getBbHeight() + 2.0F + (float) worldserver.random.nextInt(4)));
        MovingObjectPositionBlock movingobjectpositionblock = worldserver.clip(new RayTrace(vec3d, vec3d1, RayTrace.BlockCollisionOption.VISUAL, RayTrace.FluidCollisionOption.NONE, VoxelShapeCollision.empty()));
        Vec3D vec3d2 = movingobjectpositionblock.getBlockPos().getCenter().relative(EnumDirection.DOWN, 1.0D);
        BlockPosition blockposition = BlockPosition.containing(vec3d2);

        return !worldserver.getBlockState(blockposition).getCollisionShape(worldserver, blockposition).isEmpty() ? Optional.empty() : Optional.of(vec3d2);
    }

    @Nullable
    private static Entity selectEntityToSpawnItemAbove(List<EntityHuman> list, Set<UUID> set, TrialSpawner trialspawner, BlockPosition blockposition, WorldServer worldserver) {
        Stream stream = set.stream();

        Objects.requireNonNull(worldserver);
        Stream<Entity> stream1 = stream.map(worldserver::getEntity).filter(Objects::nonNull).filter((entity) -> {
            return entity.isAlive() && entity.distanceToSqr(blockposition.getCenter()) <= (double) MathHelper.square(trialspawner.getRequiredPlayerRange());
        });
        List<? extends Entity> list1 = worldserver.random.nextBoolean() ? stream1.toList() : list;

        return list1.isEmpty() ? null : (list1.size() == 1 ? (Entity) list1.getFirst() : (Entity) SystemUtils.getRandom(list1, worldserver.random));
    }

    private boolean timeToSpawnItemSpawner(WorldServer worldserver, TrialSpawnerData trialspawnerdata) {
        return worldserver.getGameTime() >= trialspawnerdata.cooldownEndsAt;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0D;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    public void emitParticles(World world, BlockPosition blockposition, boolean flag) {
        this.particleEmission.emit(world, world.getRandom(), blockposition, flag);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private interface b {

        TrialSpawnerState.b NONE = (world, randomsource, blockposition, flag) -> {
        };
        TrialSpawnerState.b SMALL_FLAMES = (world, randomsource, blockposition, flag) -> {
            if (randomsource.nextInt(2) == 0) {
                Vec3D vec3d = blockposition.getCenter().offsetRandom(randomsource, 0.9F);

                addParticle(flag ? Particles.SOUL_FIRE_FLAME : Particles.SMALL_FLAME, vec3d, world);
            }

        };
        TrialSpawnerState.b FLAMES_AND_SMOKE = (world, randomsource, blockposition, flag) -> {
            Vec3D vec3d = blockposition.getCenter().offsetRandom(randomsource, 1.0F);

            addParticle(Particles.SMOKE, vec3d, world);
            addParticle(flag ? Particles.SOUL_FIRE_FLAME : Particles.FLAME, vec3d, world);
        };
        TrialSpawnerState.b SMOKE_INSIDE_AND_TOP_FACE = (world, randomsource, blockposition, flag) -> {
            Vec3D vec3d = blockposition.getCenter().offsetRandom(randomsource, 0.9F);

            if (randomsource.nextInt(3) == 0) {
                addParticle(Particles.SMOKE, vec3d, world);
            }

            if (world.getGameTime() % 20L == 0L) {
                Vec3D vec3d1 = blockposition.getCenter().add(0.0D, 0.5D, 0.0D);
                int i = world.getRandom().nextInt(4) + 20;

                for (int j = 0; j < i; ++j) {
                    addParticle(Particles.SMOKE, vec3d1, world);
                }
            }

        };

        private static void addParticle(ParticleType particletype, Vec3D vec3d, World world) {
            world.addParticle(particletype, vec3d.x(), vec3d.y(), vec3d.z(), 0.0D, 0.0D, 0.0D);
        }

        void emit(World world, RandomSource randomsource, BlockPosition blockposition, boolean flag);
    }

    private static class a {

        private static final int UNLIT = 0;
        private static final int HALF_LIT = 4;
        private static final int LIT = 8;

        private a() {}
    }

    private static class c {

        private static final double NONE = -1.0D;
        private static final double SLOW = 200.0D;
        private static final double FAST = 1000.0D;

        private c() {}
    }
}
