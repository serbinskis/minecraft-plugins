package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.HeightMap;

public class ChunkStatus {

    public static final int MAX_STRUCTURE_DISTANCE = 8;
    private static final EnumSet<HeightMap.Type> PRE_FEATURES = EnumSet.of(HeightMap.Type.OCEAN_FLOOR_WG, HeightMap.Type.WORLD_SURFACE_WG);
    public static final EnumSet<HeightMap.Type> POST_FEATURES = EnumSet.of(HeightMap.Type.OCEAN_FLOOR, HeightMap.Type.WORLD_SURFACE, HeightMap.Type.MOTION_BLOCKING, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES);
    public static final ChunkStatus EMPTY = register("empty", (ChunkStatus) null, -1, false, ChunkStatus.PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateEmpty, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus STRUCTURE_STARTS = register("structure_starts", ChunkStatus.EMPTY, 0, false, ChunkStatus.PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateStructureStarts, ChunkStatusTasks::loadStructureStarts);
    public static final ChunkStatus STRUCTURE_REFERENCES = register("structure_references", ChunkStatus.STRUCTURE_STARTS, 8, false, ChunkStatus.PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateStructureReferences, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus BIOMES = register("biomes", ChunkStatus.STRUCTURE_REFERENCES, 8, false, ChunkStatus.PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateBiomes, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus NOISE = register("noise", ChunkStatus.BIOMES, 8, false, ChunkStatus.PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateNoise, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus SURFACE = register("surface", ChunkStatus.NOISE, 8, false, ChunkStatus.PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateSurface, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus CARVERS = register("carvers", ChunkStatus.SURFACE, 8, false, ChunkStatus.POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateCarvers, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus FEATURES = register("features", ChunkStatus.CARVERS, 8, false, ChunkStatus.POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateFeatures, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus INITIALIZE_LIGHT = register("initialize_light", ChunkStatus.FEATURES, 0, false, ChunkStatus.POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateInitializeLight, ChunkStatusTasks::loadInitializeLight);
    public static final ChunkStatus LIGHT = register("light", ChunkStatus.INITIALIZE_LIGHT, 1, true, ChunkStatus.POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateLight, ChunkStatusTasks::loadLight);
    public static final ChunkStatus SPAWN = register("spawn", ChunkStatus.LIGHT, 1, false, ChunkStatus.POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateSpawn, ChunkStatusTasks::loadPassThrough);
    public static final ChunkStatus FULL = register("full", ChunkStatus.SPAWN, 0, false, ChunkStatus.POST_FEATURES, ChunkType.LEVELCHUNK, ChunkStatusTasks::generateFull, ChunkStatusTasks::loadFull);
    private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(ChunkStatus.FULL, ChunkStatus.INITIALIZE_LIGHT, ChunkStatus.CARVERS, ChunkStatus.BIOMES, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_STARTS, new ChunkStatus[0]);
    private static final IntList RANGE_BY_STATUS = (IntList) SystemUtils.make(new IntArrayList(getStatusList().size()), (intarraylist) -> {
        int i = 0;

        for (int j = getStatusList().size() - 1; j >= 0; --j) {
            while (i + 1 < ChunkStatus.STATUS_BY_RANGE.size() && j <= ((ChunkStatus) ChunkStatus.STATUS_BY_RANGE.get(i + 1)).getIndex()) {
                ++i;
            }

            intarraylist.add(0, i);
        }

    });
    private final int index;
    private final ChunkStatus parent;
    private final ChunkStatus.a generationTask;
    private final ChunkStatus.b loadingTask;
    private final int range;
    private final boolean hasLoadDependencies;
    private final ChunkType chunkType;
    private final EnumSet<HeightMap.Type> heightmapsAfter;

    private static ChunkStatus register(String s, @Nullable ChunkStatus chunkstatus, int i, boolean flag, EnumSet<HeightMap.Type> enumset, ChunkType chunktype, ChunkStatus.a chunkstatus_a, ChunkStatus.b chunkstatus_b) {
        return (ChunkStatus) IRegistry.register(BuiltInRegistries.CHUNK_STATUS, s, new ChunkStatus(chunkstatus, i, flag, enumset, chunktype, chunkstatus_a, chunkstatus_b));
    }

    public static List<ChunkStatus> getStatusList() {
        List<ChunkStatus> list = Lists.newArrayList();

        ChunkStatus chunkstatus;

        for (chunkstatus = ChunkStatus.FULL; chunkstatus.getParent() != chunkstatus; chunkstatus = chunkstatus.getParent()) {
            list.add(chunkstatus);
        }

        list.add(chunkstatus);
        Collections.reverse(list);
        return list;
    }

    public static ChunkStatus getStatusAroundFullChunk(int i) {
        return i >= ChunkStatus.STATUS_BY_RANGE.size() ? ChunkStatus.EMPTY : (i < 0 ? ChunkStatus.FULL : (ChunkStatus) ChunkStatus.STATUS_BY_RANGE.get(i));
    }

    public static int maxDistance() {
        return ChunkStatus.STATUS_BY_RANGE.size();
    }

    public static int getDistance(ChunkStatus chunkstatus) {
        return ChunkStatus.RANGE_BY_STATUS.getInt(chunkstatus.getIndex());
    }

    ChunkStatus(@Nullable ChunkStatus chunkstatus, int i, boolean flag, EnumSet<HeightMap.Type> enumset, ChunkType chunktype, ChunkStatus.a chunkstatus_a, ChunkStatus.b chunkstatus_b) {
        this.parent = chunkstatus == null ? this : chunkstatus;
        this.generationTask = chunkstatus_a;
        this.loadingTask = chunkstatus_b;
        this.range = i;
        this.hasLoadDependencies = flag;
        this.chunkType = chunktype;
        this.heightmapsAfter = enumset;
        this.index = chunkstatus == null ? 0 : chunkstatus.getIndex() + 1;
    }

    public int getIndex() {
        return this.index;
    }

    public ChunkStatus getParent() {
        return this.parent;
    }

    public CompletableFuture<IChunkAccess> generate(WorldGenContext worldgencontext, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list) {
        IChunkAccess ichunkaccess = (IChunkAccess) list.get(list.size() / 2);
        ProfiledDuration profiledduration = JvmProfiler.INSTANCE.onChunkGenerate(ichunkaccess.getPos(), worldgencontext.level().dimension(), this.toString());

        return this.generationTask.doWork(worldgencontext, this, executor, tofullchunk, list, ichunkaccess).thenApply((ichunkaccess1) -> {
            if (ichunkaccess1 instanceof ProtoChunk protochunk) {
                if (!protochunk.getStatus().isOrAfter(this)) {
                    protochunk.setStatus(this);
                }
            }

            if (profiledduration != null) {
                profiledduration.finish();
            }

            return ichunkaccess1;
        });
    }

    public CompletableFuture<IChunkAccess> load(WorldGenContext worldgencontext, ToFullChunk tofullchunk, IChunkAccess ichunkaccess) {
        return this.loadingTask.doWork(worldgencontext, this, tofullchunk, ichunkaccess);
    }

    public int getRange() {
        return this.range;
    }

    public boolean hasLoadDependencies() {
        return this.hasLoadDependencies;
    }

    public ChunkType getChunkType() {
        return this.chunkType;
    }

    public static ChunkStatus byName(String s) {
        return (ChunkStatus) BuiltInRegistries.CHUNK_STATUS.get(MinecraftKey.tryParse(s));
    }

    public EnumSet<HeightMap.Type> heightmapsAfter() {
        return this.heightmapsAfter;
    }

    public boolean isOrAfter(ChunkStatus chunkstatus) {
        return this.getIndex() >= chunkstatus.getIndex();
    }

    public String toString() {
        return BuiltInRegistries.CHUNK_STATUS.getKey(this).toString();
    }

    @FunctionalInterface
    protected interface a {

        CompletableFuture<IChunkAccess> doWork(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess);
    }

    @FunctionalInterface
    protected interface b {

        CompletableFuture<IChunkAccess> doWork(WorldGenContext worldgencontext, ChunkStatus chunkstatus, ToFullChunk tofullchunk, IChunkAccess ichunkaccess);
    }
}
