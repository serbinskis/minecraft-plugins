package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkCoordIntPair;
import org.apache.commons.io.FileUtils;

public class RecreatingSimpleRegionStorage extends SimpleRegionStorage {

    private final IOWorker writeWorker;
    private final Path writeFolder;

    public RecreatingSimpleRegionStorage(RegionStorageInfo regionstorageinfo, Path path, RegionStorageInfo regionstorageinfo1, Path path1, DataFixer datafixer, boolean flag, DataFixTypes datafixtypes) {
        super(regionstorageinfo, path, datafixer, flag, datafixtypes);
        this.writeFolder = path1;
        this.writeWorker = new IOWorker(regionstorageinfo1, path1, flag);
    }

    @Override
    public CompletableFuture<Void> write(ChunkCoordIntPair chunkcoordintpair, @Nullable NBTTagCompound nbttagcompound) {
        return this.writeWorker.store(chunkcoordintpair, nbttagcompound);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.writeWorker.close();
        if (this.writeFolder.toFile().exists()) {
            FileUtils.deleteDirectory(this.writeFolder.toFile());
        }

    }
}
