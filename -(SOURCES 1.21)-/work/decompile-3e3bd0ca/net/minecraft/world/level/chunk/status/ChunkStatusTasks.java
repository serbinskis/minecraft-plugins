package net.minecraft.world.level.chunk.status;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.ChunkTaskQueueSorter;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.thread.Mailbox;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunkExtension;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.blending.Blender;

public class ChunkStatusTasks {

    public ChunkStatusTasks() {}

    private static boolean isLighted(IChunkAccess ichunkaccess) {
        return ichunkaccess.getPersistedStatus().isOrAfter(ChunkStatus.LIGHT) && ichunkaccess.isLightCorrect();
    }

    static CompletableFuture<IChunkAccess> passThrough(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateStructureStarts(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();

        if (worldserver.getServer().getWorldData().worldGenOptions().generateStructures()) {
            worldgencontext.generator().createStructures(worldserver.registryAccess(), worldserver.getChunkSource().getGeneratorState(), worldserver.structureManager(), ichunkaccess, worldgencontext.structureManager());
        }

        worldserver.onStructureStartsAvailable(ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> loadStructureStarts(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        worldgencontext.level().onStructureStartsAvailable(ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateStructureReferences(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, staticcache2d, chunkstep, ichunkaccess);

        worldgencontext.generator().createReferences(regionlimitedworldaccess, worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateBiomes(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, staticcache2d, chunkstep, ichunkaccess);

        return worldgencontext.generator().createBiomes(worldserver.getChunkSource().randomState(), Blender.of(regionlimitedworldaccess), worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateNoise(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, staticcache2d, chunkstep, ichunkaccess);

        return worldgencontext.generator().fillFromNoise(Blender.of(regionlimitedworldaccess), worldserver.getChunkSource().randomState(), worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess).thenApply((ichunkaccess1) -> {
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

    static CompletableFuture<IChunkAccess> generateSurface(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, staticcache2d, chunkstep, ichunkaccess);

        worldgencontext.generator().buildSurface(regionlimitedworldaccess, worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), worldserver.getChunkSource().randomState(), ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateCarvers(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, staticcache2d, chunkstep, ichunkaccess);

        if (ichunkaccess instanceof ProtoChunk protochunk) {
            Blender.addAroundOldChunksCarvingMaskFilter(regionlimitedworldaccess, protochunk);
        }

        worldgencontext.generator().applyCarvers(regionlimitedworldaccess, worldserver.getSeed(), worldserver.getChunkSource().randomState(), worldserver.getBiomeManager(), worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess), ichunkaccess, WorldGenStage.Features.AIR);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> generateFeatures(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        WorldServer worldserver = worldgencontext.level();

        HeightMap.primeHeightmaps(ichunkaccess, EnumSet.of(HeightMap.Type.MOTION_BLOCKING, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, HeightMap.Type.OCEAN_FLOOR, HeightMap.Type.WORLD_SURFACE));
        RegionLimitedWorldAccess regionlimitedworldaccess = new RegionLimitedWorldAccess(worldserver, staticcache2d, chunkstep, ichunkaccess);

        worldgencontext.generator().applyBiomeDecoration(regionlimitedworldaccess, ichunkaccess, worldserver.structureManager().forWorldGenRegion(regionlimitedworldaccess));
        Blender.generateBorderTicks(regionlimitedworldaccess, ichunkaccess);
        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> initializeLight(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        LightEngineThreaded lightenginethreaded = worldgencontext.lightEngine();

        ichunkaccess.initializeLightSources();
        ((ProtoChunk) ichunkaccess).setLightEngine(lightenginethreaded);
        boolean flag = isLighted(ichunkaccess);

        return lightenginethreaded.initializeLight(ichunkaccess, flag);
    }

    static CompletableFuture<IChunkAccess> light(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        boolean flag = isLighted(ichunkaccess);

        return worldgencontext.lightEngine().lightChunk(ichunkaccess, flag);
    }

    static CompletableFuture<IChunkAccess> generateSpawn(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        if (!ichunkaccess.isUpgrading()) {
            worldgencontext.generator().spawnOriginalMobs(new RegionLimitedWorldAccess(worldgencontext.level(), staticcache2d, chunkstep, ichunkaccess));
        }

        return CompletableFuture.completedFuture(ichunkaccess);
    }

    static CompletableFuture<IChunkAccess> full(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
        GenerationChunkHolder generationchunkholder = (GenerationChunkHolder) staticcache2d.get(chunkcoordintpair.x, chunkcoordintpair.z);

        return CompletableFuture.supplyAsync(() -> {
            ProtoChunk protochunk = (ProtoChunk) ichunkaccess;
            WorldServer worldserver = worldgencontext.level();
            Chunk chunk;

            if (protochunk instanceof ProtoChunkExtension) {
                chunk = ((ProtoChunkExtension) protochunk).getWrapped();
            } else {
                chunk = new Chunk(worldserver, protochunk, (chunk1) -> {
                    postLoadProtoChunk(worldserver, protochunk.getEntities());
                });
                generationchunkholder.replaceProtoChunk(new ProtoChunkExtension(chunk, false));
            }

            Objects.requireNonNull(generationchunkholder);
            chunk.setFullStatus(generationchunkholder::getFullStatus);
            chunk.runPostLoad();
            chunk.setLoaded(true);
            chunk.registerAllBlockEntitiesAfterLevelLoad();
            chunk.registerTickContainerInLevel(worldserver);
            return chunk;
        }, (runnable) -> {
            Mailbox mailbox = worldgencontext.mainThreadMailBox();
            long i = chunkcoordintpair.toLong();

            Objects.requireNonNull(generationchunkholder);
            mailbox.tell(ChunkTaskQueueSorter.message(runnable, i, generationchunkholder::getTicketLevel));
        });
    }

    private static void postLoadProtoChunk(WorldServer worldserver, List<NBTTagCompound> list) {
        if (!list.isEmpty()) {
            worldserver.addWorldGenChunkEntities(EntityTypes.loadEntitiesRecursive(list, worldserver));
        }

    }
}
