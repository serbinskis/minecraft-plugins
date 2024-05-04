package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.ChunkCoordIntPair;
import org.apache.commons.io.FileUtils;

public class RecreatingChunkStorage extends IChunkLoader {

    private final IOWorker writeWorker;
    private final Path writeFolder;

    public RecreatingChunkStorage(RegionStorageInfo regionstorageinfo, Path path, RegionStorageInfo regionstorageinfo1, Path path1, DataFixer datafixer, boolean flag) {
        super(regionstorageinfo, path, datafixer, flag);
        this.writeFolder = path1;
        this.writeWorker = new IOWorker(regionstorageinfo1, path1, flag);
    }

    @Override
    public CompletableFuture<Void> write(ChunkCoordIntPair chunkcoordintpair, NBTTagCompound nbttagcompound) {
        this.handleLegacyStructureIndex(chunkcoordintpair);
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
