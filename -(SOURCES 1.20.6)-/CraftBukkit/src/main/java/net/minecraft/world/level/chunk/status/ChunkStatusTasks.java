package net.minecraft.world.level.chunk.status;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.blending.Blender;

public class ChunkStatusTasks {

    public ChunkStatusTasks() {}

    private static boolean isLighted(IChunkAccess ichunkaccess) {
        return ichunkaccess.getStatus().isOrAfter(ChunkStatus.LIGHT) && ichunkaccess.isLightCorrect();
    }

    static CompletableFuture<IChunkAccess> generateEmpty(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> loadPassThrough(WorldGenContext worldgencontext, ChunkStatus chunkstatus, ToFullChunk tofullchunk, IChunkAccess ichunkaccess) {
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateStructureStarts(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();

        if (worldserver.serverLevelData.worldGenOptions().generateStructures()) { // CraftBukkit
            worldgencontext.generator().createStructures(worldserver.registryAccess(), worldserver.getChunkSource().getGeneratorState(), worldserver.structureManager(), ichunkaccess, worldgencontext.structureManager());
        }

        worldserver.onStructureStartsAvailable(ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> loadStructureStarts(WorldGenContext worldgencontext, ChunkStatus chunkstatus, ToFullChunk tofullchunk, IChunkAccess ichunkaccess) {
        worldgencontext.level().onStructureStartsAvailable(ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateStructureReferences(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, list, chunkstatus, -1);

        worldgencontext.generator().createReferences(regionlimitedworldaccess, worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateBiomes(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, list, chunkstatus, -1);

        return worldgencontext.generator().createBiomes(executor, worldserver.getChunkSource().randomState(), Blender.of(regionlimitedworldaccess), worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateNoise(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, list, chunkstatus, 0);

        return worldgencontext.generator().fillFromNoise(executor, Blender.of(regionlimitedworldaccess), worldserver.getChunkSource().randomState(), worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess).thenApply((ichunkaccess1) -> {
            if (ichunkaccess1 instanceof ProtoChunk protochunk) {
                BelowZeroRetrogen belowzeroretrogen = protochunk.getBelowZeroRetrogen();

                if (belowzeroretrogen != null) {
                    BelowZeroRetrogen.replaceOldBedrock(protochunk);
                    if (belowzeroretrogen.hasBedrockHoles()) {
                        belowzeroretrogen.applyBedrockMask(protochunk);
                    }
                }
            }

            return ichunkaccess1;
        });
    }

    static CompletableFuture<IChunkAccess> generateSurface(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, list, chunkstatus, 0);

        worldgencontext.generator().buildSurface(regionlimitedworldaccess, worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), worldserver.getChunkSource().randomState(), ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateCarvers(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, list, chunkstatus, 0);

        if (ichunkaccess instanceof ProtoChunk protochunk) {
            Blender.addAroundOldChunksCarvingMaskFilter(regionlimitedworldaccess, protochunk);
        }

        worldgencontext.generator().applyCarvers(regionlimitedworldaccess, worldserver.getSeed(), worldserver.getChunkSource().randomState(), worldserver.getBiomeManager(), worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess, WorldGenStage.Features.AIR);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateFeatures(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();

        HeightMap.primeHeightmaps(ichunkaccess, EnumSet.of(HeightMap.Type.MOTION_BLOCKING, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, HeightMap.Type.OCEAN_FLOOR, HeightMap.Type.WORLD_SURFACE));
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, list, chunkstatus, 1);

        worldgencontext.generator().applyBiomeDecoration(regionlimitedworldaccess, ichunkaccess, worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess));
        Blender.generateBorderTicks(regionlimitedworldaccess, ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateInitializeLight(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        return initializeLight(worldgencontext.lightEngine(), ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> loadInitializeLight(WorldGenContext worldgencontext, ChunkStatus chunkstatus, ToFullChunk tofullchunk, IChunkAccess ichunkaccess) {
        return initializeLight(worldgencontext.lightEngine(), ichunkaccess);
    }

    private static CompletableFuture<IChunkAccess> initializeLight(LightEngineThreaded lightenginethreaded, IChunkAccess ichunkaccess) {
        ichunkaccess.initializeLightSources();
        ((ProtoChunk) ichunkaccess).setLightEngine(lightenginethreaded);
        boolean flag = isLighted(ichunkaccess);

        return lightenginethreaded.initializeLight(ichunkaccess, flag);
    }

    static CompletableFuture<IChunkAccess> generateLight(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        return lightChunk(worldgencontext.lightEngine(), ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> loadLight(WorldGenContext worldgencontext, ChunkStatus chunkstatus, ToFullChunk tofullchunk, IChunkAccess ichunkaccess) {
        return lightChunk(worldgencontext.lightEngine(), ichunkaccess);
    }

    private static CompletableFuture<IChunkAccess> lightChunk(LightEngineThreaded lightenginethreaded, IChunkAccess ichunkaccess) {
        boolean flag = isLighted(ichunkaccess);

        return lightenginethreaded.lightChunk(ichunkaccess, flag);
    }

    static CompletableFuture<IChunkAccess> generateSpawn(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        if (!ichunkaccess.isUpgrading()) {
            worldgencontext.generator().spawnOriginalMobs(new RegionLimitedWorldAccess(worldgencontext.level(), list, chunkstatus, -1));
        }

        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateFull(WorldGenContext worldgencontext, ChunkStatus chunkstatus, Executor executor, ToFullChunk tofullchunk, List<IChunkAccess> list, IChunkAccess ichunkaccess) {
        return tofullchunk.apply(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> loadFull(WorldGenContext worldgencontext, ChunkStatus chunkstatus, ToFullChunk tofullchunk, IChunkAccess ichunkaccess) {
        return tofullchunk.apply(ichunkaccess);
    }
}
