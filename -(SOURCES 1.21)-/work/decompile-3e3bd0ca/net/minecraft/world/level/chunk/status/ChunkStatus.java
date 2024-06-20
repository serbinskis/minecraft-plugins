package net.minecraft.world.level.chunk.status;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.levelgen.HeightMap;
import org.jetbrains.annotations.VisibleForTesting;

public class ChunkStatus {

    public static final int MAX_STRUCTURE_DISTANCE = 8;
    private static final EnumSet<HeightMap.Type> WORLDGEN_HEIGHTMAPS = EnumSet.of(HeightMap.Type.OCEAN_FLOOR_WG, HeightMap.Type.WORLD_SURFACE_WG);
    public static final EnumSet<HeightMap.Type> FINAL_HEIGHTMAPS = EnumSet.of(HeightMap.Type.OCEAN_FLOOR, HeightMap.Type.WORLD_SURFACE, HeightMap.Type.MOTION_BLOCKING, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES);
    public static final ChunkStatus EMPTY = register("empty", (ChunkStatus) null, ChunkStatus.WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus STRUCTURE_STARTS = register("structure_starts", ChunkStatus.EMPTY, ChunkStatus.WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus STRUCTURE_REFERENCES = register("structure_references", ChunkStatus.STRUCTURE_STARTS, ChunkStatus.WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus BIOMES = register("biomes", ChunkStatus.STRUCTURE_REFERENCES, ChunkStatus.WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus NOISE = register("noise", ChunkStatus.BIOMES, ChunkStatus.WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus SURFACE = register("surface", ChunkStatus.NOISE, ChunkStatus.WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus CARVERS = register("carvers", ChunkStatus.SURFACE, ChunkStatus.FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus FEATURES = register("features", ChunkStatus.CARVERS, ChunkStatus.FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus INITIALIZE_LIGHT = register("initialize_light", ChunkStatus.FEATURES, ChunkStatus.FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus LIGHT = register("light", ChunkStatus.INITIALIZE_LIGHT, ChunkStatus.FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus SPAWN = register("spawn", ChunkStatus.LIGHT, ChunkStatus.FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
    public static final ChunkStatus FULL = register("full", ChunkStatus.SPAWN, ChunkStatus.FINAL_HEIGHTMAPS, ChunkType.LEVELCHUNK);
    private final int index;
    private final ChunkStatus parent;
    private final ChunkType chunkType;
    private final EnumSet<HeightMap.Type> heightmapsAfter;

    private static ChunkStatus register(String s, @Nullable ChunkStatus chunkstatus, EnumSet<HeightMap.Type> enumset, ChunkType chunktype) {
        return (ChunkStatus) IRegistry.register(BuiltInRegistries.CHUNK_STATUS, s, new ChunkStatus(chunkstatus, enumset, chunktype));
    }

    public static List<ChunkStatus> getStatusList() {
        List<ChunkStatus> list = Lists.newArrayList();

        ChunkStatus chunkstatus;

        for (chunkstatus = ChunkStatus.FULL; chunkstatus.getParent() != chunkstatus; chunkstatus = chunkstatus.getParent()) {
            list.add(chunkstatus);
        }

        list.add(chunkstatus);
        Collections.reverse(list);
        return list;
    }

    @VisibleForTesting
    protected ChunkStatus(@Nullable ChunkStatus chunkstatus, EnumSet<HeightMap.Type> enumset, ChunkType chunktype) {
        this.parent = chunkstatus == null ? this : chunkstatus;
        this.chunkType = chunktype;
        this.heightmapsAfter = enumset;
        this.index = chunkstatus == null ? 0 : chunkstatus.getIndex() + 1;
    }

    public int getIndex() {
        return this.index;
    }

    public ChunkStatus getParent() {
        return this.parent;
    }

    public ChunkType getChunkType() {
        return this.chunkType;
    }

    public static ChunkStatus byName(String s) {
        return (ChunkStatus) BuiltInRegistries.CHUNK_STATUS.get(MinecraftKey.tryParse(s));
    }

    public EnumSet<HeightMap.Type> heightmapsAfter() {
        return this.heightmapsAfter;
    }

    public boolean isOrAfter(ChunkStatus chunkstatus) {
        return this.getIndex() >= chunkstatus.getIndex();
    }

    public boolean isAfter(ChunkStatus chunkstatus) {
        return this.getIndex() > chunkstatus.getIndex();
    }

    public boolean isOrBefore(ChunkStatus chunkstatus) {
        return this.getIndex() <= chunkstatus.getIndex();
    }

    public boolean isBefore(ChunkStatus chunkstatus) {
        return this.getIndex() < chunkstatus.getIndex();
    }

    public static ChunkStatus max(ChunkStatus chunkstatus, ChunkStatus chunkstatus1) {
        return chunkstatus.isAfter(chunkstatus1) ? chunkstatus : chunkstatus1;
    }

    public String toString() {
        return this.getName();
    }

    public String getName() {
        return BuiltInRegistries.CHUNK_STATUS.getKey(this).toString();
    }
}
