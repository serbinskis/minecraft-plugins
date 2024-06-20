package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.OptionalDynamic;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.WorldSettings;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.levelgen.GeneratorSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.timers.CustomFunctionCallbackTimerQueue;
import net.minecraft.world.level.timers.CustomFunctionCallbackTimers;
import org.slf4j.Logger;

public class WorldDataServer implements IWorldDataServer, SaveData {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String LEVEL_NAME = "LevelName";
    protected static final String PLAYER = "Player";
    protected static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
    public WorldSettings settings;
    private final WorldOptions worldOptions;
    private final WorldDataServer.a specialWorldProperty;
    private final Lifecycle worldGenSettingsLifecycle;
    private BlockPosition spawnPos;
    private float spawnAngle;
    private long gameTime;
    private long dayTime;
    @Nullable
    private final NBTTagCompound loadedPlayerTag;
    private final int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private boolean initialized;
    private boolean difficultyLocked;
    private WorldBorder.c worldBorder;
    private EnderDragonBattle.a endDragonFightData;
    @Nullable
    private NBTTagCompound customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    @Nullable
    private UUID wanderingTraderId;
    private final Set<String> knownServerBrands;
    private boolean wasModded;
    private final Set<String> removedFeatureFlags;
    private final CustomFunctionCallbackTimerQueue<MinecraftServer> scheduledEvents;

    private WorldDataServer(@Nullable NBTTagCompound nbttagcompound, boolean flag, BlockPosition blockposition, float f, long i, long j, int k, int l, int i1, boolean flag1, int j1, boolean flag2, boolean flag3, boolean flag4, WorldBorder.c worldborder_c, int k1, int l1, @Nullable UUID uuid, Set<String> set, Set<String> set1, CustomFunctionCallbackTimerQueue<MinecraftServer> customfunctioncallbacktimerqueue, @Nullable NBTTagCompound nbttagcompound1, EnderDragonBattle.a enderdragonbattle_a, WorldSettings worldsettings, WorldOptions worldoptions, WorldDataServer.a worlddataserver_a, Lifecycle lifecycle) {
        this.wasModded = flag;
        this.spawnPos = blockposition;
        this.spawnAngle = f;
        this.gameTime = i;
        this.dayTime = j;
        this.version = k;
        this.clearWeatherTime = l;
        this.rainTime = i1;
        this.raining = flag1;
        this.thunderTime = j1;
        this.thundering = flag2;
        this.initialized = flag3;
        this.difficultyLocked = flag4;
        this.worldBorder = worldborder_c;
        this.wanderingTraderSpawnDelay = k1;
        this.wanderingTraderSpawnChance = l1;
        this.wanderingTraderId = uuid;
        this.knownServerBrands = set;
        this.removedFeatureFlags = set1;
        this.loadedPlayerTag = nbttagcompound;
        this.scheduledEvents = customfunctioncallbacktimerqueue;
        this.customBossEvents = nbttagcompound1;
        this.endDragonFightData = enderdragonbattle_a;
        this.settings = worldsettings;
        this.worldOptions = worldoptions;
        this.specialWorldProperty = worlddataserver_a;
        this.worldGenSettingsLifecycle = lifecycle;
    }

    public WorldDataServer(WorldSettings worldsettings, WorldOptions worldoptions, WorldDataServer.a worlddataserver_a, Lifecycle lifecycle) {
        this((NBTTagCompound) null, false, BlockPosition.ZERO, 0.0F, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_SETTINGS, 0, 0, (UUID) null, Sets.newLinkedHashSet(), new HashSet(), new CustomFunctionCallbackTimerQueue<>(CustomFunctionCallbackTimers.SERVER_CALLBACKS), (NBTTagCompound) null, EnderDragonBattle.a.DEFAULT, worldsettings.copy(), worldoptions, worlddataserver_a, lifecycle);
    }

