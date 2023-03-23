package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.PersistentStructureLegacy;
import net.minecraft.world.level.storage.WorldPersistentData;

// CraftBukkit start
import java.util.concurrent.ExecutionException;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.WorldDimension;
// CraftBukkit end

public class IChunkLoader implements AutoCloseable {

    public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
    private final IOWorker worker;
    protected final DataFixer fixerUpper;
    @Nullable
    private volatile PersistentStructureLegacy legacyStructureHandler;

    public IChunkLoader(Path path, DataFixer datafixer, boolean flag) {
        this.fixerUpper = datafixer;
        this.worker = new IOWorker(path, flag, "chunk");
    }

    public boolean isOldChunkAround(ChunkCoordIntPair chunkcoordintpair, int i) {
        return this.worker.isOldChunkAround(chunkcoordintpair, i);
    }

    // CraftBukkit start
    private boolean check(ChunkProviderServer cps, int x, int z) {
        ChunkCoordIntPair pos = new ChunkCoordIntPair(x, z);
        if (cps != null) {
            com.google.common.base.Preconditions.checkState(org.bukkit.Bukkit.isPrimaryThread(), "primary thread");
            if (cps.hasChunk(x, z)) {
                return true;
            }
        }

        NBTTagCompound nbt;
        try {
            nbt = read(pos).get().orElse(null);
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        if (nbt != null) {
            NBTTagCompound level = nbt.getCompound("Level");
            if (level.getBoolean("TerrainPopulated")) {
                return true;
            }

            ChunkStatus status = ChunkStatus.byName(level.getString("Status"));
            if (status != null && status.isOrAfter(ChunkStatus.FEATURES)) {
                return true;
            }
        }

        return false;
    }

    public NBTTagCompound upgradeChunkTag(ResourceKey<WorldDimension> resourcekey, Supplier<WorldPersistentData> supplier, NBTTagCompound nbttagcompound, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> optional, ChunkCoordIntPair pos, @Nullable GeneratorAccess generatoraccess) {
        // CraftBukkit end
        int i = getVersion(nbttagcompound);

        // CraftBukkit start
        if (i < 1466) {
            NBTTagCompound level = nbttagcompound.getCompound("Level");
            if (level.getBoolean("TerrainPopulated") && !level.getBoolean("LightPopulated")) {
                ChunkProviderServer cps = (generatoraccess == null) ? null : ((WorldServer) generatoraccess).getChunkSource();
                if (check(cps, pos.x - 1, pos.z) && check(cps, pos.x - 1, pos.z - 1) && check(cps, pos.x, pos.z - 1)) {
                    level.putBoolean("LightPopulated", true);
                }
            }
        }
        // CraftBukkit end

        if (i < 1493) {
            nbttagcompound = DataFixTypes.CHUNK.update(this.fixerUpper, nbttagcompound, i, 1493);
            if (nbttagcompound.getCompound("Level").getBoolean("hasLegacyStructureData")) {
                PersistentStructureLegacy persistentstructurelegacy = this.getLegacyStructureHandler(resourcekey, supplier);

                nbttagcompound = persistentstructurelegacy.updateFromLegacy(nbttagcompound);
            }
        }

        injectDatafixingContext(nbttagcompound, resourcekey, optional);
        nbttagcompound = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, nbttagcompound, Math.max(1493, i));
        if (i < SharedConstants.getCurrentVersion().getDataVersion().getVersion()) {
            GameProfileSerializer.addCurrentDataVersion(nbttagcompound);
        }

        nbttagcompound.remove("__context");
        return nbttagcompound;
    }

    private PersistentStructureLegacy getLegacyStructureHandler(ResourceKey<WorldDimension> resourcekey, Supplier<WorldPersistentData> supplier) { // CraftBukkit
        PersistentStructureLegacy persistentstructurelegacy = this.legacyStructureHandler;

        if (persistentstructurelegacy == null) {
            synchronized (this) {
                persistentstructurelegacy = this.legacyStructureHandler;
                if (persistentstructurelegacy == null) {
                    this.legacyStructureHandler = persistentstructurelegacy = PersistentStructureLegacy.getLegacyStructureHandler(resourcekey, (WorldPersistentData) supplier.get());
                }
            }
        }

        return persistentstructurelegacy;
    }

    public static void injectDatafixingContext(NBTTagCompound nbttagcompound, ResourceKey<WorldDimension> resourcekey, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> optional) { // CraftBukkit
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();

        nbttagcompound1.putString("dimension", resourcekey.location().toString());
        optional.ifPresent((resourcekey1) -> {
            nbttagcompound1.putString("generator", resourcekey1.location().toString());
        });
        nbttagcompound.put("__context", nbttagcompound1);
    }

    public static int getVersion(NBTTagCompound nbttagcompound) {
        return GameProfileSerializer.getDataVersion(nbttagcompound, -1);
    }

    public CompletableFuture<Optional<NBTTagCompound>> read(ChunkCoordIntPair chunkcoordintpair) {
        return this.worker.loadAsync(chunkcoordintpair);
    }

    public void write(ChunkCoordIntPair chunkcoordintpair, NBTTagCompound nbttagcompound) {
        this.worker.store(chunkcoordintpair, nbttagcompound);
        if (this.legacyStructureHandler != null) {
            this.legacyStructureHandler.removeIndex(chunkcoordintpair.toLong());
        }

    }

    public void flushWorker() {
        this.worker.synchronize(true).join();
    }

    public void close() throws IOException {
        this.worker.close();
    }

    public ChunkScanAccess chunkScanner() {
        return this.worker;
    }
}
