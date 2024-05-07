package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.DispenseBehaviorItem;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityPositionTypes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public final class TrialSpawner {

    public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
    public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = MathHelper.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
    private final TrialSpawnerConfig normalConfig;
    private final TrialSpawnerConfig ominousConfig;
    private final TrialSpawnerData data;
    private final int requiredPlayerRange;
    private final int targetCooldownLength;
    public final TrialSpawner.b stateAccessor;
    private PlayerDetector playerDetector;
    private final PlayerDetector.a entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;

    public Codec<TrialSpawner> codec() {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(TrialSpawnerConfig.CODEC.optionalFieldOf("normal_config", TrialSpawnerConfig.DEFAULT).forGetter(TrialSpawner::getNormalConfig), TrialSpawnerConfig.CODEC.optionalFieldOf("ominous_config", TrialSpawnerConfig.DEFAULT).forGetter(TrialSpawner::getOminousConfigForSerialization), TrialSpawnerData.MAP_CODEC.forGetter(TrialSpawner::getData), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", 36000).forGetter(TrialSpawner::getTargetCooldownLength), Codec.intRange(1, 128).optionalFieldOf("required_player_range", 14).forGetter(TrialSpawner::getRequiredPlayerRange)).apply(instance, (trialspawnerconfig, trialspawnerconfig1, trialspawnerdata, integer, integer1) -> {
                return new TrialSpawner(trialspawnerconfig, trialspawnerconfig1, trialspawnerdata, integer, integer1, this.stateAccessor, this.playerDetector, this.entitySelector);
            });
        });
    }

    public TrialSpawner(TrialSpawner.b trialspawner_b, PlayerDetector playerdetector, PlayerDetector.a playerdetector_a) {
        this(TrialSpawnerConfig.DEFAULT, TrialSpawnerConfig.DEFAULT, new TrialSpawnerData(), 36000, 14, trialspawner_b, playerdetector, playerdetector_a);
    }

    public TrialSpawner(TrialSpawnerConfig trialspawnerconfig, TrialSpawnerConfig trialspawnerconfig1, TrialSpawnerData trialspawnerdata, int i, int j, TrialSpawner.b trialspawner_b, PlayerDetector playerdetector, PlayerDetector.a playerdetector_a) {
        this.normalConfig = trialspawnerconfig;
        this.ominousConfig = trialspawnerconfig1;
        this.data = trialspawnerdata;
        this.targetCooldownLength = i;
        this.requiredPlayerRange = j;
        this.stateAccessor = trialspawner_b;
        this.playerDetector = playerdetector;
        this.entitySelector = playerdetector_a;
    }

    public TrialSpawnerConfig getConfig() {
        return this.isOminous ? this.ominousConfig : this.normalConfig;
    }

    @VisibleForTesting
    public TrialSpawnerConfig getNormalConfig() {
        return this.normalConfig;
    }

    @VisibleForTesting
    public TrialSpawnerConfig getOminousConfig() {
        return this.ominousConfig;
    }

    private TrialSpawnerConfig getOminousConfigForSerialization() {
        return !this.ominousConfig.equals(this.normalConfig) ? this.ominousConfig : TrialSpawnerConfig.DEFAULT;
    }

    public void applyOminous(WorldServer worldserver, BlockPosition blockposition) {
        worldserver.setBlock(blockposition, (IBlockData) worldserver.getBlockState(blockposition).setValue(TrialSpawnerBlock.OMINOUS, true), 3);
        worldserver.levelEvent(3020, blockposition, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, worldserver);
    }

    public void removeOminous(WorldServer worldserver, BlockPosition blockposition) {
        worldserver.setBlock(blockposition, (IBlockData) worldserver.getBlockState(blockposition).setValue(TrialSpawnerBlock.OMINOUS, false), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public TrialSpawnerData getData() {
        return this.data;
    }

    public int getTargetCooldownLength() {
        return this.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    public TrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(World world, TrialSpawnerState trialspawnerstate) {
        this.stateAccessor.setState(world, trialspawnerstate);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.a getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(World world) {
        return this.overridePeacefulAndMobSpawnRule ? true : (world.getDifficulty() == EnumDifficulty.PEACEFUL ? false : world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING));
    }

    public Optional<UUID> spawnMob(WorldServer worldserver, BlockPosition blockposition) {
        RandomSource randomsource = worldserver.getRandom();
        MobSpawnerData mobspawnerdata = this.data.getOrCreateNextSpawnData(this, worldserver.getRandom());
        NBTTagCompound nbttagcompound = mobspawnerdata.entityToSpawn();
        NBTTagList nbttaglist = nbttagcompound.getList("Pos", 6);
        Optional<EntityTypes<?>> optional = EntityTypes.by(nbttagcompound);

        if (optional.isEmpty()) {
            return Optional.empty();
        } else {
            int i = nbttaglist.size();
            double d0 = i >= 1 ? nbttaglist.getDouble(0) : (double) blockposition.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double) this.getConfig().spawnRange() + 0.5D;
            double d1 = i >= 2 ? nbttaglist.getDouble(1) : (double) (blockposition.getY() + randomsource.nextInt(3) - 1);
            double d2 = i >= 3 ? nbttaglist.getDouble(2) : (double) blockposition.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double) this.getConfig().spawnRange() + 0.5D;

            if (!worldserver.noCollision(((EntityTypes) optional.get()).getSpawnAABB(d0, d1, d2))) {
                return Optional.empty();
            } else {
                Vec3D vec3d = new Vec3D(d0, d1, d2);

                if (!inLineOfSight(worldserver, blockposition.getCenter(), vec3d)) {
                    return Optional.empty();
                } else {
                    BlockPosition blockposition1 = BlockPosition.containing(vec3d);

                    if (!EntityPositionTypes.checkSpawnRules((EntityTypes) optional.get(), worldserver, EnumMobSpawn.TRIAL_SPAWNER, blockposition1, worldserver.getRandom())) {
                        return Optional.empty();
                    } else {
                        if (mobspawnerdata.getCustomSpawnRules().isPresent()) {
                            MobSpawnerData.a mobspawnerdata_a = (MobSpawnerData.a) mobspawnerdata.getCustomSpawnRules().get();

                            if (!mobspawnerdata_a.isValidPosition(blockposition1, worldserver)) {
                                return Optional.empty();
                            }
                        }

                        Entity entity = EntityTypes.loadEntityRecursive(nbttagcompound, worldserver, (entity1) -> {
                            entity1.moveTo(d0, d1, d2, randomsource.nextFloat() * 360.0F, 0.0F);
                            return entity1;
                        });

                        if (entity == null) {
                            return Optional.empty();
                        } else {
                            if (entity instanceof EntityInsentient) {
                                EntityInsentient entityinsentient = (EntityInsentient) entity;

                                if (!entityinsentient.checkSpawnObstruction(worldserver)) {
                                    return Optional.empty();
                                }

                                boolean flag = mobspawnerdata.getEntityToSpawn().size() == 1 && mobspawnerdata.getEntityToSpawn().contains("id", 8);

                                if (flag) {
                                    entityinsentient.finalizeSpawn(worldserver, worldserver.getCurrentDifficultyAt(entityinsentient.blockPosition()), EnumMobSpawn.TRIAL_SPAWNER, (GroupDataEntity) null);
                                }

                                entityinsentient.setPersistenceRequired();
                                Optional optional1 = mobspawnerdata.getEquipment();

                                Objects.requireNonNull(entityinsentient);
                                optional1.ifPresent(entityinsentient::equip);
                            }

                            if (!worldserver.tryAddFreshEntityWithPassengers(entity)) {
                                return Optional.empty();
                            } else {
                                TrialSpawner.a trialspawner_a = this.isOminous ? TrialSpawner.a.OMINOUS : TrialSpawner.a.NORMAL;

                                worldserver.levelEvent(3011, blockposition, trialspawner_a.encode());
                                worldserver.levelEvent(3012, blockposition1, trialspawner_a.encode());
                                worldserver.gameEvent(entity, (Holder) GameEvent.ENTITY_PLACE, blockposition1);
                                return Optional.of(entity.getUUID());
                            }
                        }
                    }
                }
            }
        }
    }

    public void ejectReward(WorldServer worldserver, BlockPosition blockposition, ResourceKey<LootTable> resourcekey) {
        LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(resourcekey);
        LootParams lootparams = (new LootParams.a(worldserver)).create(LootContextParameterSets.EMPTY);
        ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams);

        if (!objectarraylist.isEmpty()) {
            ObjectListIterator objectlistiterator = objectarraylist.iterator();

            while (objectlistiterator.hasNext()) {
                ItemStack itemstack = (ItemStack) objectlistiterator.next();

                DispenseBehaviorItem.spawnItem(worldserver, itemstack, 2, EnumDirection.UP, Vec3D.atBottomCenterOf(blockposition).relative(EnumDirection.UP, 1.2D));
            }

            worldserver.levelEvent(3014, blockposition, 0);
        }

    }

    public void tickClient(World world, BlockPosition blockposition, boolean flag) {
        if (!this.canSpawnInLevel(world)) {
            this.data.oSpin = this.data.spin;
        } else {
            TrialSpawnerState trialspawnerstate = this.getState();

            trialspawnerstate.emitParticles(world, blockposition, flag);
            if (trialspawnerstate.hasSpinningMob()) {
                double d0 = (double) Math.max(0L, this.data.nextMobSpawnsAt - world.getGameTime());

                this.data.oSpin = this.data.spin;
                this.data.spin = (this.data.spin + trialspawnerstate.spinningMobSpeed() / (d0 + 200.0D)) % 360.0D;
            }

            if (trialspawnerstate.isCapableOfSpawning()) {
                RandomSource randomsource = world.getRandom();

                if (randomsource.nextFloat() <= 0.02F) {
                    SoundEffect soundeffect = flag ? SoundEffects.TRIAL_SPAWNER_AMBIENT_OMINOUS : SoundEffects.TRIAL_SPAWNER_AMBIENT;

                    world.playLocalSound(blockposition, soundeffect, SoundCategory.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false);
                }
            }

        }
    }

    public void tickServer(WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        this.isOminous = flag;
        TrialSpawnerState trialspawnerstate = this.getState();

        if (!this.canSpawnInLevel(worldserver)) {
            if (trialspawnerstate.isCapableOfSpawning()) {
                this.data.reset();
                this.setState(worldserver, TrialSpawnerState.INACTIVE);
            }

        } else {
            if (this.data.currentMobs.removeIf((uuid) -> {
                return shouldMobBeUntracked(worldserver, blockposition, uuid);
            })) {
                this.data.nextMobSpawnsAt = worldserver.getGameTime() + (long) this.getConfig().ticksBetweenSpawn();
            }

            TrialSpawnerState trialspawnerstate1 = trialspawnerstate.tickAndGetNext(blockposition, this, worldserver);

            if (trialspawnerstate1 != trialspawnerstate) {
                this.setState(worldserver, trialspawnerstate1);
            }

        }
    }

    private static boolean shouldMobBeUntracked(WorldServer worldserver, BlockPosition blockposition, UUID uuid) {
        Entity entity = worldserver.getEntity(uuid);

        return entity == null || !entity.isAlive() || !entity.level().dimension().equals(worldserver.dimension()) || entity.blockPosition().distSqr(blockposition) > (double) TrialSpawner.MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(World world, Vec3D vec3d, Vec3D vec3d1) {
        MovingObjectPositionBlock movingobjectpositionblock = world.clip(new RayTrace(vec3d1, vec3d, RayTrace.BlockCollisionOption.VISUAL, RayTrace.FluidCollisionOption.NONE, VoxelShapeCollision.empty()));

        return movingobjectpositionblock.getBlockPos().equals(BlockPosition.containing(vec3d)) || movingobjectpositionblock.getType() == MovingObjectPosition.EnumMovingObjectType.MISS;
    }

    public static void addSpawnParticles(World world, BlockPosition blockposition, RandomSource randomsource, ParticleType particletype) {
        for (int i = 0; i < 20; ++i) {
            double d0 = (double) blockposition.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
            double d1 = (double) blockposition.getY() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
            double d2 = (double) blockposition.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;

            world.addParticle(Particles.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            world.addParticle(particletype, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }

    }

    public static void addBecomeOminousParticles(World world, BlockPosition blockposition, RandomSource randomsource) {
        for (int i = 0; i < 20; ++i) {
            double d0 = (double) blockposition.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
            double d1 = (double) blockposition.getY() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
            double d2 = (double) blockposition.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
            double d3 = randomsource.nextGaussian() * 0.02D;
            double d4 = randomsource.nextGaussian() * 0.02D;
            double d5 = randomsource.nextGaussian() * 0.02D;

            world.addParticle(Particles.TRIAL_OMEN, d0, d1, d2, d3, d4, d5);
            world.addParticle(Particles.SOUL_FIRE_FLAME, d0, d1, d2, d3, d4, d5);
        }

    }

    public static void addDetectPlayerParticles(World world, BlockPosition blockposition, RandomSource randomsource, int i, ParticleParam particleparam) {
        for (int j = 0; j < 30 + Math.min(i, 10) * 5; ++j) {
            double d0 = (double) (2.0F * randomsource.nextFloat() - 1.0F) * 0.65D;
            double d1 = (double) (2.0F * randomsource.nextFloat() - 1.0F) * 0.65D;
            double d2 = (double) blockposition.getX() + 0.5D + d0;
            double d3 = (double) blockposition.getY() + 0.1D + (double) randomsource.nextFloat() * 0.8D;
            double d4 = (double) blockposition.getZ() + 0.5D + d1;

            world.addParticle(particleparam, d2, d3, d4, 0.0D, 0.0D, 0.0D);
        }

    }

    public static void addEjectItemParticles(World world, BlockPosition blockposition, RandomSource randomsource) {
        for (int i = 0; i < 20; ++i) {
            double d0 = (double) blockposition.getX() + 0.4D + randomsource.nextDouble() * 0.2D;
            double d1 = (double) blockposition.getY() + 0.4D + randomsource.nextDouble() * 0.2D;
            double d2 = (double) blockposition.getZ() + 0.4D + randomsource.nextDouble() * 0.2D;
            double d3 = randomsource.nextGaussian() * 0.02D;
            double d4 = randomsource.nextGaussian() * 0.02D;
            double d5 = randomsource.nextGaussian() * 0.02D;

            world.addParticle(Particles.SMALL_FLAME, d0, d1, d2, d3, d4, d5 * 0.25D);
            world.addParticle(Particles.SMOKE, d0, d1, d2, d3, d4, d5);
        }

    }

    /** @deprecated */
    @Deprecated(forRemoval = true)
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector playerdetector) {
        this.playerDetector = playerdetector;
    }

    /** @deprecated */
    @Deprecated(forRemoval = true)
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public interface b {

        void setState(World world, TrialSpawnerState trialspawnerstate);

        TrialSpawnerState getState();

        void markUpdated();
    }

    public static enum a {

        NORMAL(Particles.FLAME), OMINOUS(Particles.SOUL_FIRE_FLAME);

        public final ParticleType particleType;

        private a(final ParticleType particletype) {
            this.particleType = particletype;
        }

        public static TrialSpawner.a decode(int i) {
            TrialSpawner.a[] atrialspawner_a = values();

            return i <= atrialspawner_a.length && i >= 0 ? atrialspawner_a[i] : TrialSpawner.a.NORMAL;
        }

        public int encode() {
            return this.ordinal();
        }
    }
}