    public static <T> WorldDataServer parse(Dynamic<T> dynamic, WorldSettings worldsettings, WorldDataServer.a worlddataserver_a, WorldOptions worldoptions, Lifecycle lifecycle) {
        long i = dynamic.get("Time").asLong(0L);
        OptionalDynamic optionaldynamic = dynamic.get("Player");
        Codec codec = NBTTagCompound.CODEC;

        Objects.requireNonNull(codec);
        NBTTagCompound nbttagcompound = (NBTTagCompound) optionaldynamic.flatMap(codec::parse).result().orElse((Object) null);
        boolean flag = dynamic.get("WasModded").asBoolean(false);
        BlockPosition blockposition = new BlockPosition(dynamic.get("SpawnX").asInt(0), dynamic.get("SpawnY").asInt(0), dynamic.get("SpawnZ").asInt(0));
        float f = dynamic.get("SpawnAngle").asFloat(0.0F);
        long j = dynamic.get("DayTime").asLong(i);
        int k = LevelVersion.parse(dynamic).levelDataVersion();
        int l = dynamic.get("clearWeatherTime").asInt(0);
        int i1 = dynamic.get("rainTime").asInt(0);
        boolean flag1 = dynamic.get("raining").asBoolean(false);
        int j1 = dynamic.get("thunderTime").asInt(0);
        boolean flag2 = dynamic.get("thundering").asBoolean(false);
        boolean flag3 = dynamic.get("initialized").asBoolean(true);
        boolean flag4 = dynamic.get("DifficultyLocked").asBoolean(false);
        WorldBorder.c worldborder_c = WorldBorder.c.read(dynamic, WorldBorder.DEFAULT_SETTINGS);
        int k1 = dynamic.get("WanderingTraderSpawnDelay").asInt(0);
        int l1 = dynamic.get("WanderingTraderSpawnChance").asInt(0);
        UUID uuid = (UUID) dynamic.get("WanderingTraderId").read(UUIDUtil.CODEC).result().orElse((Object) null);
        Set set = (Set) dynamic.get("ServerBrands").asStream().flatMap((dynamic1) -> {
            return dynamic1.asString().result().stream();
        }).collect(Collectors.toCollection(Sets::newLinkedHashSet));
        Set set1 = (Set) dynamic.get("removed_features").asStream().flatMap((dynamic1) -> {
            return dynamic1.asString().result().stream();
        }).collect(Collectors.toSet());
        CustomFunctionCallbackTimerQueue customfunctioncallbacktimerqueue = new CustomFunctionCallbackTimerQueue<>(CustomFunctionCallbackTimers.SERVER_CALLBACKS, dynamic.get("ScheduledEvents").asStream());
        NBTTagCompound nbttagcompound1 = (NBTTagCompound) dynamic.get("CustomBossEvents").orElseEmptyMap().getValue();
        DataResult dataresult = dynamic.get("DragonFight").read(EnderDragonBattle.a.CODEC);
        Logger logger = WorldDataServer.LOGGER;

        Objects.requireNonNull(logger);
        return new WorldDataServer(nbttagcompound, flag, blockposition, f, i, j, k, l, i1, flag1, j1, flag2, flag3, flag4, worldborder_c, k1, l1, uuid, set, set1, customfunctioncallbacktimerqueue, nbttagcompound1, (EnderDragonBattle.a) dataresult.resultOrPartial(logger::error).orElse(EnderDragonBattle.a.DEFAULT), worldsettings, worldoptions, worlddataserver_a, lifecycle);
    }

    @Override
    public NBTTagCompound createTag(IRegistryCustom iregistrycustom, @Nullable NBTTagCompound nbttagcompound) {
        if (nbttagcompound == null) {
            nbttagcompound = this.loadedPlayerTag;
        }

        NBTTagCompound nbttagcompound1 = new NBTTagCompound();

        this.setTagData(iregistrycustom, nbttagcompound1, nbttagcompound);
        return nbttagcompound1;
    }

