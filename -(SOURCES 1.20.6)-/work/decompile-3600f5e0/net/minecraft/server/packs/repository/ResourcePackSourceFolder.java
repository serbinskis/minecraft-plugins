package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.ResourcePackFile;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.slf4j.Logger;

public class ResourcePackSourceFolder implements ResourcePackSource {

    static final Logger LOGGER = LogUtils.getLogger();
    private static final PackSelectionConfig DISCOVERED_PACK_SELECTION_CONFIG = new PackSelectionConfig(false, ResourcePackLoader.Position.TOP, false);
    private final Path folder;
    private final EnumResourcePackType packType;
    private final PackSource packSource;
    private final DirectoryValidator validator;

    public ResourcePackSourceFolder(Path path, EnumResourcePackType enumresourcepacktype, PackSource packsource, DirectoryValidator directoryvalidator) {
        this.folder = path;
        this.packType = enumresourcepacktype;
        this.packSource = packsource;
        this.validator = directoryvalidator;
    }

    private static String nameFromPath(Path path) {
        return path.getFileName().toString();
    }

    @Override
    public void loadPacks(Consumer<ResourcePackLoader> consumer) {
        try {
            FileUtils.createDirectoriesSafe(this.folder);
            discoverPacks(this.folder, this.validator, (path, resourcepackloader_c) -> {
                PackLocationInfo packlocationinfo = this.createDiscoveredFilePackInfo(path);
                ResourcePackLoader resourcepackloader = ResourcePackLoader.readMetaAndCreate(packlocationinfo, resourcepackloader_c, this.packType, ResourcePackSourceFolder.DISCOVERED_PACK_SELECTION_CONFIG);

                if (resourcepackloader != null) {
                    consumer.accept(resourcepackloader);
                }

            });
        } catch (IOException ioexception) {
            ResourcePackSourceFolder.LOGGER.warn("Failed to list packs in {}", this.folder, ioexception);
        }

    }

    private PackLocationInfo createDiscoveredFilePackInfo(Path path) {
        String s = nameFromPath(path);

        return new PackLocationInfo("file/" + s, IChatBaseComponent.literal(s), this.packSource, Optional.empty());
    }

    public static void discoverPacks(Path path, DirectoryValidator directoryvalidator, BiConsumer<Path, ResourcePackLoader.c> biconsumer) throws IOException {
        ResourcePackSourceFolder.a resourcepacksourcefolder_a = new ResourcePackSourceFolder.a(directoryvalidator);
        DirectoryStream<Path> directorystream = Files.newDirectoryStream(path);

        try {
            Iterator iterator = directorystream.iterator();

            while (iterator.hasNext()) {
                Path path1 = (Path) iterator.next();

                try {
                    List<ForbiddenSymlinkInfo> list = new ArrayList();
                    ResourcePackLoader.c resourcepackloader_c = (ResourcePackLoader.c) resourcepacksourcefolder_a.detectPackResources(path1, list);

                    if (!list.isEmpty()) {
                        ResourcePackSourceFolder.LOGGER.warn("Ignoring potential pack entry: {}", ContentValidationException.getMessage(path1, list));
                    } else if (resourcepackloader_c != null) {
                        biconsumer.accept(path1, resourcepackloader_c);
                    } else {
                        ResourcePackSourceFolder.LOGGER.info("Found non-pack entry '{}', ignoring", path1);
                    }
                } catch (IOException ioexception) {
                    ResourcePackSourceFolder.LOGGER.warn("Failed to read properties of '{}', ignoring", path1, ioexception);
                }
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

    }

    private static class a extends PackDetector<ResourcePackLoader.c> {

        protected a(DirectoryValidator directoryvalidator) {
            super(directoryvalidator);
        }

        @Nullable
        @Override
        protected ResourcePackLoader.c createZipPack(Path path) {
            FileSystem filesystem = path.getFileSystem();

            if (filesystem != FileSystems.getDefault() && !(filesystem instanceof LinkFileSystem)) {
                ResourcePackSourceFolder.LOGGER.info("Can't open pack archive at {}", path);
                return null;
            } else {
                return new ResourcePackFile.a(path);
            }
        }

        @Override
        protected ResourcePackLoader.c createDirectoryPack(Path path) {
            return new PathPackResources.a(path);
        }
    }
}
