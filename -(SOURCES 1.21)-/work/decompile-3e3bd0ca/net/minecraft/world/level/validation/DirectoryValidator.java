package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {

    private final PathMatcher symlinkTargetAllowList;

    public DirectoryValidator(PathMatcher pathmatcher) {
        this.symlinkTargetAllowList = pathmatcher;
    }

    public void validateSymlink(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
        Path path1 = Files.readSymbolicLink(path);

        if (!this.symlinkTargetAllowList.matches(path1)) {
            list.add(new ForbiddenSymlinkInfo(path, path1));
        }

    }

    public List<ForbiddenSymlinkInfo> validateSymlink(Path path) throws IOException {
        List<ForbiddenSymlinkInfo> list = new ArrayList();

        this.validateSymlink(path, list);
        return list;
    }

    public List<ForbiddenSymlinkInfo> validateDirectory(Path path, boolean flag) throws IOException {
        List<ForbiddenSymlinkInfo> list = new ArrayList();

        BasicFileAttributes basicfileattributes;

        try {
            basicfileattributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException nosuchfileexception) {
            return list;
        }

        if (basicfileattributes.isRegularFile()) {
            throw new IOException("Path " + String.valueOf(path) + " is not a directory");
        } else {
            if (basicfileattributes.isSymbolicLink()) {
                if (!flag) {
                    this.validateSymlink(path, list);
                    return list;
                }

                path = Files.readSymbolicLink(path);
            }

            this.validateKnownDirectory(path, list);
            return list;
        }
    }

    public void validateKnownDirectory(Path path, final List<ForbiddenSymlinkInfo> list) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            private void validateSymlink(Path path1, BasicFileAttributes basicfileattributes) throws IOException {
                if (basicfileattributes.isSymbolicLink()) {
                    DirectoryValidator.this.validateSymlink(path1, list);
                }

            }

            public FileVisitResult preVisitDirectory(Path path1, BasicFileAttributes basicfileattributes) throws IOException {
                this.validateSymlink(path1, basicfileattributes);
                return super.preVisitDirectory(path1, basicfileattributes);
            }

            public FileVisitResult visitFile(Path path1, BasicFileAttributes basicfileattributes) throws IOException {
                this.validateSymlink(path1, basicfileattributes);
                return super.visitFile(path1, basicfileattributes);
            }
        });
    }
}