    private void setTagData(IRegistryCustom iregistrycustom, NBTTagCompound nbttagcompound, @Nullable NBTTagCompound nbttagcompound1) {
        nbttagcompound.put("ServerBrands", stringCollectionToTag(this.knownServerBrands));
        nbttagcompound.putBoolean("WasModded", this.wasModded);
        if (!this.removedFeatureFlags.isEmpty()) {
            nbttagcompound.put("removed_features", stringCollectionToTag(this.removedFeatureFlags));
        }

        NBTTagCompound nbttagcompound2 = new NBTTagCompound();

        nbttagcompound2.putString("Name", SharedConstants.getCurrentVersion().getName());
        nbttagcompound2.putInt("Id", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        nbttagcompound2.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
        nbttagcompound2.putString("Series", SharedConstants.getCurrentVersion().getDataVersion().getSeries());
        nbttagcompound.put("Version", nbttagcompound2);
        GameProfileSerializer.addCurrentDataVersion(nbttagcompound);
        DynamicOps<NBTBase> dynamicops = iregistrycustom.createSerializationContext(DynamicOpsNBT.INSTANCE);
        DataResult dataresult = GeneratorSettings.encode(dynamicops, this.worldOptions, iregistrycustom);
        Logger logger = WorldDataServer.LOGGER;

        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(SystemUtils.prefix("WorldGenSettings: ", logger::error)).ifPresent((nbtbase) -> {
            nbttagcompound.put("WorldGenSettings", nbtbase);
        });
        nbttagcompound.putInt("GameType", this.settings.gameType().getId());
        nbttagcompound.putInt("SpawnX", this.spawnPos.getX());
        nbttagcompound.putInt("SpawnY", this.spawnPos.getY());
        nbttagcompound.putInt("SpawnZ", this.spawnPos.getZ());
        nbttagcompound.putFloat("SpawnAngle", this.spawnAngle);
        nbttagcompound.putLong("Time", this.gameTime);
        nbttagcompound.putLong("DayTime", this.dayTime);
        nbttagcompound.putLong("LastPlayed", SystemUtils.getEpochMillis());
        nbttagcompound.putString("LevelName", this.settings.levelName());
        nbttagcompound.putInt("version", 19133);
        nbttagcompound.putInt("clearWeatherTime", this.clearWeatherTime);
        nbttagcompound.putInt("rainTime", this.rainTime);
        nbttagcompound.putBoolean("raining", this.raining);
        nbttagcompound.putInt("thunderTime", this.thunderTime);
        nbttagcompound.putBoolean("thundering", this.thundering);
        nbttagcompound.putBoolean("hardcore", this.settings.hardcore());
        nbttagcompound.putBoolean("allowCommands", this.settings.allowCommands());
        nbttagcompound.putBoolean("initialized", this.initialized);
        this.worldBorder.write(nbttagcompound);
        nbttagcompound.putByte("Difficulty", (byte) this.settings.difficulty().getId());
        nbttagcompound.putBoolean("DifficultyLocked", this.difficultyLocked);
        nbttagcompound.put("GameRules", this.settings.gameRules().createTag());
        nbttagcompound.put("DragonFight", (NBTBase) EnderDragonBattle.a.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.endDragonFightData).getOrThrow());
        if (nbttagcompound1 != null) {
            nbttagcompound.put("Player", nbttagcompound1);
        }

        WorldDataConfiguration.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.settings.getDataConfiguration()).ifSuccess((nbtbase) -> {
            nbttagcompound.merge((NBTTagCompound) nbtbase);
        }).ifError((error) -> {
            WorldDataServer.LOGGER.warn("Failed to encode configuration {}", error.message());
        });
        if (this.customBossEvents != null) {
            nbttagcompound.put("CustomBossEvents", this.customBossEvents);
        }

