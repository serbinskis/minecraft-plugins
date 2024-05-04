package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.FileUtils;
import net.minecraft.ReportedException;
import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.ResourcePackRepository;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.SessionLock;
import net.minecraft.util.datafix.DataConverterRegistry;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.WorldSettings;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.levelgen.GeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraft.world.level.validation.PathAllowList;
import org.slf4j.Logger;

// CraftBukkit start
import net.minecraft.world.level.dimension.WorldDimension;
// CraftBukkit end

public class Convertable {

    static final Logger LOGGER = LogUtils.getLogger();
    static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();
    private static final String TAG_DATA = "Data";
    private static final PathMatcher NO_SYMLINKS_ALLOWED = (path) -> {
        return false;
    };
    public static final String ALLOWED_SYMLINKS_CONFIG_NAME = "allowed_symlinks.txt";
    private static final int UNCOMPRESSED_NBT_QUOTA = 104857600;
    private static final int DISK_SPACE_WARNING_THRESHOLD = 67108864;
    public final Path baseDir;
    private final Path backupDir;
    final DataFixer fixerUpper;
    private final DirectoryValidator worldDirValidator;

    public Convertable(Path path, Path path1, DirectoryValidator directoryvalidator, DataFixer datafixer) {
        this.fixerUpper = datafixer;

        try {
            FileUtils.createDirectoriesSafe(path);
        } catch (IOException ioexception) {
            throw new UncheckedIOException(ioexception);
        }

        this.baseDir = path;
        this.backupDir = path1;
        this.worldDirValidator = directoryvalidator;
    }

