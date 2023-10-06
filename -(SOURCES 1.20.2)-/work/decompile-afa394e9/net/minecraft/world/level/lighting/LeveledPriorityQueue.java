package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public class LeveledPriorityQueue {

    private final int levelCount;
    private final LongLinkedOpenHashSet[] queues;
    private int firstQueuedLevel;

    public LeveledPriorityQueue(int i, final int j) {
        this.levelCount = i;
        this.queues = new LongLinkedOpenHashSet[i];

        for (int k = 0; k < i; ++k) {
            this.queues[k] = new LongLinkedOpenHashSet(j, 0.5F) {
                protected void rehash(int l) {
                    if (l > j) {
                        super.rehash(l);
                    }

                }
            };
        }

        this.firstQueuedLevel = i;
    }

    public long removeFirstLong() {
        LongLinkedOpenHashSet longlinkedopenhashset = this.queues[this.firstQueuedLevel];
        long i = longlinkedopenhashset.removeFirstLong();

        if (longlinkedopenhashset.isEmpty()) {
            this.checkFirstQueuedLevel(this.levelCount);
        }

        return i;
    }

    public boolean isEmpty() {
        return this.firstQueuedLevel >= this.levelCount;
    }

    public void dequeue(long i, int j, int k) {
        LongLinkedOpenHashSet longlinkedopenhashset = this.queues[j];

        longlinkedopenhashset.remove(i);
        if (longlinkedopenhashset.isEmpty() && this.firstQueuedLevel == j) {
            this.checkFirstQueuedLevel(k);
        }

    }

    public void enqueue(long i, int j) {
        this.queues[j].add(i);
        if (this.firstQueuedLevel > j) {
            this.firstQueuedLevel = j;
        }

    }

    private void checkFirstQueuedLevel(int i) {
        int j = this.firstQueuedLevel;

        this.firstQueuedLevel = i;

        for (int k = j + 1; k < i; ++k) {
            if (!this.queues[k].isEmpty()) {
                this.firstQueuedLevel = k;
                break;
            }
        }

    }
}
