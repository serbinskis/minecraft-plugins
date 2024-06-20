package net.minecraft.world.level.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ContentValidationException extends Exception {

    private final Path directory;
    private final List<ForbiddenSymlinkInfo> entries;

    public ContentValidationException(Path path, List<ForbiddenSymlinkInfo> list) {
        this.directory = path;
        this.entries = list;
    }

    public String getMessage() {
        return getMessage(this.directory, this.entries);
    }

    public static String getMessage(Path path, List<ForbiddenSymlinkInfo> list) {
        String s = String.valueOf(path);

        return "Failed to validate '" + s + "'. Found forbidden symlinks: " + (String) list.stream().map((forbiddensymlinkinfo) -> {
            String s1 = String.valueOf(forbiddensymlinkinfo.link());

            return s1 + "->" + String.valueOf(forbiddensymlinkinfo.target());
        }).collect(Collectors.joining(", "));
    }
}
