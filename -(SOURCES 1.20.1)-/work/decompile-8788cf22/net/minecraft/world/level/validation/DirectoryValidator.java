package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {

    private final PathAllowList symlinkTargetAllowList;

    public DirectoryValidator(PathAllowList pathallowlist) {
        this.symlinkTargetAllowList = pathallowlist;
    }

    public void validateSymlink(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
        Path path1 = Files.readSymbolicLink(path);

        if (!this.symlinkTargetAllowList.matches(path1)) {
            list.add(new ForbiddenSymlinkInfo(path, path1));
        }

    }

    public List<ForbiddenSymlinkInfo> validateSave(Path path, boolean flag) throws IOException {
        final ArrayList arraylist = new ArrayList();

        BasicFileAttributes basicfileattributes;

        try {
            basicfileattributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException nosuchfileexception) {
            return arraylist;
        }

        if (!basicfileattributes.isRegularFile() && !basicfileattributes.isOther()) {
            if (basicfileattributes.isSymbolicLink()) {
                if (!flag) {
                    this.validateSymlink(path, arraylist);
                    return arraylist;
                }

                path = Files.readSymbolicLink(path);
            }

            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                private void validateSymlink(Path path1, BasicFileAttributes basicfileattributes1) throws IOException {
                    if (basicfileattributes1.isSymbolicLink()) {
                        DirectoryValidator.this.validateSymlink(path1, arraylist);
                    }

                }

                public FileVisitResult preVisitDirectory(Path path1, BasicFileAttributes basicfileattributes1) throws IOException {
                    this.validateSymlink(path1, basicfileattributes1);
                    return super.preVisitDirectory(path1, basicfileattributes1);
                }

                public FileVisitResult visitFile(Path path1, BasicFileAttributes basicfileattributes1) throws IOException {
                    this.validateSymlink(path1, basicfileattributes1);
                    return super.visitFile(path1, basicfileattributes1);
                }
            });
            return arraylist;
        } else {
            throw new IOException("Path " + path + " is not a directory");
        }
    }
}
