package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.FileUtils;
import net.minecraft.ResourceKeyInvalidException;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.gametest.framework.GameTestHarnessStructures;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.SavedFile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String STRUCTURE_RESOURCE_DIRECTORY_NAME = "structure";
    private static final String STRUCTURE_GENERATED_DIRECTORY_NAME = "structures";
    private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
    private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
    public final Map<MinecraftKey, Optional<DefinedStructure>> structureRepository = Maps.newConcurrentMap();
    private final DataFixer fixerUpper;
    private IResourceManager resourceManager;
    private final Path generatedDir;
    private final List<StructureTemplateManager.b> sources;
    private final HolderGetter<Block> blockLookup;
    private static final FileToIdConverter RESOURCE_LISTER = new FileToIdConverter("structure", ".nbt");

    public StructureTemplateManager(IResourceManager iresourcemanager, Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer, HolderGetter<Block> holdergetter) {
        this.resourceManager = iresourcemanager;
        this.fixerUpper = datafixer;
        this.generatedDir = convertable_conversionsession.getLevelPath(SavedFile.GENERATED_DIR).normalize();
        this.blockLookup = holdergetter;
        Builder<StructureTemplateManager.b> builder = ImmutableList.builder();

        builder.add(new StructureTemplateManager.b(this::loadFromGenerated, this::listGenerated));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            builder.add(new StructureTemplateManager.b(this::loadFromTestStructures, this::listTestStructures));
        }

        builder.add(new StructureTemplateManager.b(this::loadFromResource, this::listResources));
        this.sources = builder.build();
    }

    public DefinedStructure getOrCreate(MinecraftKey minecraftkey) {
        Optional<DefinedStructure> optional = this.get(minecraftkey);

        if (optional.isPresent()) {
            return (DefinedStructure) optional.get();
        } else {
            DefinedStructure definedstructure = new DefinedStructure();

            this.structureRepository.put(minecraftkey, Optional.of(definedstructure));
            return definedstructure;
        }
    }

    public Optional<DefinedStructure> get(MinecraftKey minecraftkey) {
        return (Optional) this.structureRepository.computeIfAbsent(minecraftkey, this::tryLoad);
    }

    public Stream<MinecraftKey> listTemplates() {
        return this.sources.stream().flatMap((structuretemplatemanager_b) -> {
            return (Stream) structuretemplatemanager_b.lister().get();
        }).distinct();
    }

    private Optional<DefinedStructure> tryLoad(MinecraftKey minecraftkey) {
        Iterator iterator = this.sources.iterator();

        while (iterator.hasNext()) {
            StructureTemplateManager.b structuretemplatemanager_b = (StructureTemplateManager.b) iterator.next();

            try {
                Optional<DefinedStructure> optional = (Optional) structuretemplatemanager_b.loader().apply(minecraftkey);

                if (optional.isPresent()) {
                    return optional;
                }
            } catch (Exception exception) {
                ;
            }
        }

        return Optional.empty();
    }

    public void onResourceManagerReload(IResourceManager iresourcemanager) {
        this.resourceManager = iresourcemanager;
        this.structureRepository.clear();
    }

    public Optional<DefinedStructure> loadFromResource(MinecraftKey minecraftkey) {
        MinecraftKey minecraftkey1 = StructureTemplateManager.RESOURCE_LISTER.idToFile(minecraftkey);

        return this.load(() -> {
            return this.resourceManager.open(minecraftkey1);
        }, (throwable) -> {
            StructureTemplateManager.LOGGER.error("Couldn't load structure {}", minecraftkey, throwable);
        });
    }

    private Stream<MinecraftKey> listResources() {
        Stream stream = StructureTemplateManager.RESOURCE_LISTER.listMatchingResources(this.resourceManager).keySet().stream();
        FileToIdConverter filetoidconverter = StructureTemplateManager.RESOURCE_LISTER;

        Objects.requireNonNull(filetoidconverter);
        return stream.map(filetoidconverter::fileToId);
    }

    private Optional<DefinedStructure> loadFromTestStructures(MinecraftKey minecraftkey) {
        return this.loadFromSnbt(minecraftkey, Paths.get(GameTestHarnessStructures.testStructuresDir));
    }

    private Stream<MinecraftKey> listTestStructures() {
        Path path = Paths.get(GameTestHarnessStructures.testStructuresDir);

        if (!Files.isDirectory(path, new LinkOption[0])) {
            return Stream.empty();
        } else {
            List<MinecraftKey> list = new ArrayList();

            Objects.requireNonNull(list);
            this.listFolderContents(path, "minecraft", ".snbt", list::add);
            return list.stream();
        }
    }

    public Optional<DefinedStructure> loadFromGenerated(MinecraftKey minecraftkey) {
        if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
            return Optional.empty();
        } else {
            Path path = this.createAndValidatePathToGeneratedStructure(minecraftkey, ".nbt");

            return this.load(() -> {
                return new FileInputStream(path.toFile());
            }, (throwable) -> {
                StructureTemplateManager.LOGGER.error("Couldn't load structure from {}", path, throwable);
            });
        }
    }

    private Stream<MinecraftKey> listGenerated() {
        if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
            return Stream.empty();
        } else {
            try {
                List<MinecraftKey> list = new ArrayList();
                DirectoryStream<Path> directorystream = Files.newDirectoryStream(this.generatedDir, (path) -> {
                    return Files.isDirectory(path, new LinkOption[0]);
                });

                try {
                    Iterator iterator = directorystream.iterator();

                    while (iterator.hasNext()) {
                        Path path = (Path) iterator.next();
                        String s = path.getFileName().toString();
                        Path path1 = path.resolve("structures");

                        Objects.requireNonNull(list);
                        this.listFolderContents(path1, s, ".nbt", list::add);
                    }
                } catch (Throwable throwable) {
                    if (directorystream != null) {
                        try {
                            directorystream.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (directorystream != null) {
                    directorystream.close();
                }

                return list.stream();
            } catch (IOException ioexception) {
                return Stream.empty();
            }
        }
    }

    private void listFolderContents(Path path, String s, String s1, Consumer<MinecraftKey> consumer) {
        int i = s1.length();
        Function<String, String> function = (s2) -> {
            return s2.substring(0, s2.length() - i);
        };

        try {
            Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (path1, basicfileattributes) -> {
                return basicfileattributes.isRegularFile() && path1.toString().endsWith(s1);
            }, new FileVisitOption[0]);

            try {
                stream.forEach((path1) -> {
                    try {
                        consumer.accept(MinecraftKey.fromNamespaceAndPath(s, (String) function.apply(this.relativize(path, path1))));
                    } catch (ResourceKeyInvalidException resourcekeyinvalidexception) {
                        StructureTemplateManager.LOGGER.error("Invalid location while listing folder {} contents", path, resourcekeyinvalidexception);
                    }

                });
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
        } catch (IOException ioexception) {
            StructureTemplateManager.LOGGER.error("Failed to list folder {} contents", path, ioexception);
        }

    }

    private String relativize(Path path, Path path1) {
        return path.relativize(path1).toString().replace(File.separator, "/");
    }

    private Optional<DefinedStructure> loadFromSnbt(MinecraftKey minecraftkey, Path path) {
        if (!Files.isDirectory(path, new LinkOption[0])) {
            return Optional.empty();
        } else {
            Path path1 = FileUtils.createPathToResource(path, minecraftkey.getPath(), ".snbt");

            try {
                BufferedReader bufferedreader = Files.newBufferedReader(path1);

                Optional optional;

                try {
                    String s = IOUtils.toString(bufferedreader);

                    optional = Optional.of(this.readStructure(GameProfileSerializer.snbtToStructure(s)));
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

                return optional;
            } catch (NoSuchFileException nosuchfileexception) {
                return Optional.empty();
            } catch (CommandSyntaxException | IOException ioexception) {
                StructureTemplateManager.LOGGER.error("Couldn't load structure from {}", path1, ioexception);
                return Optional.empty();
            }
        }
    }

    private Optional<DefinedStructure> load(StructureTemplateManager.a structuretemplatemanager_a, Consumer<Throwable> consumer) {
        try {
            InputStream inputstream = structuretemplatemanager_a.open();

            Optional optional;

            try {
                FastBufferedInputStream fastbufferedinputstream = new FastBufferedInputStream(inputstream);

                try {
                    optional = Optional.of(this.readStructure((InputStream) fastbufferedinputstream));
                } catch (Throwable throwable) {
                    try {
                        fastbufferedinputstream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }

                    throw throwable;
                }

                fastbufferedinputstream.close();
            } catch (Throwable throwable2) {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (Throwable throwable3) {
                        throwable2.addSuppressed(throwable3);
                    }
                }

                throw throwable2;
            }

            if (inputstream != null) {
                inputstream.close();
            }

            return optional;
        } catch (FileNotFoundException filenotfoundexception) {
            return Optional.empty();
        } catch (Throwable throwable4) {
            consumer.accept(throwable4);
            return Optional.empty();
        }
    }

    public DefinedStructure readStructure(InputStream inputstream) throws IOException {
        NBTTagCompound nbttagcompound = NBTCompressedStreamTools.readCompressed(inputstream, NBTReadLimiter.unlimitedHeap());

        return this.readStructure(nbttagcompound);
    }

    public DefinedStructure readStructure(NBTTagCompound nbttagcompound) {
        DefinedStructure definedstructure = new DefinedStructure();
        int i = GameProfileSerializer.getDataVersion(nbttagcompound, 500);

        definedstructure.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, nbttagcompound, i));
        return definedstructure;
    }

    public boolean save(MinecraftKey minecraftkey) {
        Optional<DefinedStructure> optional = (Optional) this.structureRepository.get(minecraftkey);

        if (optional.isEmpty()) {
            return false;
        } else {
            DefinedStructure definedstructure = (DefinedStructure) optional.get();
            Path path = this.createAndValidatePathToGeneratedStructure(minecraftkey, ".nbt");
            Path path1 = path.getParent();

            if (path1 == null) {
                return false;
            } else {
                try {
                    Files.createDirectories(Files.exists(path1, new LinkOption[0]) ? path1.toRealPath() : path1);
                } catch (IOException ioexception) {
                    StructureTemplateManager.LOGGER.error("Failed to create parent directory: {}", path1);
                    return false;
                }

                NBTTagCompound nbttagcompound = definedstructure.save(new NBTTagCompound());

                try {
                    FileOutputStream fileoutputstream = new FileOutputStream(path.toFile());

                    try {
                        NBTCompressedStreamTools.writeCompressed(nbttagcompound, (OutputStream) fileoutputstream);
                    } catch (Throwable throwable) {
                        try {
                            fileoutputstream.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }

                        throw throwable;
                    }

                    fileoutputstream.close();
                    return true;
                } catch (Throwable throwable2) {
                    return false;
                }
            }
        }
    }

    public Path createAndValidatePathToGeneratedStructure(MinecraftKey minecraftkey, String s) {
        if (minecraftkey.getPath().contains("//")) {
            throw new ResourceKeyInvalidException("Invalid resource path: " + String.valueOf(minecraftkey));
        } else {
            try {
                Path path = this.generatedDir.resolve(minecraftkey.getNamespace());
                Path path1 = path.resolve("structures");
                Path path2 = FileUtils.createPathToResource(path1, minecraftkey.getPath(), s);

                if (path2.startsWith(this.generatedDir) && FileUtils.isPathNormalized(path2) && FileUtils.isPathPortable(path2)) {
                    return path2;
                } else {
                    throw new ResourceKeyInvalidException("Invalid resource path: " + String.valueOf(path2));
                }
            } catch (InvalidPathException invalidpathexception) {
                throw new ResourceKeyInvalidException("Invalid resource path: " + String.valueOf(minecraftkey), invalidpathexception);
            }
        }
    }

    public void remove(MinecraftKey minecraftkey) {
        this.structureRepository.remove(minecraftkey);
    }

    private static record b(Function<MinecraftKey, Optional<DefinedStructure>> loader, Supplier<Stream<MinecraftKey>> lister) {

    }

    @FunctionalInterface
    private interface a {

        InputStream open() throws IOException;
    }
}