        nbttagcompound.put("ScheduledEvents", this.scheduledEvents.store());
        nbttagcompound.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        nbttagcompound.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
        if (this.wanderingTraderId != null) {
            nbttagcompound.putUUID("WanderingTraderId", this.wanderingTraderId);
        }

    }

    private static NBTTagList stringCollectionToTag(Set<String> set) {
        NBTTagList nbttaglist = new NBTTagList();
        Stream stream = set.stream().map(NBTTagString::valueOf);

        Objects.requireNonNull(nbttaglist);
        stream.forEach(nbttaglist::add);
        return nbttaglist;
    }

    @Override
    public BlockPosition getSpawnPos() {
        return this.spawnPos;
    }

    @Override
    public float getSpawnAngle() {
        return this.spawnAngle;
    }

    @Override
    public long getGameTime() {
        return this.gameTime;
    }

    @Override
    public long getDayTime() {
        return this.dayTime;
    }

    @Nullable
    @Override
    public NBTTagCompound getLoadedPlayerTag() {
        return this.loadedPlayerTag;
    }

    @Override
    public void setGameTime(long i) {
        this.gameTime = i;
    }

    @Override
    public void setDayTime(long i) {
        this.dayTime = i;
    }

    @Override
    public void setSpawn(BlockPosition blockposition, float f) {
        this.spawnPos = blockposition.immutable();
        this.spawnAngle = f;
    }

    @Override
    public String getLevelName() {
        return this.settings.levelName();
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public int getClearWeatherTime() {
        return this.clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int i) {
        this.clearWeatherTime = i;
    }

    @Override
    public boolean isThundering() {
        return this.thundering;
    }

    @Override
    public void setThundering(boolean flag) {
        this.thundering = flag;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int i) {
        this.thunderTime = i;
    }

    @Override
    public boolean isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean flag) {
        this.raining = flag;
    }

    @Override
    public int getRainTime() {
        return this.rainTime;
    }

    @Override
    public void setRainTime(int i) {
        this.rainTime = i;
    }

    @Override
    public EnumGamemode getGameType() {
        return this.settings.gameType();
    }

    @Override
    public void setGameType(EnumGamemode enumgamemode) {
        this.settings = this.settings.withGameType(enumgamemode);
    }

    @Override
    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    @Override
    public boolean isAllowCommands() {
        return this.settings.allowCommands();
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void setInitialized(boolean flag) {
        this.initialized = flag;
    }

    @Override
    public GameRules getGameRules() {
        return this.settings.gameRules();
    }

    @Override
    public WorldBorder.c getWorldBorder() {
        return this.worldBorder;
    }

    @Override
    public void setWorldBorder(WorldBorder.c worldborder_c) {
        this.worldBorder = worldborder_c;
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return this.settings.difficulty();
    }

    @Override
    public void setDifficulty(EnumDifficulty enumdifficulty) {
        this.settings = this.settings.withDifficulty(enumdifficulty);
    }

    @Override
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }

    @Override
    public void setDifficultyLocked(boolean flag) {
        this.difficultyLocked = flag;
    }

    @Override
    public CustomFunctionCallbackTimerQueue<MinecraftServer> getScheduledEvents() {
        return this.scheduledEvents;
    }

    @Override
    public void fillCrashReportCategory(CrashReportSystemDetails crashreportsystemdetails, LevelHeightAccessor levelheightaccessor) {
        IWorldDataServer.super.fillCrashReportCategory(crashreportsystemdetails, levelheightaccessor);
        SaveData.super.fillCrashReportCategory(crashreportsystemdetails);
    }

    @Override
    public WorldOptions worldGenOptions() {
        return this.worldOptions;
    }

    @Override
    public boolean isFlatWorld() {
        return this.specialWorldProperty == WorldDataServer.a.FLAT;
    }

    @Override
    public boolean isDebugWorld() {
        return this.specialWorldProperty == WorldDataServer.a.DEBUG;
    }

    @Override
    public Lifecycle worldGenSettingsLifecycle() {
        return this.worldGenSettingsLifecycle;
    }

    @Override
    public EnderDragonBattle.a endDragonFightData() {
        return this.endDragonFightData;
    }

    @Override
    public void setEndDragonFightData(EnderDragonBattle.a enderdragonbattle_a) {
        this.endDragonFightData = enderdragonbattle_a;
    }

    @Override
    public WorldDataConfiguration getDataConfiguration() {
        return this.settings.getDataConfiguration();
    }

    @Override
    public void setDataConfiguration(WorldDataConfiguration worlddataconfiguration) {
        this.settings = this.settings.withDataConfiguration(worlddataconfiguration);
    }

    @Nullable
    @Override
    public NBTTagCompound getCustomBossEvents() {
        return this.customBossEvents;
    }

    @Override
    public void setCustomBossEvents(@Nullable NBTTagCompound nbttagcompound) {
        this.customBossEvents = nbttagcompound;
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return this.wanderingTraderSpawnDelay;
    }

    @Override
    public void setWanderingTraderSpawnDelay(int i) {
        this.wanderingTraderSpawnDelay = i;
    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return this.wanderingTraderSpawnChance;
    }

    @Override
    public void setWanderingTraderSpawnChance(int i) {
        this.wanderingTraderSpawnChance = i;
    }

    @Nullable
    @Override
    public UUID getWanderingTraderId() {
        return this.wanderingTraderId;
    }

    @Override
    public void setWanderingTraderId(UUID uuid) {
        this.wanderingTraderId = uuid;
    }

    @Override
    public void setModdedInfo(String s, boolean flag) {
        this.knownServerBrands.add(s);
        this.wasModded |= flag;
    }

    @Override
    public boolean wasModded() {
        return this.wasModded;
    }

    @Override
    public Set<String> getKnownServerBrands() {
        return ImmutableSet.copyOf(this.knownServerBrands);
    }

    @Override
    public Set<String> getRemovedFeatureFlags() {
        return Set.copyOf(this.removedFeatureFlags);
    }

    @Override
    public IWorldDataServer overworldData() {
        return this;
    }

    @Override
    public WorldSettings getLevelSettings() {
        return this.settings.copy();
    }

    /** @deprecated */
    @Deprecated
    public static enum a {

        NONE, FLAT, DEBUG;

        private a() {}
    }
}