    public static DirectoryValidator parseValidator(Path path) {
        if (Files.exists(path, new LinkOption[0])) {
            try {
                BufferedReader bufferedreader = Files.newBufferedReader(path);

                DirectoryValidator directoryvalidator;

                try {
                    directoryvalidator = new DirectoryValidator(PathAllowList.readPlain(bufferedreader));
                } catch (Throwable throwable) {
                    if (bufferedreader != null) {
                        try {
                            bufferedreader.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (bufferedreader != null) {
                    bufferedreader.close();
                }

                return directoryvalidator;
            } catch (Exception exception) {
                Convertable.LOGGER.error("Failed to parse {}, disallowing all symbolic links", "allowed_symlinks.txt", exception);
            }
        }

        return new DirectoryValidator(Convertable.NO_SYMLINKS_ALLOWED);
    }

    public static Convertable createDefault(Path path) {
        DirectoryValidator directoryvalidator = parseValidator(path.resolve("allowed_symlinks.txt"));

        return new Convertable(path, path.resolve("../backups"), directoryvalidator, DataConverterRegistry.getDataFixer());
    }

    public static WorldDataConfiguration readDataConfig(Dynamic<?> dynamic) {
        DataResult<WorldDataConfiguration> dataresult = WorldDataConfiguration.CODEC.parse(dynamic); // CraftBukkit - decompile error
        Logger logger = Convertable.LOGGER;

        Objects.requireNonNull(logger);
        return (WorldDataConfiguration) dataresult.resultOrPartial(logger::error).orElse(WorldDataConfiguration.DEFAULT);
    }

    public static WorldLoader.d getPackConfig(Dynamic<?> dynamic, ResourcePackRepository resourcepackrepository, boolean flag) {
        return new WorldLoader.d(resourcepackrepository, readDataConfig(dynamic), flag, false);
    }

    public static LevelDataAndDimensions getLevelDataAndDimensions(Dynamic<?> dynamic, WorldDataConfiguration worlddataconfiguration, IRegistry<WorldDimension> iregistry, IRegistryCustom.Dimension iregistrycustom_dimension) {
        Dynamic<?> dynamic1 = RegistryOps.injectRegistryContext(dynamic, iregistrycustom_dimension);
        Dynamic<?> dynamic2 = dynamic1.get("WorldGenSettings").orElseEmptyMap();
        GeneratorSettings generatorsettings = (GeneratorSettings) GeneratorSettings.CODEC.parse(dynamic2).getOrThrow();
        WorldSettings worldsettings = WorldSettings.parse(dynamic1, worlddataconfiguration);
        WorldDimensions.b worlddimensions_b = generatorsettings.dimensions().bake(iregistry);
        Lifecycle lifecycle = worlddimensions_b.lifecycle().add(iregistrycustom_dimension.allRegistriesLifecycle());
        WorldDataServer worlddataserver = WorldDataServer.parse(dynamic1, worldsettings, worlddimensions_b.specialWorldProperty(), generatorsettings.options(), lifecycle);
        worlddataserver.pdc = ((Dynamic<NBTBase>) dynamic1).getElement("BukkitValues", null); // CraftBukkit - Add PDC to world

        return new LevelDataAndDimensions(worlddataserver, worlddimensions_b);
    }

    public String getName() {
        return "Anvil";
    }

    public Convertable.a findLevelCandidates() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
            throw new LevelStorageException(IChatBaseComponent.translatable("selectWorld.load_folder_access"));
        } else {
            try {
                Stream<Path> stream = Files.list(this.baseDir);

                Convertable.a convertable_a;

                try {
                    List<Convertable.b> list = stream.filter((path) -> {
                        return Files.isDirectory(path, new LinkOption[0]);
                    }).map(Convertable.b::new).filter((convertable_b) -> {
                        return Files.isRegularFile(convertable_b.dataFile(), new LinkOption[0]) || Files.isRegularFile(convertable_b.oldDataFile(), new LinkOption[0]);
                    }).toList();

                    convertable_a = new Convertable.a(list);
                } catch (Throwable throwable) {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (stream != null) {
                    stream.close();
                }

                return convertable_a;
            } catch (IOException ioexception) {
                throw new LevelStorageException(IChatBaseComponent.translatable("selectWorld.load_folder_access"));
            }
        }
    }

    public CompletableFuture<List<WorldInfo>> loadLevelSummaries(Convertable.a convertable_a) {
        List<CompletableFuture<WorldInfo>> list = new ArrayList(convertable_a.levels.size());
        Iterator iterator = convertable_a.levels.iterator();

        while (iterator.hasNext()) {
            Convertable.b convertable_b = (Convertable.b) iterator.next();

            list.add(CompletableFuture.supplyAsync(() -> {
                boolean flag;

                try {
                    flag = SessionLock.isLocked(convertable_b.path());
                } catch (Exception exception) {
                    Convertable.LOGGER.warn("Failed to read {} lock", convertable_b.path(), exception);
                    return null;
                }

                try {
                    return this.readLevelSummary(convertable_b, flag);
                } catch (OutOfMemoryError outofmemoryerror) {
                    MemoryReserve.release();
                    System.gc();
                    String s = "Ran out of memory trying to read summary of world folder \"" + convertable_b.directoryName() + "\"";

                    Convertable.LOGGER.error(LogUtils.FATAL_MARKER, s);
                    OutOfMemoryError outofmemoryerror1 = new OutOfMemoryError("Ran out of memory reading level data");

                    outofmemoryerror1.initCause(outofmemoryerror);
                    CrashReport crashreport = CrashReport.forThrowable(outofmemoryerror1, s);
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("World details");

                    crashreportsystemdetails.setDetail("Folder Name", (Object) convertable_b.directoryName());

                    try {
                        long i = Files.size(convertable_b.dataFile());

                        crashreportsystemdetails.setDetail("level.dat size", (Object) i);
                    } catch (IOException ioexception) {
                        crashreportsystemdetails.setDetailError("level.dat size", ioexception);
                    }

                    throw new ReportedException(crashreport);
                }
            }, SystemUtils.backgroundExecutor()));
        }

        return SystemUtils.sequenceFailFastAndCancel(list).thenApply((list1) -> {
            return list1.stream().filter(Objects::nonNull).sorted().toList();
        });
    }

    private int getStorageVersion() {
        return 19133;
    }

    static NBTTagCompound readLevelDataTagRaw(Path path) throws IOException {
        return NBTCompressedStreamTools.readCompressed(path, NBTReadLimiter.create(104857600L));
    }

    static Dynamic<?> readLevelDataTagFixed(Path path, DataFixer datafixer) throws IOException {
        NBTTagCompound nbttagcompound = readLevelDataTagRaw(path);
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Data");
        int i = GameProfileSerializer.getDataVersion(nbttagcompound1, -1);
        Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(datafixer, new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound1), i);

        dynamic = dynamic.update("Player", (dynamic1) -> {
            return DataFixTypes.PLAYER.updateToCurrentVersion(datafixer, dynamic1, i);
        });
        dynamic = dynamic.update("WorldGenSettings", (dynamic1) -> {
            return DataFixTypes.WORLD_GEN_SETTINGS.updateToCurrentVersion(datafixer, dynamic1, i);
        });
        return dynamic;
    }

    private WorldInfo readLevelSummary(Convertable.b convertable_b, boolean flag) {
        Path path = convertable_b.dataFile();

        if (Files.exists(path, new LinkOption[0])) {
            try {
                if (Files.isSymbolicLink(path)) {
                    List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateSymlink(path);

                    if (!list.isEmpty()) {
                        Convertable.LOGGER.warn("{}", ContentValidationException.getMessage(path, list));
                        return new WorldInfo.c(convertable_b.directoryName(), convertable_b.iconFile());
                    }
                }

                NBTBase nbtbase = readLightweightData(path);

                if (nbtbase instanceof NBTTagCompound) {
                    NBTTagCompound nbttagcompound = (NBTTagCompound) nbtbase;
                    NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Data");
                    int i = GameProfileSerializer.getDataVersion(nbttagcompound1, -1);
                    Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(this.fixerUpper, new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound1), i);

                    return this.makeLevelSummary(dynamic, convertable_b, flag);
                }

                Convertable.LOGGER.warn("Invalid root tag in {}", path);
            } catch (Exception exception) {
                Convertable.LOGGER.error("Exception reading {}", path, exception);
            }
        }

        return new WorldInfo.b(convertable_b.directoryName(), convertable_b.iconFile(), getFileModificationTime(convertable_b));
    }

