package net.minecraft.util.profiling.jfr.parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.ChunkIdentification;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.IoSummary;
import net.minecraft.util.profiling.jfr.stats.PacketIdentification;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;

public class JfrStatsParser {

    private Instant recordingStarted;
    private Instant recordingEnded;
    private final List<ChunkGenStat> chunkGenStats;
    private final List<CpuLoadStat> cpuLoadStat;
    private final Map<PacketIdentification, JfrStatsParser.a> receivedPackets;
    private final Map<PacketIdentification, JfrStatsParser.a> sentPackets;
    private final Map<ChunkIdentification, JfrStatsParser.a> readChunks;
    private final Map<ChunkIdentification, JfrStatsParser.a> writtenChunks;
    private final List<FileIOStat> fileWrites;
    private final List<FileIOStat> fileReads;
    private int garbageCollections;
    private Duration gcTotalDuration;
    private final List<GcHeapStat> gcHeapStats;
    private final List<ThreadAllocationStat> threadAllocationStats;
    private final List<TickTimeStat> tickTimes;
    @Nullable
    private Duration worldCreationDuration;

    private JfrStatsParser(Stream<RecordedEvent> stream) {
        this.recordingStarted = Instant.EPOCH;
        this.recordingEnded = Instant.EPOCH;
        this.chunkGenStats = Lists.newArrayList();
        this.cpuLoadStat = Lists.newArrayList();
        this.receivedPackets = Maps.newHashMap();
        this.sentPackets = Maps.newHashMap();
        this.readChunks = Maps.newHashMap();
        this.writtenChunks = Maps.newHashMap();
        this.fileWrites = Lists.newArrayList();
        this.fileReads = Lists.newArrayList();
        this.gcTotalDuration = Duration.ZERO;
        this.gcHeapStats = Lists.newArrayList();
        this.threadAllocationStats = Lists.newArrayList();
        this.tickTimes = Lists.newArrayList();
        this.worldCreationDuration = null;
        this.capture(stream);
    }

    public static JfrStatsResult parse(Path path) {
        try {
            final RecordingFile recordingfile = new RecordingFile(path);

            JfrStatsResult jfrstatsresult;

            try {
                Iterator<RecordedEvent> iterator = new Iterator<RecordedEvent>() {
                    public boolean hasNext() {
                        return recordingfile.hasMoreEvents();
                    }

                    public RecordedEvent next() {
                        if (!this.hasNext()) {
                            throw new NoSuchElementException();
                        } else {
                            try {
                                return recordingfile.readEvent();
                            } catch (IOException ioexception) {
                                throw new UncheckedIOException(ioexception);
                            }
                        }
                    }
                };
                Stream<RecordedEvent> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1297), false);

                jfrstatsresult = (new JfrStatsParser(stream)).results();
            } catch (Throwable throwable) {
                try {
                    recordingfile.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            recordingfile.close();
            return jfrstatsresult;
        } catch (IOException ioexception) {
            throw new UncheckedIOException(ioexception);
        }
    }

    private JfrStatsResult results() {
        Duration duration = Duration.between(this.recordingStarted, this.recordingEnded);

        return new JfrStatsResult(this.recordingStarted, this.recordingEnded, duration, this.worldCreationDuration, this.tickTimes, this.cpuLoadStat, GcHeapStat.summary(duration, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections), ThreadAllocationStat.summary(this.threadAllocationStats), collectIoStats(duration, this.receivedPackets), collectIoStats(duration, this.sentPackets), collectIoStats(duration, this.writtenChunks), collectIoStats(duration, this.readChunks), FileIOStat.summary(duration, this.fileWrites), FileIOStat.summary(duration, this.fileReads), this.chunkGenStats);
    }

    private void capture(Stream<RecordedEvent> stream) {
        stream.forEach((recordedevent) -> {
            if (recordedevent.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
                this.recordingEnded = recordedevent.getEndTime();
            }

            if (recordedevent.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
                this.recordingStarted = recordedevent.getStartTime();
            }

            switch (recordedevent.getEventType().getName()) {
                case "minecraft.ChunkGeneration":
                    this.chunkGenStats.add(ChunkGenStat.from(recordedevent));
                    break;
                case "minecraft.LoadWorld":
                    this.worldCreationDuration = recordedevent.getDuration();
                    break;
                case "minecraft.ServerTickTime":
                    this.tickTimes.add(TickTimeStat.from(recordedevent));
                    break;
                case "minecraft.PacketReceived":
                    this.incrementPacket(recordedevent, recordedevent.getInt("bytes"), this.receivedPackets);
                    break;
                case "minecraft.PacketSent":
                    this.incrementPacket(recordedevent, recordedevent.getInt("bytes"), this.sentPackets);
                    break;
                case "minecraft.ChunkRegionRead":
                    this.incrementChunk(recordedevent, recordedevent.getInt("bytes"), this.readChunks);
                    break;
                case "minecraft.ChunkRegionWrite":
                    this.incrementChunk(recordedevent, recordedevent.getInt("bytes"), this.writtenChunks);
                    break;
                case "jdk.ThreadAllocationStatistics":
                    this.threadAllocationStats.add(ThreadAllocationStat.from(recordedevent));
                    break;
                case "jdk.GCHeapSummary":
                    this.gcHeapStats.add(GcHeapStat.from(recordedevent));
                    break;
                case "jdk.CPULoad":
                    this.cpuLoadStat.add(CpuLoadStat.from(recordedevent));
                    break;
                case "jdk.FileWrite":
                    this.appendFileIO(recordedevent, this.fileWrites, "bytesWritten");
                    break;
                case "jdk.FileRead":
                    this.appendFileIO(recordedevent, this.fileReads, "bytesRead");
                    break;
                case "jdk.GarbageCollection":
                    ++this.garbageCollections;
                    this.gcTotalDuration = this.gcTotalDuration.plus(recordedevent.getDuration());
            }

        });
    }

    private void incrementPacket(RecordedEvent recordedevent, int i, Map<PacketIdentification, JfrStatsParser.a> map) {
        ((JfrStatsParser.a) map.computeIfAbsent(PacketIdentification.from(recordedevent), (packetidentification) -> {
            return new JfrStatsParser.a();
        })).increment(i);
    }

    private void incrementChunk(RecordedEvent recordedevent, int i, Map<ChunkIdentification, JfrStatsParser.a> map) {
        ((JfrStatsParser.a) map.computeIfAbsent(ChunkIdentification.from(recordedevent), (chunkidentification) -> {
            return new JfrStatsParser.a();
        })).increment(i);
    }

    private void appendFileIO(RecordedEvent recordedevent, List<FileIOStat> list, String s) {
        list.add(new FileIOStat(recordedevent.getDuration(), recordedevent.getString("path"), recordedevent.getLong(s)));
    }

    private static <T> IoSummary<T> collectIoStats(Duration duration, Map<T, JfrStatsParser.a> map) {
        List<Pair<T, IoSummary.a>> list = map.entrySet().stream().map((entry) -> {
            return Pair.of(entry.getKey(), ((JfrStatsParser.a) entry.getValue()).toCountAndSize());
        }).toList();

        return new IoSummary<>(duration, list);
    }

    public static final class a {

        private long count;
        private long totalSize;

        public a() {}

        public void increment(int i) {
            this.totalSize += (long) i;
            ++this.count;
        }

        public IoSummary.a toCountAndSize() {
            return new IoSummary.a(this.count, this.totalSize);
        }
    }
}
