package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkCoordIntPair;

public interface ChunkTrackingView {

    ChunkTrackingView EMPTY = new ChunkTrackingView() {
        @Override
        public boolean contains(int i, int j, boolean flag) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkCoordIntPair> consumer) {}
    };

    static ChunkTrackingView of(ChunkCoordIntPair chunkcoordintpair, int i) {
        return new ChunkTrackingView.a(chunkcoordintpair, i);
    }

    static void difference(ChunkTrackingView chunktrackingview, ChunkTrackingView chunktrackingview1, Consumer<ChunkCoordIntPair> consumer, Consumer<ChunkCoordIntPair> consumer1) {
        if (!chunktrackingview.equals(chunktrackingview1)) {
            if (chunktrackingview instanceof ChunkTrackingView.a) {
                ChunkTrackingView.a chunktrackingview_a = (ChunkTrackingView.a) chunktrackingview;

                if (chunktrackingview1 instanceof ChunkTrackingView.a) {
                    ChunkTrackingView.a chunktrackingview_a1 = (ChunkTrackingView.a) chunktrackingview1;

                    if (chunktrackingview_a.squareIntersects(chunktrackingview_a1)) {
                        int i = Math.min(chunktrackingview_a.minX(), chunktrackingview_a1.minX());
                        int j = Math.min(chunktrackingview_a.minZ(), chunktrackingview_a1.minZ());
                        int k = Math.max(chunktrackingview_a.maxX(), chunktrackingview_a1.maxX());
                        int l = Math.max(chunktrackingview_a.maxZ(), chunktrackingview_a1.maxZ());

                        for (int i1 = i; i1 <= k; ++i1) {
                            for (int j1 = j; j1 <= l; ++j1) {
                                boolean flag = chunktrackingview_a.contains(i1, j1);
                                boolean flag1 = chunktrackingview_a1.contains(i1, j1);

                                if (flag != flag1) {
                                    if (flag1) {
                                        consumer.accept(new ChunkCoordIntPair(i1, j1));
                                    } else {
                                        consumer1.accept(new ChunkCoordIntPair(i1, j1));
                                    }
                                }
                            }
                        }

                        return;
                    }
                }
            }

            chunktrackingview.forEach(consumer1);
            chunktrackingview1.forEach(consumer);
        }
    }

    default boolean contains(ChunkCoordIntPair chunkcoordintpair) {
        return this.contains(chunkcoordintpair.x, chunkcoordintpair.z);
    }

    default boolean contains(int i, int j) {
        return this.contains(i, j, true);
    }

    boolean contains(int i, int j, boolean flag);

    void forEach(Consumer<ChunkCoordIntPair> consumer);

    default boolean isInViewDistance(int i, int j) {
        return this.contains(i, j, false);
    }

    static boolean isInViewDistance(int i, int j, int k, int l, int i1) {
        return isWithinDistance(i, j, k, l, i1, false);
    }

    static boolean isWithinDistance(int i, int j, int k, int l, int i1, boolean flag) {
        int j1 = Math.max(0, Math.abs(l - i) - 1);
        int k1 = Math.max(0, Math.abs(i1 - j) - 1);
        long l1 = (long) Math.max(0, Math.max(j1, k1) - (flag ? 1 : 0));
        long i2 = (long) Math.min(j1, k1);
        long j2 = i2 * i2 + l1 * l1;
        int k2 = k * k;

        return j2 < (long) k2;
    }

    public static record a(ChunkCoordIntPair center, int viewDistance) implements ChunkTrackingView {

        int minX() {
            return this.center.x - this.viewDistance - 1;
        }

        int minZ() {
            return this.center.z - this.viewDistance - 1;
        }

        int maxX() {
            return this.center.x + this.viewDistance + 1;
        }

        int maxZ() {
            return this.center.z + this.viewDistance + 1;
        }

        @VisibleForTesting
        protected boolean squareIntersects(ChunkTrackingView.a chunktrackingview_a) {
            return this.minX() <= chunktrackingview_a.maxX() && this.maxX() >= chunktrackingview_a.minX() && this.minZ() <= chunktrackingview_a.maxZ() && this.maxZ() >= chunktrackingview_a.minZ();
        }

        @Override
        public boolean contains(int i, int j, boolean flag) {
            return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, i, j, flag);
        }

        @Override
        public void forEach(Consumer<ChunkCoordIntPair> consumer) {
            for (int i = this.minX(); i <= this.maxX(); ++i) {
                for (int j = this.minZ(); j <= this.maxZ(); ++j) {
                    if (this.contains(i, j)) {
                        consumer.accept(new ChunkCoordIntPair(i, j));
                    }
                }
            }

        }
    }
}
