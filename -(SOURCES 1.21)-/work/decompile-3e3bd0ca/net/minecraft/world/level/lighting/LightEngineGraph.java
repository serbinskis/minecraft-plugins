package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.function.LongPredicate;
import net.minecraft.util.MathHelper;

public abstract class LightEngineGraph {

    public static final long SOURCE = Long.MAX_VALUE;
    private static final int NO_COMPUTED_LEVEL = 255;
    protected final int levelCount;
    private final LeveledPriorityQueue priorityQueue;
    private final Long2ByteMap computedLevels;
    private volatile boolean hasWork;

    protected LightEngineGraph(int i, int j, final int k) {
        if (i >= 254) {
            throw new IllegalArgumentException("Level count must be < 254.");
        } else {
            this.levelCount = i;
            this.priorityQueue = new LeveledPriorityQueue(i, j);
            this.computedLevels = new Long2ByteOpenHashMap(k, 0.5F) {
                protected void rehash(int l) {
                    if (l > k) {
                        super.rehash(l);
                    }

                }
            };
            this.computedLevels.defaultReturnValue((byte) -1);
        }
    }

    protected void removeFromQueue(long i) {
        int j = this.computedLevels.remove(i) & 255;

        if (j != 255) {
            int k = this.getLevel(i);
            int l = this.calculatePriority(k, j);

            this.priorityQueue.dequeue(i, l, this.levelCount);
            this.hasWork = !this.priorityQueue.isEmpty();
        }
    }

    public void removeIf(LongPredicate longpredicate) {
        LongArrayList longarraylist = new LongArrayList();

        this.computedLevels.keySet().forEach((i) -> {
            if (longpredicate.test(i)) {
                longarraylist.add(i);
            }

        });
        longarraylist.forEach(this::removeFromQueue);
    }

    private int calculatePriority(int i, int j) {
        return Math.min(Math.min(i, j), this.levelCount - 1);
    }

    protected void checkNode(long i) {
        this.checkEdge(i, i, this.levelCount - 1, false);
    }

    protected void checkEdge(long i, long j, int k, boolean flag) {
        this.checkEdge(i, j, k, this.getLevel(j), this.computedLevels.get(j) & 255, flag);
        this.hasWork = !this.priorityQueue.isEmpty();
    }

    private void checkEdge(long i, long j, int k, int l, int i1, boolean flag) {
        if (!this.isSource(j)) {
            k = MathHelper.clamp(k, 0, this.levelCount - 1);
            l = MathHelper.clamp(l, 0, this.levelCount - 1);
            boolean flag1 = i1 == 255;

            if (flag1) {
                i1 = l;
            }

            int j1;

            if (flag) {
                j1 = Math.min(i1, k);
            } else {
                j1 = MathHelper.clamp(this.getComputedLevel(j, i, k), 0, this.levelCount - 1);
            }

            int k1 = this.calculatePriority(l, i1);

            if (l != j1) {
                int l1 = this.calculatePriority(l, j1);

                if (k1 != l1 && !flag1) {
                    this.priorityQueue.dequeue(j, k1, l1);
                }

                this.priorityQueue.enqueue(j, l1);
                this.computedLevels.put(j, (byte) j1);
            } else if (!flag1) {
                this.priorityQueue.dequeue(j, k1, this.levelCount);
                this.computedLevels.remove(j);
            }

        }
    }

    protected final void checkNeighbor(long i, long j, int k, boolean flag) {
        int l = this.computedLevels.get(j) & 255;
        int i1 = MathHelper.clamp(this.computeLevelFromNeighbor(i, j, k), 0, this.levelCount - 1);

        if (flag) {
            this.checkEdge(i, j, i1, this.getLevel(j), l, flag);
        } else {
            boolean flag1 = l == 255;
            int j1;

            if (flag1) {
                j1 = MathHelper.clamp(this.getLevel(j), 0, this.levelCount - 1);
            } else {
                j1 = l;
            }

            if (i1 == j1) {
                this.checkEdge(i, j, this.levelCount - 1, flag1 ? j1 : this.getLevel(j), l, flag);
            }
        }

    }

    protected final boolean hasWork() {
        return this.hasWork;
    }

    protected final int runUpdates(int i) {
        if (this.priorityQueue.isEmpty()) {
            return i;
        } else {
            while (!this.priorityQueue.isEmpty() && i > 0) {
                --i;
                long j = this.priorityQueue.removeFirstLong();
                int k = MathHelper.clamp(this.getLevel(j), 0, this.levelCount - 1);
                int l = this.computedLevels.remove(j) & 255;

                if (l < k) {
                    this.setLevel(j, l);
                    this.checkNeighborsAfterUpdate(j, l, true);
                } else if (l > k) {
                    this.setLevel(j, this.levelCount - 1);
                    if (l != this.levelCount - 1) {
                        this.priorityQueue.enqueue(j, this.calculatePriority(this.levelCount - 1, l));
                        this.computedLevels.put(j, (byte) l);
                    }

                    this.checkNeighborsAfterUpdate(j, k, false);
                }
            }

            this.hasWork = !this.priorityQueue.isEmpty();
            return i;
        }
    }

    public int getQueueSize() {
        return this.computedLevels.size();
    }

    protected boolean isSource(long i) {
        return i == Long.MAX_VALUE;
    }

    protected abstract int getComputedLevel(long i, long j, int k);

    protected abstract void checkNeighborsAfterUpdate(long i, int j, boolean flag);

    protected abstract int getLevel(long i);

    protected abstract void setLevel(long i, int j);

    protected abstract int computeLevelFromNeighbor(long i, long j, int k);
}
