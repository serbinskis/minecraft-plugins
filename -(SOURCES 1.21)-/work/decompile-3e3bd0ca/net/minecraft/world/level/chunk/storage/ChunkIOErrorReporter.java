package net.minecraft.world.level.chunk.storage;

import java.util.Objects;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.world.level.ChunkCoordIntPair;

public interface ChunkIOErrorReporter {

    void reportChunkLoadFailure(Throwable throwable, RegionStorageInfo regionstorageinfo, ChunkCoordIntPair chunkcoordintpair);

    void reportChunkSaveFailure(Throwable throwable, RegionStorageInfo regionstorageinfo, ChunkCoordIntPair chunkcoordintpair);

    static ReportedException createMisplacedChunkReport(ChunkCoordIntPair chunkcoordintpair, ChunkCoordIntPair chunkcoordintpair1) {
        String s = String.valueOf(chunkcoordintpair);
        CrashReport crashreport = CrashReport.forThrowable(new IllegalStateException("Retrieved chunk position " + s + " does not match requested " + String.valueOf(chunkcoordintpair1)), "Chunk found in invalid location");
        CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Misplaced Chunk");

        Objects.requireNonNull(chunkcoordintpair);
        crashreportsystemdetails.setDetail("Stored Position", chunkcoordintpair::toString);
        return new ReportedException(crashreport);
    }

    default void reportMisplacedChunk(ChunkCoordIntPair chunkcoordintpair, ChunkCoordIntPair chunkcoordintpair1, RegionStorageInfo regionstorageinfo) {
        this.reportChunkLoadFailure(createMisplacedChunkReport(chunkcoordintpair, chunkcoordintpair1), regionstorageinfo, chunkcoordintpair1);
    }
}
