package net.minecraft.util;

import java.util.Locale;
import java.util.function.Consumer;

public class StaticCache2D<T> {

    private final int minX;
    private final int minZ;
    private final int sizeX;
    private final int sizeZ;
    private final Object[] cache;

    public static <T> StaticCache2D<T> create(int i, int j, int k, StaticCache2D.a<T> staticcache2d_a) {
        int l = i - k;
        int i1 = j - k;
        int j1 = 2 * k + 1;

        return new StaticCache2D<>(l, i1, j1, j1, staticcache2d_a);
    }

    private StaticCache2D(int i, int j, int k, int l, StaticCache2D.a<T> staticcache2d_a) {
        this.minX = i;
        this.minZ = j;
        this.sizeX = k;
        this.sizeZ = l;
        this.cache = new Object[this.sizeX * this.sizeZ];

        for (int i1 = i; i1 < i + k; ++i1) {
            for (int j1 = j; j1 < j + l; ++j1) {
                this.cache[this.getIndex(i1, j1)] = staticcache2d_a.get(i1, j1);
            }
        }

    }

    public void forEach(Consumer<T> consumer) {
        Object[] aobject = this.cache;
        int i = aobject.length;

        for (int j = 0; j < i; ++j) {
            Object object = aobject[j];

            consumer.accept(object);
        }

    }

    public T get(int i, int j) {
        if (!this.contains(i, j)) {
            throw new IllegalArgumentException("Requested out of range value (" + i + "," + j + ") from " + String.valueOf(this));
        } else {
            return this.cache[this.getIndex(i, j)];
        }
    }

    public boolean contains(int i, int j) {
        int k = i - this.minX;
        int l = j - this.minZ;

        return k >= 0 && k < this.sizeX && l >= 0 && l < this.sizeZ;
    }

    public String toString() {
        return String.format(Locale.ROOT, "StaticCache2D[%d, %d, %d, %d]", this.minX, this.minZ, this.minX + this.sizeX, this.minZ + this.sizeZ);
    }

    private int getIndex(int i, int j) {
        int k = i - this.minX;
        int l = j - this.minZ;

        return k * this.sizeZ + l;
    }

    @FunctionalInterface
    public interface a<T> {

        T get(int i, int j);
    }
}
