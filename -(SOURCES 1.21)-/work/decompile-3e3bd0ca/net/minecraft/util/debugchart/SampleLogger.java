package net.minecraft.util.debugchart;

public interface SampleLogger {

    void logFullSample(long[] along);

    void logSample(long i);

    void logPartialSample(long i, int j);
}
