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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.ResourcePackFile;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.slf4j.Logger;

public class ResourcePackSourceFolder implements ResourcePackSource {

    static final Logger LOGGER = LogUtils.getLogger();
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
            discoverPacks(this.folder, this.validator, false, (path, resourcepackloader_c) -> {
                String s = nameFromPath(path);
                ResourcePackLoader resourcepackloader = ResourcePackLoader.readMetaAndCreate("file/" + s, IChatBaseComponent.literal(s), false, resourcepackloader_c, this.packType, ResourcePackLoader.Position.TOP, this.packSource);

                if (resourcepackloader != null) {
                    consumer.accept(resourcepackloader);
                }

            });
        } catch (IOException ioexception) {
            ResourcePackSourceFolder.LOGGER.warn("Failed to list packs in {}", this.folder, ioexception);
        }

    }

    public static void discoverPacks(Path path, DirectoryValidator directoryvalidator, boolean flag, BiConsumer<Path, ResourcePackLoader.c> biconsumer) throws IOException {
        ResourcePackSourceFolder.a resourcepacksourcefolder_a = new ResourcePackSourceFolder.a(directoryvalidator, flag);
        DirectoryStream directorystream = Files.newDirectoryStream(path);

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

        private final boolean isBuiltin;

        protected a(DirectoryValidator directoryvalidator, boolean flag) {
            super(directoryvalidator);
            this.isBuiltin = flag;
        }

        @Nullable
        @Override
        protected ResourcePackLoader.c createZipPack(Path path) {
            FileSystem filesystem = path.getFileSystem();

            if (filesystem != FileSystems.getDefault() && !(filesystem instanceof LinkFileSystem)) {
                ResourcePackSourceFolder.LOGGER.info("Can't open pack archive at {}", path);
                return null;
            } else {
                return new ResourcePackFile.a(path, this.isBuiltin);
            }
        }

        @Override
        protected ResourcePackLoader.c createDirectoryPack(Path path) {
            return new PathPackResources.a(path, this.isBuiltin);
        }
    }
}
