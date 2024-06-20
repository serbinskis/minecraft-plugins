package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.EntityHuman;
import org.slf4j.Logger;

public class WorldNBTStorage {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;
    private static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();

    public WorldNBTStorage(Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer) {
        this.fixerUpper = datafixer;
        this.playerDir = convertable_conversionsession.getLevelPath(SavedFile.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(EntityHuman entityhuman) {
        try {
            NBTTagCompound nbttagcompound = entityhuman.saveWithoutId(new NBTTagCompound());
            Path path = this.playerDir.toPath();
            Path path1 = Files.createTempFile(path, entityhuman.getStringUUID() + "-", ".dat");

            NBTCompressedStreamTools.writeCompressed(nbttagcompound, path1);
            Path path2 = path.resolve(entityhuman.getStringUUID() + ".dat");
            Path path3 = path.resolve(entityhuman.getStringUUID() + ".dat_old");

            SystemUtils.safeReplaceFile(path2, path1, path3);
        } catch (Exception exception) {
            WorldNBTStorage.LOGGER.warn("Failed to save player data for {}", entityhuman.getName().getString());
        }

    }

    private void backup(EntityHuman entityhuman, String s) {
        Path path = this.playerDir.toPath();
        String s1 = entityhuman.getStringUUID();
        Path path1 = path.resolve(s1 + s);

        s1 = entityhuman.getStringUUID();
        Path path2 = path.resolve(s1 + "_corrupted_" + LocalDateTime.now().format(WorldNBTStorage.FORMATTER) + s);

        if (Files.isRegularFile(path1, new LinkOption[0])) {
            try {
                Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception exception) {
                WorldNBTStorage.LOGGER.warn("Failed to copy the player.dat file for {}", entityhuman.getName().getString(), exception);
            }

        }
    }

    private Optional<NBTTagCompound> load(EntityHuman entityhuman, String s) {
        File file = this.playerDir;
        String s1 = entityhuman.getStringUUID();
        File file1 = new File(file, s1 + s);

        if (file1.exists() && file1.isFile()) {
            try {
                return Optional.of(NBTCompressedStreamTools.readCompressed(file1.toPath(), NBTReadLimiter.unlimitedHeap()));
            } catch (Exception exception) {
                WorldNBTStorage.LOGGER.warn("Failed to load player data for {}", entityhuman.getName().getString());
            }
        }

        return Optional.empty();
    }

    public Optional<NBTTagCompound> load(EntityHuman entityhuman) {
        Optional<NBTTagCompound> optional = this.load(entityhuman, ".dat");

        if (optional.isEmpty()) {
            this.backup(entityhuman, ".dat");
        }

        return optional.or(() -> {
            return this.load(entityhuman, ".dat_old");
        }).map((nbttagcompound) -> {
            int i = GameProfileSerializer.getDataVersion(nbttagcompound, -1);

            nbttagcompound = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, nbttagcompound, i);
            entityhuman.load(nbttagcompound);
            return nbttagcompound;
        });
    }
}
