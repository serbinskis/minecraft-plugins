package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import org.slf4j.Logger;

public class RegionFileSection<R> implements AutoCloseable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SECTIONS_TAG = "Sections";
    private final SimpleRegionStorage simpleRegionStorage;
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap();
    private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
    private final Function<Runnable, Codec<R>> codec;
    private final Function<Runnable, R> factory;
    private final IRegistryCustom registryAccess;
    private final ChunkIOErrorReporter errorReporter;
    protected final LevelHeightAccessor levelHeightAccessor;

    public RegionFileSection(SimpleRegionStorage simpleregionstorage, Function<Runnable, Codec<R>> function, Function<Runnable, R> function1, IRegistryCustom iregistrycustom, ChunkIOErrorReporter chunkioerrorreporter, LevelHeightAccessor levelheightaccessor) {
        this.simpleRegionStorage = simpleregionstorage;
        this.codec = function;
        this.factory = function1;
        this.registryAccess = iregistrycustom;
        this.errorReporter = chunkioerrorreporter;
        this.levelHeightAccessor = levelheightaccessor;
    }

    protected void tick(BooleanSupplier booleansupplier) {
        while (this.hasWork() && booleansupplier.getAsBoolean()) {
            ChunkCoordIntPair chunkcoordintpair = SectionPosition.of(this.dirty.firstLong()).chunk();

            this.writeColumn(chunkcoordintpair);
        }

    }

    public boolean hasWork() {
        return !this.dirty.isEmpty();
    }

    @Nullable
    protected Optional<R> get(long i) {
        return (Optional) this.storage.get(i);
    }

    protected Optional<R> getOrLoad(long i) {
        if (this.outsideStoredRange(i)) {
            return Optional.empty();
        } else {
            Optional<R> optional = this.get(i);

            if (optional != null) {
                return optional;
            } else {
                this.readColumn(SectionPosition.of(i).chunk());
                optional = this.get(i);
                if (optional == null) {
                    throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException());
                } else {
                    return optional;
                }
            }
        }
    }

    protected boolean outsideStoredRange(long i) {
        int j = SectionPosition.sectionToBlockCoord(SectionPosition.y(i));

        return this.levelHeightAccessor.isOutsideBuildHeight(j);
    }

    protected R getOrCreate(long i) {
        if (this.outsideStoredRange(i)) {
            throw (IllegalArgumentException) SystemUtils.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
        } else {
            Optional<R> optional = this.getOrLoad(i);

            if (optional.isPresent()) {
                return optional.get();
            } else {
                R r0 = this.factory.apply(() -> {
                    this.setDirty(i);
                });

                this.storage.put(i, Optional.of(r0));
                return r0;
            }
        }
    }

    private void readColumn(ChunkCoordIntPair chunkcoordintpair) {
        Optional<NBTTagCompound> optional = (Optional) this.tryRead(chunkcoordintpair).join();
        RegistryOps<NBTBase> registryops = this.registryAccess.createSerializationContext(DynamicOpsNBT.INSTANCE);

        this.readColumn(chunkcoordintpair, registryops, (NBTTagCompound) optional.orElse((Object) null));
    }

    private CompletableFuture<Optional<NBTTagCompound>> tryRead(ChunkCoordIntPair chunkcoordintpair) {
        return this.simpleRegionStorage.read(chunkcoordintpair).exceptionally((throwable) -> {
            if (throwable instanceof IOException ioexception) {
                RegionFileSection.LOGGER.error("Error reading chunk {} data from disk", chunkcoordintpair, ioexception);
                this.errorReporter.reportChunkLoadFailure(ioexception, this.simpleRegionStorage.storageInfo(), chunkcoordintpair);
                return Optional.empty();
            } else {
                throw new CompletionException(throwable);
            }
        });
    }

    private void readColumn(ChunkCoordIntPair chunkcoordintpair, RegistryOps<NBTBase> registryops, @Nullable NBTTagCompound nbttagcompound) {
        if (nbttagcompound == null) {
            for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                this.storage.put(getKey(chunkcoordintpair, i), Optional.empty());
            }
        } else {
            Dynamic<NBTBase> dynamic = new Dynamic(registryops, nbttagcompound);
            int j = getVersion(dynamic);
            int k = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            boolean flag = j != k;
            Dynamic<NBTBase> dynamic1 = this.simpleRegionStorage.upgradeChunkTag(dynamic, j);
            OptionalDynamic<NBTBase> optionaldynamic = dynamic1.get("Sections");

            for (int l = this.levelHeightAccessor.getMinSection(); l < this.levelHeightAccessor.getMaxSection(); ++l) {
                long i1 = getKey(chunkcoordintpair, l);
                Optional<R> optional = optionaldynamic.get(Integer.toString(l)).result().flatMap((dynamic2) -> {
                    DataResult dataresult = ((Codec) this.codec.apply(() -> {
                        this.setDirty(i1);
                    })).parse(dynamic2);
                    Logger logger = RegionFileSection.LOGGER;

                    Objects.requireNonNull(logger);
                    return dataresult.resultOrPartial(logger::error);
                });

                this.storage.put(i1, optional);
                optional.ifPresent((object) -> {
                    this.onSectionLoad(i1);
                    if (flag) {
                        this.setDirty(i1);
                    }

                });
            }
        }

    }

    private void writeColumn(ChunkCoordIntPair chunkcoordintpair) {
        RegistryOps<NBTBase> registryops = this.registryAccess.createSerializationContext(DynamicOpsNBT.INSTANCE);
        Dynamic<NBTBase> dynamic = this.writeColumn(chunkcoordintpair, registryops);
        NBTBase nbtbase = (NBTBase) dynamic.getValue();

        if (nbtbase instanceof NBTTagCompound) {
            this.simpleRegionStorage.write(chunkcoordintpair, (NBTTagCompound) nbtbase).exceptionally((throwable) -> {
                this.errorReporter.reportChunkSaveFailure(throwable, this.simpleRegionStorage.storageInfo(), chunkcoordintpair);
                return null;
            });
        } else {
            RegionFileSection.LOGGER.error("Expected compound tag, got {}", nbtbase);
        }

    }

    private <T> Dynamic<T> writeColumn(ChunkCoordIntPair chunkcoordintpair, DynamicOps<T> dynamicops) {
        Map<T, T> map = Maps.newHashMap();

        for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
            long j = getKey(chunkcoordintpair, i);

            this.dirty.remove(j);
            Optional<R> optional = (Optional) this.storage.get(j);

            if (optional != null && !optional.isEmpty()) {
                DataResult<T> dataresult = ((Codec) this.codec.apply(() -> {
                    this.setDirty(j);
                })).encodeStart(dynamicops, optional.get());
                String s = Integer.toString(i);
                Logger logger = RegionFileSection.LOGGER;

                Objects.requireNonNull(logger);
                dataresult.resultOrPartial(logger::error).ifPresent((object) -> {
                    map.put(dynamicops.createString(s), object);
                });
            }
        }

        return new Dynamic(dynamicops, dynamicops.createMap(ImmutableMap.of(dynamicops.createString("Sections"), dynamicops.createMap(map), dynamicops.createString("DataVersion"), dynamicops.createInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion()))));
    }

    private static long getKey(ChunkCoordIntPair chunkcoordintpair, int i) {
        return SectionPosition.asLong(chunkcoordintpair.x, i, chunkcoordintpair.z);
    }

    protected void onSectionLoad(long i) {}

    protected void setDirty(long i) {
        Optional<R> optional = (Optional) this.storage.get(i);

        if (optional != null && !optional.isEmpty()) {
            this.dirty.add(i);
        } else {
            RegionFileSection.LOGGER.warn("No data for position: {}", SectionPosition.of(i));
        }
    }

    private static int getVersion(Dynamic<?> dynamic) {
        return dynamic.get("DataVersion").asInt(1945);
    }

    public void flush(ChunkCoordIntPair chunkcoordintpair) {
        if (this.hasWork()) {
            for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                long j = getKey(chunkcoordintpair, i);

                if (this.dirty.contains(j)) {
                    this.writeColumn(chunkcoordintpair);
                    return;
                }
            }
        }

    }

    public void close() throws IOException {
        this.simpleRegionStorage.close();
    }
}
