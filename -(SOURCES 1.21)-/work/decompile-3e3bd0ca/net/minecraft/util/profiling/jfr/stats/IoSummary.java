package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public final class IoSummary<T> {

    private final IoSummary.a totalCountAndSize;
    private final List<Pair<T, IoSummary.a>> largestSizeContributors;
    private final Duration recordingDuration;

    public IoSummary(Duration duration, List<Pair<T, IoSummary.a>> list) {
        this.recordingDuration = duration;
        this.totalCountAndSize = (IoSummary.a) list.stream().map(Pair::getSecond).reduce(new IoSummary.a(0L, 0L), IoSummary.a::add);
        this.largestSizeContributors = list.stream().sorted(Comparator.comparing(Pair::getSecond, IoSummary.a.SIZE_THEN_COUNT)).limit(10L).toList();
    }

    public double getCountsPerSecond() {
        return (double) this.totalCountAndSize.totalCount / (double) this.recordingDuration.getSeconds();
    }

    public double getSizePerSecond() {
        return (double) this.totalCountAndSize.totalSize / (double) this.recordingDuration.getSeconds();
    }

    public long getTotalCount() {
        return this.totalCountAndSize.totalCount;
    }

    public long getTotalSize() {
        return this.totalCountAndSize.totalSize;
    }

    public List<Pair<T, IoSummary.a>> largestSizeContributors() {
        return this.largestSizeContributors;
    }

    public static record a(long totalCount, long totalSize) {

        static final Comparator<IoSummary.a> SIZE_THEN_COUNT = Comparator.comparing(IoSummary.a::totalSize).thenComparing(IoSummary.a::totalCount).reversed();

        IoSummary.a add(IoSummary.a iosummary_a) {
            return new IoSummary.a(this.totalCount + iosummary_a.totalCount, this.totalSize + iosummary_a.totalSize);
        }

        public float averageSize() {
            return (float) this.totalSize / (float) this.totalCount;
        }
    }
}
