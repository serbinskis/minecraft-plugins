package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.storage.RegionFileCompression;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

@Category({"Minecraft", "Storage"})
@StackTrace(false)
@Enabled(false)
public abstract class ChunkRegionIoEvent extends Event {

    @Name("regionPosX")
    @Label("Region X Position")
    public final int regionPosX;
    @Name("regionPosZ")
    @Label("Region Z Position")
    public final int regionPosZ;
    @Name("localPosX")
    @Label("Local X Position")
    public final int localChunkPosX;
    @Name("localPosZ")
    @Label("Local Z Position")
    public final int localChunkPosZ;
    @Name("chunkPosX")
    @Label("Chunk X Position")
    public final int chunkPosX;
    @Name("chunkPosZ")
    @Label("Chunk Z Position")
    public final int chunkPosZ;
    @Name("level")
    @Label("Level Id")
    public final String levelId;
    @Name("dimension")
    @Label("Dimension")
    public final String dimension;
    @Name("type")
    @Label("Type")
    public final String type;
    @Name("compression")
    @Label("Compression")
    public final String compression;
    @Name("bytes")
    @Label("Bytes")
    public final int bytes;

    public ChunkRegionIoEvent(RegionStorageInfo regionstorageinfo, ChunkCoordIntPair chunkcoordintpair, RegionFileCompression regionfilecompression, int i) {
        this.regionPosX = chunkcoordintpair.getRegionX();
        this.regionPosZ = chunkcoordintpair.getRegionZ();
        this.localChunkPosX = chunkcoordintpair.getRegionLocalX();
        this.localChunkPosZ = chunkcoordintpair.getRegionLocalZ();
        this.chunkPosX = chunkcoordintpair.x;
        this.chunkPosZ = chunkcoordintpair.z;
        this.levelId = regionstorageinfo.level();
        this.dimension = regionstorageinfo.dimension().location().toString();
        this.type = regionstorageinfo.type();
        this.compression = "standard:" + regionfilecompression.getId();
        this.bytes = i;
    }

    public static class a {

        public static final String REGION_POS_X = "regionPosX";
        public static final String REGION_POS_Z = "regionPosZ";
        public static final String LOCAL_POS_X = "localPosX";
        public static final String LOCAL_POS_Z = "localPosZ";
        public static final String CHUNK_POS_X = "chunkPosX";
        public static final String CHUNK_POS_Z = "chunkPosZ";
        public static final String LEVEL = "level";
        public static final String DIMENSION = "dimension";
        public static final String TYPE = "type";
        public static final String COMPRESSION = "compression";
        public static final String BYTES = "bytes";

        private a() {}
    }
}
