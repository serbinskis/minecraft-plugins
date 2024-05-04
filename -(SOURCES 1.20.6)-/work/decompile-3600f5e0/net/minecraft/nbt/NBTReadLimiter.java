package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NBTReadLimiter {

    private static final int MAX_STACK_DEPTH = 512;
    private final long quota;
    private long usage;
    private final int maxDepth;
    private int depth;

    public NBTReadLimiter(long i, int j) {
        this.quota = i;
        this.maxDepth = j;
    }

    public static NBTReadLimiter create(long i) {
        return new NBTReadLimiter(i, 512);
    }

    public static NBTReadLimiter unlimitedHeap() {
        return new NBTReadLimiter(Long.MAX_VALUE, 512);
    }

    public void accountBytes(long i, long j) {
        this.accountBytes(i * j);
    }

    public void accountBytes(long i) {
        if (this.usage + i > this.quota) {
            throw new NbtAccounterException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage + " + " + i + " bytes where max allowed: " + this.quota);
        } else {
            this.usage += i;
        }
    }

    public void pushDepth() {
        if (this.depth >= this.maxDepth) {
            throw new NbtAccounterException("Tried to read NBT tag with too high complexity, depth > " + this.maxDepth);
        } else {
            ++this.depth;
        }
    }

    public void popDepth() {
        if (this.depth <= 0) {
            throw new NbtAccounterException("NBT-Accounter tried to pop stack-depth at top-level");
        } else {
            --this.depth;
        }
    }

    @VisibleForTesting
    public long getUsage() {
        return this.usage;
    }

    @VisibleForTesting
    public int getDepth() {
        return this.depth;
    }
}
