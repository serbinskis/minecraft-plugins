package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;

public class TrialSpawnerData {

    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", Sets.newHashSet()).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.detectedPlayers;
        }), UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", Sets.newHashSet()).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.currentMobs;
        }), Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", 0L).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.cooldownEndsAt;
        }), Codec.LONG.lenientOptionalFieldOf("next_mob_spawns_at", 0L).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.nextMobSpawnsAt;
        }), Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.totalMobsSpawned;
        }), MobSpawnerData.CODEC.lenientOptionalFieldOf("spawn_data").forGetter((trialspawnerdata) -> {
            return trialspawnerdata.nextSpawnData;
        }), ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter((trialspawnerdata) -> {
            return trialspawnerdata.ejectingLootTable;
        })).apply(instance, TrialSpawnerData::new);
    });
    public final Set<UUID> detectedPlayers;
    public final Set<UUID> currentMobs;
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    public Optional<MobSpawnerData> nextSpawnData;
    protected Optional<ResourceKey<LootTable>> ejectingLootTable;
    @Nullable
    protected Entity displayEntity;
    @Nullable
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(Set<UUID> set, Set<UUID> set1, long i, long j, int k, Optional<MobSpawnerData> optional, Optional<ResourceKey<LootTable>> optional1) {
        this.detectedPlayers = new HashSet();
        this.currentMobs = new HashSet();
        this.detectedPlayers.addAll(set);
        this.currentMobs.addAll(set1);
        this.cooldownEndsAt = i;
        this.nextMobSpawnsAt = j;
        this.totalMobsSpawned = k;
        this.nextSpawnData = optional;
        this.ejectingLootTable = optional1;
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
    }

    public boolean hasMobToSpawn(TrialSpawner trialspawner, RandomSource randomsource) {
        boolean flag = this.getOrCreateNextSpawnData(trialspawner, randomsource).getEntityToSpawn().contains("id", 8);

        return flag || !trialspawner.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig trialspawnerconfig, int i) {
        return this.totalMobsSpawned >= trialspawnerconfig.calculateTargetTotalMobs(i);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(WorldServer worldserver, TrialSpawnerConfig trialspawnerconfig, int i) {
        return worldserver.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < trialspawnerconfig.calculateTargetSimultaneousMobs(i);
    }

    public int countAdditionalPlayers(BlockPosition blockposition) {
        if (this.detectedPlayers.isEmpty()) {
            SystemUtils.logAndPauseIfInIde("Trial Spawner at " + String.valueOf(blockposition) + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(WorldServer worldserver, BlockPosition blockposition, TrialSpawner trialspawner) {
        boolean flag = (blockposition.asLong() + worldserver.getGameTime()) % 20L != 0L;

        if (!flag) {
            if (!trialspawner.getState().equals(TrialSpawnerState.COOLDOWN) || !trialspawner.isOminous()) {
                List<UUID> list = trialspawner.getPlayerDetector().detect(worldserver, trialspawner.getEntitySelector(), blockposition, (double) trialspawner.getRequiredPlayerRange(), true);
                EntityHuman entityhuman = null;
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    UUID uuid = (UUID) iterator.next();
                    EntityHuman entityhuman1 = worldserver.getPlayerByUUID(uuid);

                    if (entityhuman1 != null) {
                        if (entityhuman1.hasEffect(MobEffects.BAD_OMEN)) {
                            this.transformBadOmenIntoTrialOmen(entityhuman1, entityhuman1.getEffect(MobEffects.BAD_OMEN));
                            entityhuman = entityhuman1;
                        } else if (entityhuman1.hasEffect(MobEffects.TRIAL_OMEN)) {
                            entityhuman = entityhuman1;
                        }
                    }
                }

                boolean flag1 = !trialspawner.isOminous() && entityhuman != null;

                if (!trialspawner.getState().equals(TrialSpawnerState.COOLDOWN) || flag1) {
                    if (flag1) {
                        worldserver.levelEvent(3020, BlockPosition.containing(entityhuman.getEyePosition()), 0);
                        trialspawner.applyOminous(worldserver, blockposition);
                    }

                    boolean flag2 = trialspawner.getData().detectedPlayers.isEmpty();
                    List<UUID> list1 = flag2 ? list : trialspawner.getPlayerDetector().detect(worldserver, trialspawner.getEntitySelector(), blockposition, (double) trialspawner.getRequiredPlayerRange(), false);

                    if (this.detectedPlayers.addAll(list1)) {
                        this.nextMobSpawnsAt = Math.max(worldserver.getGameTime() + 40L, this.nextMobSpawnsAt);
                        if (!flag1) {
                            int i = trialspawner.isOminous() ? 3019 : 3013;

                            worldserver.levelEvent(i, blockposition, this.detectedPlayers.size());
                        }
                    }

                }
            }
        }
    }

    public void resetAfterBecomingOminous(TrialSpawner trialspawner, WorldServer worldserver) {
        Stream stream = this.currentMobs.stream();

        Objects.requireNonNull(worldserver);
        stream.map(worldserver::getEntity).forEach((entity) -> {
            if (entity != null) {
                worldserver.levelEvent(3012, entity.blockPosition(), TrialSpawner.a.NORMAL.encode());
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        });
        if (!trialspawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }

        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = worldserver.getGameTime() + (long) trialspawner.getOminousConfig().ticksBetweenSpawn();
        trialspawner.markUpdated();
        this.cooldownEndsAt = worldserver.getGameTime() + trialspawner.getOminousConfig().ticksBetweenItemSpawners();
    }

    private void transformBadOmenIntoTrialOmen(EntityHuman entityhuman, MobEffect mobeffect) {
        int i = mobeffect.getAmplifier() + 1;
        int j = 18000 * i;

        entityhuman.removeEffect(MobEffects.BAD_OMEN);
        entityhuman.addEffect(new MobEffect(MobEffects.TRIAL_OMEN, j, 0));
    }

    public boolean isReadyToOpenShutter(WorldServer worldserver, float f, int i) {
        long j = this.cooldownEndsAt - (long) i;

        return (float) worldserver.getGameTime() >= (float) j + f;
    }

    public boolean isReadyToEjectItems(WorldServer worldserver, float f, int i) {
        long j = this.cooldownEndsAt - (long) i;

        return (float) (worldserver.getGameTime() - j) % f == 0.0F;
    }

    public boolean isCooldownFinished(WorldServer worldserver) {
        return worldserver.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(TrialSpawner trialspawner, RandomSource randomsource, EntityTypes<?> entitytypes) {
        this.getOrCreateNextSpawnData(trialspawner, randomsource).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entitytypes).toString());
    }

    protected MobSpawnerData getOrCreateNextSpawnData(TrialSpawner trialspawner, RandomSource randomsource) {
        if (this.nextSpawnData.isPresent()) {
            return (MobSpawnerData) this.nextSpawnData.get();
        } else {
            SimpleWeightedRandomList<MobSpawnerData> simpleweightedrandomlist = trialspawner.getConfig().spawnPotentialsDefinition();
            Optional<MobSpawnerData> optional = simpleweightedrandomlist.isEmpty() ? this.nextSpawnData : simpleweightedrandomlist.getRandom(randomsource).map(WeightedEntry.b::data);

            this.nextSpawnData = Optional.of((MobSpawnerData) optional.orElseGet(MobSpawnerData::new));
            trialspawner.markUpdated();
            return (MobSpawnerData) this.nextSpawnData.get();
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner trialspawner, World world, TrialSpawnerState trialspawnerstate) {
        if (trialspawner.canSpawnInLevel(world) && trialspawnerstate.hasSpinningMob()) {
            if (this.displayEntity == null) {
                NBTTagCompound nbttagcompound = this.getOrCreateNextSpawnData(trialspawner, world.getRandom()).getEntityToSpawn();

                if (nbttagcompound.contains("id", 8)) {
                    this.displayEntity = EntityTypes.loadEntityRecursive(nbttagcompound, world, Function.identity());
                }
            }

            return this.displayEntity;
        } else {
            return null;
        }
    }

    public NBTTagCompound getUpdateTag(TrialSpawnerState trialspawnerstate) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        if (trialspawnerstate == TrialSpawnerState.ACTIVE) {
            nbttagcompound.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData.ifPresent((mobspawnerdata) -> {
            nbttagcompound.put("spawn_data", (NBTBase) MobSpawnerData.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, mobspawnerdata).result().orElseThrow(() -> {
                return new IllegalStateException("Invalid SpawnData");
            }));
        });
        return nbttagcompound;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(WorldServer worldserver, TrialSpawnerConfig trialspawnerconfig, BlockPosition blockposition) {
        if (this.dispensing != null) {
            return this.dispensing;
        } else {
            LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(trialspawnerconfig.itemsToDropWhenOminous());
            LootParams lootparams = (new LootParams.a(worldserver)).create(LootContextParameterSets.EMPTY);
            long i = lowResolutionPosition(worldserver, blockposition);
            ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams, i);

            if (objectarraylist.isEmpty()) {
                return SimpleWeightedRandomList.empty();
            } else {
                SimpleWeightedRandomList.a<ItemStack> simpleweightedrandomlist_a = new SimpleWeightedRandomList.a<>();
                ObjectListIterator objectlistiterator = objectarraylist.iterator();

                while (objectlistiterator.hasNext()) {
                    ItemStack itemstack = (ItemStack) objectlistiterator.next();

                    simpleweightedrandomlist_a.add(itemstack.copyWithCount(1), itemstack.getCount());
                }

                this.dispensing = simpleweightedrandomlist_a.build();
                return this.dispensing;
            }
        }
    }

    private static long lowResolutionPosition(WorldServer worldserver, BlockPosition blockposition) {
        BlockPosition blockposition1 = new BlockPosition(MathHelper.floor((float) blockposition.getX() / 30.0F), MathHelper.floor((float) blockposition.getY() / 20.0F), MathHelper.floor((float) blockposition.getZ() / 30.0F));

        return worldserver.getSeed() + blockposition1.asLong();
    }
}
