package net.minecraft.util.worldupdate;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMaps;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.IChunkLoader;
import net.minecraft.world.level.chunk.storage.RecreatingChunkStorage;
import net.minecraft.world.level.chunk.storage.RecreatingSimpleRegionStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.WorldPersistentData;
import org.slf4j.Logger;

public class WorldUpgrader {

    static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory THREAD_FACTORY = (new ThreadFactoryBuilder()).setDaemon(true).build();
    private static final String NEW_DIRECTORY_PREFIX = "new_";
    static final IChatMutableComponent STATUS_UPGRADING_POI = IChatBaseComponent.translatable("optimizeWorld.stage.upgrading.poi");
    static final IChatMutableComponent STATUS_FINISHED_POI = IChatBaseComponent.translatable("optimizeWorld.stage.finished.poi");
    static final IChatMutableComponent STATUS_UPGRADING_ENTITIES = IChatBaseComponent.translatable("optimizeWorld.stage.upgrading.entities");
    static final IChatMutableComponent STATUS_FINISHED_ENTITIES = IChatBaseComponent.translatable("optimizeWorld.stage.finished.entities");
    static final IChatMutableComponent STATUS_UPGRADING_CHUNKS = IChatBaseComponent.translatable("optimizeWorld.stage.upgrading.chunks");
    static final IChatMutableComponent STATUS_FINISHED_CHUNKS = IChatBaseComponent.translatable("optimizeWorld.stage.finished.chunks");
    final IRegistry<WorldDimension> dimensions;
    final Set<ResourceKey<World>> levels;
    final boolean eraseCache;
    final boolean recreateRegionFiles;
    final Convertable.ConversionSession levelStorage;
    private final Thread thread;
    final DataFixer dataFixer;
    volatile boolean running = true;
    private volatile boolean finished;
    volatile float progress;
    volatile int totalChunks;
    volatile int totalFiles;
    volatile int converted;
    volatile int skipped;
    final Reference2FloatMap<ResourceKey<World>> progressMap = Reference2FloatMaps.synchronize(new Reference2FloatOpenHashMap());
    volatile IChatBaseComponent status = IChatBaseComponent.translatable("optimizeWorld.stage.counting");
    static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    final WorldPersistentData overworldDataStorage;

