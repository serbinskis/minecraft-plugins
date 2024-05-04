package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.minecraft.FileUtils;

public interface CachedOutput {

    CachedOutput NO_CACHE = (path, abyte, hashcode) -> {
        FileUtils.createDirectoriesSafe(path.getParent());
        Files.write(path, abyte, new OpenOption[0]);
    };

    void writeIfNeeded(Path path, byte[] abyte, HashCode hashcode) throws IOException;
}