    private static long getFileModificationTime(Convertable.b convertable_b) {
        Instant instant = getFileModificationTime(convertable_b.dataFile());

        if (instant == null) {
            instant = getFileModificationTime(convertable_b.oldDataFile());
        }

        return instant == null ? -1L : instant.toEpochMilli();
    }

    @Nullable
    static Instant getFileModificationTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException ioexception) {
            return null;
        }
    }

    WorldInfo makeLevelSummary(Dynamic<?> dynamic, Convertable.b convertable_b, boolean flag) {
        LevelVersion levelversion = LevelVersion.parse(dynamic);
        int i = levelversion.levelDataVersion();

        if (i != 19132 && i != 19133) {
            throw new NbtFormatException("Unknown data version: " + Integer.toHexString(i));
        } else {
            boolean flag1 = i != this.getStorageVersion();
            Path path = convertable_b.iconFile();
            WorldDataConfiguration worlddataconfiguration = readDataConfig(dynamic);
            WorldSettings worldsettings = WorldSettings.parse(dynamic, worlddataconfiguration);
            FeatureFlagSet featureflagset = parseFeatureFlagsFromSummary(dynamic);
            boolean flag2 = FeatureFlags.isExperimental(featureflagset);

            return new WorldInfo(worldsettings, levelversion, convertable_b.directoryName(), flag1, flag, flag2, path);
        }
    }

    private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<?> dynamic) {
        Set<MinecraftKey> set = (Set) dynamic.get("enabled_features").asStream().flatMap((dynamic1) -> {
            return dynamic1.asString().result().map(MinecraftKey::tryParse).stream();
        }).collect(Collectors.toSet());

        return FeatureFlags.REGISTRY.fromNames(set, (minecraftkey) -> {
        });
    }

    @Nullable
    private static NBTBase readLightweightData(Path path) throws IOException {
        SkipFields skipfields = new SkipFields(new FieldSelector[]{new FieldSelector("Data", NBTTagCompound.TYPE, "Player"), new FieldSelector("Data", NBTTagCompound.TYPE, "WorldGenSettings")});

        NBTCompressedStreamTools.parseCompressed(path, skipfields, NBTReadLimiter.create(104857600L));
        return skipfields.getResult();
    }

    public boolean isNewLevelIdAcceptable(String s) {
        try {
            Path path = this.getLevelPath(s);

            Files.createDirectory(path);
            Files.deleteIfExists(path);
            return true;
        } catch (IOException ioexception) {
            return false;
        }
    }

    public boolean levelExists(String s) {
        try {
            return Files.isDirectory(this.getLevelPath(s), new LinkOption[0]);
        } catch (InvalidPathException invalidpathexception) {
            return false;
        }
    }

    public Path getLevelPath(String s) {
        return this.baseDir.resolve(s);
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getBackupPath() {
        return this.backupDir;
    }

    public Convertable.ConversionSession validateAndCreateAccess(String s, ResourceKey<WorldDimension> dimensionType) throws IOException, ContentValidationException { // CraftBukkit
        Path path = this.getLevelPath(s);
        List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateDirectory(path, true);

        if (!list.isEmpty()) {
            throw new ContentValidationException(path, list);
        } else {
            return new Convertable.ConversionSession(s, path, dimensionType); // CraftBukkit
        }
    }

    public Convertable.ConversionSession createAccess(String s, ResourceKey<WorldDimension> dimensionType) throws IOException { // CraftBukkit
        Path path = this.getLevelPath(s);

        return new Convertable.ConversionSession(s, path, dimensionType); // CraftBukkit
    }

    public DirectoryValidator getWorldDirValidator() {
        return this.worldDirValidator;
    }

    // CraftBukkit start
    public static Path getStorageFolder(Path path, ResourceKey<WorldDimension> dimensionType) {
        if (dimensionType == WorldDimension.OVERWORLD) {
            return path;
        } else if (dimensionType == WorldDimension.NETHER) {
            return path.resolve("DIM-1");
        } else if (dimensionType == WorldDimension.END) {
            return path.resolve("DIM1");
        } else {
            return path.resolve("dimensions").resolve(dimensionType.location().getNamespace()).resolve(dimensionType.location().getPath());
        }
    }
    // CraftBukkit end

    public static record a(List<Convertable.b> levels) implements Iterable<Convertable.b> {

        public boolean isEmpty() {
            return this.levels.isEmpty();
        }

        public Iterator<Convertable.b> iterator() {
            return this.levels.iterator();
        }
    }

    public static record b(Path path) {

        public String directoryName() {
            return this.path.getFileName().toString();
        }

        public Path dataFile() {
            return this.resourcePath(SavedFile.LEVEL_DATA_FILE);
        }

        public Path oldDataFile() {
            return this.resourcePath(SavedFile.OLD_LEVEL_DATA_FILE);
        }

        public Path corruptedDataFile(LocalDateTime localdatetime) {
            Path path = this.path;
            String s = SavedFile.LEVEL_DATA_FILE.getId();

            return path.resolve(s + "_corrupted_" + localdatetime.format(Convertable.FORMATTER));
        }

        public Path rawDataFile(LocalDateTime localdatetime) {
            Path path = this.path;
            String s = SavedFile.LEVEL_DATA_FILE.getId();

            return path.resolve(s + "_raw_" + localdatetime.format(Convertable.FORMATTER));
        }

        public Path iconFile() {
            return this.resourcePath(SavedFile.ICON_FILE);
        }

        public Path lockFile() {
            return this.resourcePath(SavedFile.LOCK_FILE);
        }

        public Path resourcePath(SavedFile savedfile) {
            return this.path.resolve(savedfile.getId());
        }
    }

    public class ConversionSession implements AutoCloseable {

        final SessionLock lock;
        public final Convertable.b levelDirectory;
        private final String levelId;
        private final Map<SavedFile, Path> resources = Maps.newHashMap();
        // CraftBukkit start
        public final ResourceKey<WorldDimension> dimensionType;

        ConversionSession(final String s, final Path path, final ResourceKey<WorldDimension> dimensionType) throws IOException {
            this.dimensionType = dimensionType;
            // CraftBukkit end
            this.levelId = s;
            this.levelDirectory = new Convertable.b(path);
            this.lock = SessionLock.create(path);
        }

        public long estimateDiskSpace() {
            try {
                return Files.getFileStore(this.levelDirectory.path).getUsableSpace();
            } catch (Exception exception) {
                return Long.MAX_VALUE;
            }
        }

        public boolean checkForLowDiskSpace() {
            return this.estimateDiskSpace() < 67108864L;
        }

        public void safeClose() {
            try {
                this.close();
            } catch (IOException ioexception) {
                Convertable.LOGGER.warn("Failed to unlock access to level {}", this.getLevelId(), ioexception);
            }

        }

        public Convertable parent() {
            return Convertable.this;
        }

        public Convertable.b getLevelDirectory() {
            return this.levelDirectory;
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(SavedFile savedfile) {
            Map<SavedFile, Path> map = this.resources; // CraftBukkit - decompile error
            Convertable.b convertable_b = this.levelDirectory;

            Objects.requireNonNull(this.levelDirectory);
            return (Path) map.computeIfAbsent(savedfile, convertable_b::resourcePath);
        }

        public Path getDimensionPath(ResourceKey<World> resourcekey) {
            return getStorageFolder(this.levelDirectory.path(), this.dimensionType); // CraftBukkit
        }

        private void checkLock() {
            if (!this.lock.isValid()) {
                throw new IllegalStateException("Lock is no longer valid");
            }
        }

        public WorldNBTStorage createPlayerStorage() {
            this.checkLock();
            return new WorldNBTStorage(this, Convertable.this.fixerUpper);
        }

        public WorldInfo getSummary(Dynamic<?> dynamic) {
            this.checkLock();
            return Convertable.this.makeLevelSummary(dynamic, this.levelDirectory, false);
        }

        public Dynamic<?> getDataTag() throws IOException {
            return this.getDataTag(false);
        }

        public Dynamic<?> getDataTagFallback() throws IOException {
            return this.getDataTag(true);
        }

        private Dynamic<?> getDataTag(boolean flag) throws IOException {
            this.checkLock();
            return Convertable.readLevelDataTagFixed(flag ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile(), Convertable.this.fixerUpper);
        }

        public void saveDataTag(IRegistryCustom iregistrycustom, SaveData savedata) {
            this.saveDataTag(iregistrycustom, savedata, (NBTTagCompound) null);
        }

        public void saveDataTag(IRegistryCustom iregistrycustom, SaveData savedata, @Nullable NBTTagCompound nbttagcompound) {
            NBTTagCompound nbttagcompound1 = savedata.createTag(iregistrycustom, nbttagcompound);
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();

            nbttagcompound2.put("Data", nbttagcompound1);
            this.saveLevelData(nbttagcompound2);
        }

        private void saveLevelData(NBTTagCompound nbttagcompound) {
            Path path = this.levelDirectory.path();

            try {
                Path path1 = Files.createTempFile(path, "level", ".dat");

                NBTCompressedStreamTools.writeCompressed(nbttagcompound, path1);
                Path path2 = this.levelDirectory.oldDataFile();
                Path path3 = this.levelDirectory.dataFile();

                SystemUtils.safeReplaceFile(path3, path1, path2);
            } catch (Exception exception) {
                Convertable.LOGGER.error("Failed to save level {}", path, exception);
            }

        }

        public Optional<Path> getIconFile() {
            return !this.lock.isValid() ? Optional.empty() : Optional.of(this.levelDirectory.iconFile());
        }

        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path path = this.levelDirectory.lockFile();

            Convertable.LOGGER.info("Deleting level {}", this.levelId);
            int i = 1;

            while (i <= 5) {
                Convertable.LOGGER.info("Attempt {}...", i);

                try {
                    Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                        public FileVisitResult visitFile(Path path1, BasicFileAttributes basicfileattributes) throws IOException {
                            if (!path1.equals(path)) {
                                Convertable.LOGGER.debug("Deleting {}", path1);
                                Files.delete(path1);
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        public FileVisitResult postVisitDirectory(Path path1, @Nullable IOException ioexception) throws IOException {
                            if (ioexception != null) {
                                throw ioexception;
                            } else {
                                if (path1.equals(ConversionSession.this.levelDirectory.path())) {
                                    ConversionSession.this.lock.close();
                                    Files.deleteIfExists(path);
                                }

                                Files.delete(path1);
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    });
                    break;
                } catch (IOException ioexception) {
                    if (i >= 5) {
                        throw ioexception;
                    }

                    Convertable.LOGGER.warn("Failed to delete {}", this.levelDirectory.path(), ioexception);

                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException interruptedexception) {
                        ;
                    }

                    ++i;
                }
            }

        }

        public void renameLevel(String s) throws IOException {
            this.modifyLevelDataWithoutDatafix((nbttagcompound) -> {
                nbttagcompound.putString("LevelName", s.trim());
            });
        }

        public void renameAndDropPlayer(String s) throws IOException {
            this.modifyLevelDataWithoutDatafix((nbttagcompound) -> {
                nbttagcompound.putString("LevelName", s.trim());
                nbttagcompound.remove("Player");
            });
        }

        private void modifyLevelDataWithoutDatafix(Consumer<NBTTagCompound> consumer) throws IOException {
            this.checkLock();
            NBTTagCompound nbttagcompound = Convertable.readLevelDataTagRaw(this.levelDirectory.dataFile());

            consumer.accept(nbttagcompound.getCompound("Data"));
            this.saveLevelData(nbttagcompound);
        }

        public long makeWorldBackup() throws IOException {
            this.checkLock();
            String s = LocalDateTime.now().format(Convertable.FORMATTER);
            String s1 = s + "_" + this.levelId;
            Path path = Convertable.this.getBackupPath();

            try {
                FileUtils.createDirectoriesSafe(path);
            } catch (IOException ioexception) {
                throw new RuntimeException(ioexception);
            }

            Path path1 = path.resolve(FileUtils.findAvailableName(path, s1, ".zip"));
            final ZipOutputStream zipoutputstream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path1)));

            try {
                final Path path2 = Paths.get(this.levelId);

                Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path path3, BasicFileAttributes basicfileattributes) throws IOException {
                        if (path3.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            String s2 = path2.resolve(ConversionSession.this.levelDirectory.path().relativize(path3)).toString().replace('\\', '/');
                            ZipEntry zipentry = new ZipEntry(s2);

                            zipoutputstream.putNextEntry(zipentry);
                            com.google.common.io.Files.asByteSource(path3.toFile()).copyTo(zipoutputstream);
                            zipoutputstream.closeEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    }
                });
            } catch (Throwable throwable) {
                try {
                    zipoutputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            zipoutputstream.close();
            return Files.size(path1);
        }

        public boolean hasWorldData() {
            return Files.exists(this.levelDirectory.dataFile(), new LinkOption[0]) || Files.exists(this.levelDirectory.oldDataFile(), new LinkOption[0]);
        }

        public void close() throws IOException {
            this.lock.close();
        }

        public boolean restoreLevelDataFromOld() {
            return SystemUtils.safeReplaceOrMoveFile(this.levelDirectory.dataFile(), this.levelDirectory.oldDataFile(), this.levelDirectory.corruptedDataFile(LocalDateTime.now()), true);
        }

        @Nullable
        public Instant getFileModificationTime(boolean flag) {
            return Convertable.getFileModificationTime(flag ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile());
        }
    }
}
