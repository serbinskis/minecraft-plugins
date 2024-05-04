package net.minecraft.server.packs.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;

public abstract class PackDetector<T> {

    private final DirectoryValidator validator;

    protected PackDetector(DirectoryValidator directoryvalidator) {
        this.validator = directoryvalidator;
    }

    @Nullable
    public T detectPackResources(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
        Path path1 = path;

        BasicFileAttributes basicfileattributes;

        try {
            basicfileattributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException nosuchfileexception) {
            return null;
        }

        if (basicfileattributes.isSymbolicLink()) {
            this.validator.validateSymlink(path, list);
            if (!list.isEmpty()) {
                return null;
            }

            path1 = Files.readSymbolicLink(path);
            basicfileattributes = Files.readAttributes(path1, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }

        if (basicfileattributes.isDirectory()) {
            this.validator.validateKnownDirectory(path1, list);
            return !list.isEmpty() ? null : (!Files.isRegularFile(path1.resolve("pack.mcmeta"), new LinkOption[0]) ? null : this.createDirectoryPack(path1));
        } else {
            return basicfileattributes.isRegularFile() && path1.getFileName().toString().endsWith(".zip") ? this.createZipPack(path1) : null;
        }
    }

    @Nullable
    protected abstract T createZipPack(Path path) throws IOException;

    @Nullable
    protected abstract T createDirectoryPack(Path path) throws IOException;
}
