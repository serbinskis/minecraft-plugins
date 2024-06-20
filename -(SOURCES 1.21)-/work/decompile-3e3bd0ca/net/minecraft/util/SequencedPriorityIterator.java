package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Deque;
import javax.annotation.Nullable;

public final class SequencedPriorityIterator<T> extends AbstractIterator<T> {

    private static final int MIN_PRIO = Integer.MIN_VALUE;
    @Nullable
    private Deque<T> highestPrioQueue = null;
    private int highestPrio = Integer.MIN_VALUE;
    private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap();

    public SequencedPriorityIterator() {}

    public void add(T t0, int i) {
        if (i == this.highestPrio && this.highestPrioQueue != null) {
            this.highestPrioQueue.addLast(t0);
        } else {
            Deque<T> deque = (Deque) this.queuesByPriority.computeIfAbsent(i, (j) -> {
                return Queues.newArrayDeque();
            });

            deque.addLast(t0);
            if (i >= this.highestPrio) {
                this.highestPrioQueue = deque;
                this.highestPrio = i;
            }

        }
    }

    @Nullable
    protected T computeNext() {
        if (this.highestPrioQueue == null) {
            return this.endOfData();
        } else {
            T t0 = this.highestPrioQueue.removeFirst();

            if (t0 == null) {
                return this.endOfData();
            } else {
                if (this.highestPrioQueue.isEmpty()) {
                    this.switchCacheToNextHighestPrioQueue();
                }

                return t0;
            }
        }
    }

    private void switchCacheToNextHighestPrioQueue() {
        int i = Integer.MIN_VALUE;
        Deque<T> deque = null;
        ObjectIterator objectiterator = Int2ObjectMaps.fastIterable(this.queuesByPriority).iterator();

        while (objectiterator.hasNext()) {
            Entry<Deque<T>> entry = (Entry) objectiterator.next();
            Deque<T> deque1 = (Deque) entry.getValue();
            int j = entry.getIntKey();

            if (j > i && !deque1.isEmpty()) {
                i = j;
                deque = deque1;
                if (j == this.highestPrio - 1) {
                    break;
                }
            }
        }

        this.highestPrio = i;
        this.highestPrioQueue = deque;
    }
}
