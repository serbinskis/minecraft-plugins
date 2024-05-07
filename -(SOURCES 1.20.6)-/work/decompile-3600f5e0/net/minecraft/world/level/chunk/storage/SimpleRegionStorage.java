package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkCoordIntPair;

public class SimpleRegionStorage implements AutoCloseable {

    private final IOWorker worker;
    private final DataFixer fixerUpper;
    private final DataFixTypes dataFixType;

    public SimpleRegionStorage(RegionStorageInfo regionstorageinfo, Path path, DataFixer datafixer, boolean flag, DataFixTypes datafixtypes) {
        this.fixerUpper = datafixer;
        this.dataFixType = datafixtypes;
        this.worker = new IOWorker(regionstorageinfo, path, flag);
    }

    public CompletableFuture<Optional<NBTTagCompound>> read(ChunkCoordIntPair chunkcoordintpair) {
        return this.worker.loadAsync(chunkcoordintpair);
    }

    public CompletableFuture<Void> write(ChunkCoordIntPair chunkcoordintpair, @Nullable NBTTagCompound nbttagcompound) {
        return this.worker.store(chunkcoordintpair, nbttagcompound);
    }

    public NBTTagCompound upgradeChunkTag(NBTTagCompound nbttagcompound, int i) {
        int j = GameProfileSerializer.getDataVersion(nbttagcompound, i);

        return this.dataFixType.updateToCurrentVersion(this.fixerUpper, nbttagcompound, j);
    }

    public Dynamic<NBTBase> upgradeChunkTag(Dynamic<NBTBase> dynamic, int i) {
        return this.dataFixType.updateToCurrentVersion(this.fixerUpper, dynamic, i);
    }

    public CompletableFuture<Void> synchronize(boolean flag) {
        return this.worker.synchronize(flag);
    }

    public void close() throws IOException {
        this.worker.close();
    }
}