    public WorldUpgrader(Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer, IRegistryCustom iregistrycustom, boolean flag, boolean flag1) {
        this.dimensions = iregistrycustom.registryOrThrow(Registries.LEVEL_STEM);
        this.levels = (Set) java.util.stream.Stream.of(convertable_conversionsession.dimensionType).map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet()); // CraftBukkit
        this.eraseCache = flag;
        this.dataFixer = datafixer;
        this.levelStorage = convertable_conversionsession;
        this.overworldDataStorage = new WorldPersistentData(this.levelStorage.getDimensionPath(World.OVERWORLD).resolve("data").toFile(), datafixer, iregistrycustom);
        this.recreateRegionFiles = flag1;
        this.thread = WorldUpgrader.THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((thread, throwable) -> {
            WorldUpgrader.LOGGER.error("Error upgrading world", throwable);
            this.status = IChatBaseComponent.translatable("optimizeWorld.stage.failed");
            this.finished = true;
        });
        this.thread.start();
    }

    public void cancel() {
        this.running = false;

        try {
            this.thread.join();
        } catch (InterruptedException interruptedexception) {
            ;
        }

    }

    private void work() {
        long i = SystemUtils.getMillis();

        WorldUpgrader.LOGGER.info("Upgrading entities");
        (new WorldUpgrader.d(this)).upgrade();
        WorldUpgrader.LOGGER.info("Upgrading POIs");
        (new WorldUpgrader.f(this)).upgrade();
        WorldUpgrader.LOGGER.info("Upgrading blocks");
        (new WorldUpgrader.b()).upgrade();
        this.overworldDataStorage.save();
        i = SystemUtils.getMillis() - i;
        WorldUpgrader.LOGGER.info("World optimizaton finished after {} seconds", i / 1000L);
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public Set<ResourceKey<World>> levels() {
        return this.levels;
    }

    public float dimensionProgress(ResourceKey<World> resourcekey) {
        return this.progressMap.getFloat(resourcekey);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public int getConverted() {
        return this.converted;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public IChatBaseComponent getStatus() {
        return this.status;
    }

    static Path resolveRecreateDirectory(Path path) {
        return path.resolveSibling("new_" + path.getFileName().toString());
    }

    private class d extends WorldUpgrader.g {

        d(final WorldUpgrader worldupgrader) {
            super(DataFixTypes.ENTITY_CHUNK, "entities", WorldUpgrader.STATUS_UPGRADING_ENTITIES, WorldUpgrader.STATUS_FINISHED_ENTITIES);
        }

        @Override
        protected NBTTagCompound upgradeTag(SimpleRegionStorage simpleregionstorage, NBTTagCompound nbttagcompound) {
            return simpleregionstorage.upgradeChunkTag(nbttagcompound, -1);
        }
    }

    private class f extends WorldUpgrader.g {

        f(final WorldUpgrader worldupgrader) {
            super(DataFixTypes.POI_CHUNK, "poi", WorldUpgrader.STATUS_UPGRADING_POI, WorldUpgrader.STATUS_FINISHED_POI);
        }

        @Override
        protected NBTTagCompound upgradeTag(SimpleRegionStorage simpleregionstorage, NBTTagCompound nbttagcompound) {
            return simpleregionstorage.upgradeChunkTag(nbttagcompound, 1945);
        }
    }

    private class b extends WorldUpgrader.a<IChunkLoader> {

        b() {
            super(DataFixTypes.CHUNK, "chunk", "region", WorldUpgrader.STATUS_UPGRADING_CHUNKS, WorldUpgrader.STATUS_FINISHED_CHUNKS);
        }

        protected boolean tryProcessOnePosition(IChunkLoader ichunkloader, ChunkCoordIntPair chunkcoordintpair, ResourceKey<World> resourcekey) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) ((Optional) ichunkloader.read(chunkcoordintpair).join()).orElse((Object) null);

            if (nbttagcompound != null) {
                int i = IChunkLoader.getVersion(nbttagcompound);
                ChunkGenerator chunkgenerator = ((WorldDimension) WorldUpgrader.this.dimensions.getOrThrow(Registries.levelToLevelStem(resourcekey))).generator();
                NBTTagCompound nbttagcompound1 = ichunkloader.upgradeChunkTag(Registries.levelToLevelStem(resourcekey), () -> { // CraftBukkit
                    return WorldUpgrader.this.overworldDataStorage;
                }, nbttagcompound, chunkgenerator.getTypeNameForDataFixer(), chunkcoordintpair, null); // CraftBukkit
                ChunkCoordIntPair chunkcoordintpair1 = new ChunkCoordIntPair(nbttagcompound1.getInt("xPos"), nbttagcompound1.getInt("zPos"));

                if (!chunkcoordintpair1.equals(chunkcoordintpair)) {
                    WorldUpgrader.LOGGER.warn("Chunk {} has invalid position {}", chunkcoordintpair, chunkcoordintpair1);
                }

                boolean flag = i < SharedConstants.getCurrentVersion().getDataVersion().getVersion();

                if (WorldUpgrader.this.eraseCache) {
                    flag = flag || nbttagcompound1.contains("Heightmaps");
                    nbttagcompound1.remove("Heightmaps");
                    flag = flag || nbttagcompound1.contains("isLightOn");
                    nbttagcompound1.remove("isLightOn");
                    NBTTagList nbttaglist = nbttagcompound1.getList("sections", 10);

                    for (int j = 0; j < nbttaglist.size(); ++j) {
                        NBTTagCompound nbttagcompound2 = nbttaglist.getCompound(j);

                        flag = flag || nbttagcompound2.contains("BlockLight");
                        nbttagcompound2.remove("BlockLight");
                        flag = flag || nbttagcompound2.contains("SkyLight");
                        nbttagcompound2.remove("SkyLight");
                    }
                }

                if (flag || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }

                    this.previousWriteFuture = ichunkloader.write(chunkcoordintpair, nbttagcompound1);
                    return true;
                }
            }

            return false;
        }

        @Override
        protected IChunkLoader createStorage(RegionStorageInfo regionstorageinfo, Path path) {
            return (IChunkLoader) (WorldUpgrader.this.recreateRegionFiles ? new RecreatingChunkStorage(regionstorageinfo.withTypeSuffix("source"), path, regionstorageinfo.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory(path), WorldUpgrader.this.dataFixer, true) : new IChunkLoader(regionstorageinfo, path, WorldUpgrader.this.dataFixer, true));
        }
    }

    private abstract class g extends WorldUpgrader.a<SimpleRegionStorage> {

        g(final DataFixTypes datafixtypes, final String s, final IChatMutableComponent ichatmutablecomponent, final IChatMutableComponent ichatmutablecomponent1) {
            super(datafixtypes, s, s, ichatmutablecomponent, ichatmutablecomponent1);
        }

        @Override
        protected SimpleRegionStorage createStorage(RegionStorageInfo regionstorageinfo, Path path) {
            return (SimpleRegionStorage) (WorldUpgrader.this.recreateRegionFiles ? new RecreatingSimpleRegionStorage(regionstorageinfo.withTypeSuffix("source"), path, regionstorageinfo.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory(path), WorldUpgrader.this.dataFixer, true, this.dataFixType) : new SimpleRegionStorage(regionstorageinfo, path, WorldUpgrader.this.dataFixer, true, this.dataFixType));
        }

        protected boolean tryProcessOnePosition(SimpleRegionStorage simpleregionstorage, ChunkCoordIntPair chunkcoordintpair, ResourceKey<World> resourcekey) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) ((Optional) simpleregionstorage.read(chunkcoordintpair).join()).orElse((Object) null);

            if (nbttagcompound != null) {
                int i = IChunkLoader.getVersion(nbttagcompound);
                NBTTagCompound nbttagcompound1 = this.upgradeTag(simpleregionstorage, nbttagcompound);
                boolean flag = i < SharedConstants.getCurrentVersion().getDataVersion().getVersion();

                if (flag || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }

                    this.previousWriteFuture = simpleregionstorage.write(chunkcoordintpair, nbttagcompound1);
                    return true;
                }
            }

            return false;
        }

        protected abstract NBTTagCompound upgradeTag(SimpleRegionStorage simpleregionstorage, NBTTagCompound nbttagcompound);
    }

    private abstract class a<T extends AutoCloseable> {

        private final IChatMutableComponent upgradingStatus;
        private final IChatMutableComponent finishedStatus;
        private final String type;
        private final String folderName;
        @Nullable
        protected CompletableFuture<Void> previousWriteFuture;
        protected final DataFixTypes dataFixType;

        a(final DataFixTypes datafixtypes, final String s, final String s1, final IChatMutableComponent ichatmutablecomponent, final IChatMutableComponent ichatmutablecomponent1) {
            this.dataFixType = datafixtypes;
            this.type = s;
            this.folderName = s1;
            this.upgradingStatus = ichatmutablecomponent;
            this.finishedStatus = ichatmutablecomponent1;
        }

        public void upgrade() {
            WorldUpgrader.this.totalFiles = 0;
            WorldUpgrader.this.totalChunks = 0;
            WorldUpgrader.this.converted = 0;
            WorldUpgrader.this.skipped = 0;
            List<WorldUpgrader.c<T>> list = this.getDimensionsToUpgrade();

            if (WorldUpgrader.this.totalChunks != 0) {
                float f = (float) WorldUpgrader.this.totalFiles;

                WorldUpgrader.this.status = this.upgradingStatus;

                while (WorldUpgrader.this.running) {
                    boolean flag = false;
                    float f1 = 0.0F;

                    float f2;

                    for (Iterator iterator = list.iterator(); iterator.hasNext(); f1 += f2) {
                        WorldUpgrader.c<T> worldupgrader_c = (WorldUpgrader.c) iterator.next();
                        ResourceKey<World> resourcekey = worldupgrader_c.dimensionKey;
                        ListIterator<WorldUpgrader.e> listiterator = worldupgrader_c.files;
                        T t0 = (T) worldupgrader_c.storage; // CraftBukkit - decompile error

                        if (listiterator.hasNext()) {
                            WorldUpgrader.e worldupgrader_e = (WorldUpgrader.e) listiterator.next();
                            boolean flag1 = true;

                            for (Iterator iterator1 = worldupgrader_e.chunksToUpgrade.iterator(); iterator1.hasNext(); flag = true) {
                                ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) iterator1.next();

                                flag1 = flag1 && this.processOnePosition(resourcekey, t0, chunkcoordintpair);
                            }

                            if (WorldUpgrader.this.recreateRegionFiles) {
                                if (flag1) {
                                    this.onFileFinished(worldupgrader_e.file);
                                } else {
                                    WorldUpgrader.LOGGER.error("Failed to convert region file {}", worldupgrader_e.file.getPath());
                                }
                            }
                        }

                        f2 = (float) listiterator.nextIndex() / f;
                        WorldUpgrader.this.progressMap.put(resourcekey, f2);
                    }

                    WorldUpgrader.this.progress = f1;
                    if (!flag) {
                        break;
                    }
                }

                WorldUpgrader.this.status = this.finishedStatus;
                Iterator iterator2 = list.iterator();

                while (iterator2.hasNext()) {
                    WorldUpgrader.c<T> worldupgrader_c1 = (WorldUpgrader.c) iterator2.next();

                    try {
                        ((AutoCloseable) worldupgrader_c1.storage).close();
                    } catch (Exception exception) {
                        WorldUpgrader.LOGGER.error("Error upgrading chunk", exception);
                    }
                }

            }
        }

        private List<WorldUpgrader.c<T>> getDimensionsToUpgrade() {
            List<WorldUpgrader.c<T>> list = Lists.newArrayList();
            Iterator iterator = WorldUpgrader.this.levels.iterator();

            while (iterator.hasNext()) {
                ResourceKey<World> resourcekey = (ResourceKey) iterator.next();
                RegionStorageInfo regionstorageinfo = new RegionStorageInfo(WorldUpgrader.this.levelStorage.getLevelId(), resourcekey, this.type);
                Path path = WorldUpgrader.this.levelStorage.getDimensionPath(resourcekey).resolve(this.folderName);
                T t0 = this.createStorage(regionstorageinfo, path);
                ListIterator<WorldUpgrader.e> listiterator = this.getFilesToProcess(regionstorageinfo, path);

                list.add(new WorldUpgrader.c<>(resourcekey, t0, listiterator));
            }

            return list;
        }

        protected abstract T createStorage(RegionStorageInfo regionstorageinfo, Path path);

        private ListIterator<WorldUpgrader.e> getFilesToProcess(RegionStorageInfo regionstorageinfo, Path path) {
            List<WorldUpgrader.e> list = getAllChunkPositions(regionstorageinfo, path);

            WorldUpgrader.this.totalFiles += list.size();
            WorldUpgrader.this.totalChunks += list.stream().mapToInt((worldupgrader_e) -> {
                return worldupgrader_e.chunksToUpgrade.size();
            }).sum();
            return list.listIterator();
        }

        private static List<WorldUpgrader.e> getAllChunkPositions(RegionStorageInfo regionstorageinfo, Path path) {
            File[] afile = path.toFile().listFiles((file, s) -> {
                return s.endsWith(".mca");
            });

            if (afile == null) {
                return List.of();
            } else {
                List<WorldUpgrader.e> list = Lists.newArrayList();
                File[] afile1 = afile;
                int i = afile.length;

                for (int j = 0; j < i; ++j) {
                    File file = afile1[j];
                    Matcher matcher = WorldUpgrader.REGEX.matcher(file.getName());

                    if (matcher.matches()) {
                        int k = Integer.parseInt(matcher.group(1)) << 5;
                        int l = Integer.parseInt(matcher.group(2)) << 5;
                        List<ChunkCoordIntPair> list1 = Lists.newArrayList();

                        try {
                            RegionFile regionfile = new RegionFile(regionstorageinfo, file.toPath(), path, true);

                            try {
                                for (int i1 = 0; i1 < 32; ++i1) {
                                    for (int j1 = 0; j1 < 32; ++j1) {
                                        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i1 + k, j1 + l);

                                        if (regionfile.doesChunkExist(chunkcoordintpair)) {
                                            list1.add(chunkcoordintpair);
                                        }
                                    }
                                }

                                if (!list1.isEmpty()) {
                                    list.add(new WorldUpgrader.e(regionfile, list1));
                                }
                            } catch (Throwable throwable) {
                                try {
                                    regionfile.close();
                                } catch (Throwable throwable1) {
                                    throwable.addSuppressed(throwable1);
                                }

                                throw throwable;
                            }

                            regionfile.close();
                        } catch (Throwable throwable2) {
                            WorldUpgrader.LOGGER.error("Failed to read chunks from region file {}", file.toPath(), throwable2);
                        }
                    }
                }

                return list;
            }
        }

        private boolean processOnePosition(ResourceKey<World> resourcekey, T t0, ChunkCoordIntPair chunkcoordintpair) {
            boolean flag = false;

            try {
                flag = this.tryProcessOnePosition(t0, chunkcoordintpair, resourcekey);
            } catch (CompletionException | ReportedException reportedexception) {
                Throwable throwable = reportedexception.getCause();

                if (!(throwable instanceof IOException)) {
                    throw reportedexception;
                }

                WorldUpgrader.LOGGER.error("Error upgrading chunk {}", chunkcoordintpair, throwable);
            }

            if (flag) {
                ++WorldUpgrader.this.converted;
            } else {
                ++WorldUpgrader.this.skipped;
            }

            return flag;
        }

        protected abstract boolean tryProcessOnePosition(T t0, ChunkCoordIntPair chunkcoordintpair, ResourceKey<World> resourcekey);

        private void onFileFinished(RegionFile regionfile) {
            if (WorldUpgrader.this.recreateRegionFiles) {
                if (this.previousWriteFuture != null) {
                    this.previousWriteFuture.join();
                }

                Path path = regionfile.getPath();
                Path path1 = path.getParent();
                Path path2 = WorldUpgrader.resolveRecreateDirectory(path1).resolve(path.getFileName().toString());

                try {
                    if (path2.toFile().exists()) {
                        Files.delete(path);
                        Files.move(path2, path);
                    } else {
                        WorldUpgrader.LOGGER.error("Failed to replace an old region file. New file {} does not exist.", path2);
                    }
                } catch (IOException ioexception) {
                    WorldUpgrader.LOGGER.error("Failed to replace an old region file", ioexception);
                }

            }
        }
    }

    static record e(RegionFile file, List<ChunkCoordIntPair> chunksToUpgrade) {

    }

    static record c<T>(ResourceKey<World> dimensionKey, T storage, ListIterator<WorldUpgrader.e> files) {

    }
}
