package net.minecraft.util.debugchart;

public abstract class AbstractSampleLogger implements SampleLogger {

    protected final long[] defaults;
    protected final long[] sample;

    protected AbstractSampleLogger(int i, long[] along) {
        if (along.length != i) {
            throw new IllegalArgumentException("defaults have incorrect length of " + along.length);
        } else {
            this.sample = new long[i];
            this.defaults = along;
        }
    }

    @Override
    public void logFullSample(long[] along) {
        System.arraycopy(along, 0, this.sample, 0, along.length);
        this.useSample();
        this.resetSample();
    }

    @Override
    public void logSample(long i) {
        this.sample[0] = i;
        this.useSample();
        this.resetSample();
    }

    @Override
    public void logPartialSample(long i, int j) {
        if (j >= 1 && j < this.sample.length) {
            this.sample[j] = i;
        } else {
            throw new IndexOutOfBoundsException("" + j + " out of bounds for dimensions " + this.sample.length);
        }
    }

    protected abstract void useSample();

    protected void resetSample() {
        System.arraycopy(this.defaults, 0, this.sample, 0, this.defaults.length);
    }
}
