package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.NibbleArray;

public abstract class LightEngineStorage<M extends LightEngineStorageArray<M>> {

    private final EnumSkyBlock layer;
    protected final ILightAccess chunkSource;
    protected final Long2ByteMap sectionStates = new Long2ByteOpenHashMap();
    private final LongSet columnsWithSources = new LongOpenHashSet();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections = new LongOpenHashSet();
    protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
    protected final Long2ObjectMap<NibbleArray> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
    private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
    private final LongSet toRemove = new LongOpenHashSet();
    protected volatile boolean hasInconsistencies;

    protected LightEngineStorage(EnumSkyBlock enumskyblock, ILightAccess ilightaccess, M m0) {
        this.layer = enumskyblock;
        this.chunkSource = ilightaccess;
        this.updatingSectionData = m0;
        this.visibleSectionData = m0.copy();
        this.visibleSectionData.disableCache();
        this.sectionStates.defaultReturnValue((byte) 0);
    }

    protected boolean storingLightForSection(long i) {
        return this.getDataLayer(i, true) != null;
    }

    @Nullable
    protected NibbleArray getDataLayer(long i, boolean flag) {
        return this.getDataLayer(flag ? this.updatingSectionData : this.visibleSectionData, i);
    }

    @Nullable
    protected NibbleArray getDataLayer(M m0, long i) {
        return m0.getLayer(i);
    }

    @Nullable
    protected NibbleArray getDataLayerToWrite(long i) {
        NibbleArray nibblearray = this.updatingSectionData.getLayer(i);

        if (nibblearray == null) {
            return null;
        } else {
            if (this.changedSections.add(i)) {
                nibblearray = nibblearray.copy();
                this.updatingSectionData.setLayer(i, nibblearray);
                this.updatingSectionData.clearCache();
            }

            return nibblearray;
        }
    }

    @Nullable
    public NibbleArray getDataLayerData(long i) {
        NibbleArray nibblearray = (NibbleArray) this.queuedSections.get(i);

        return nibblearray != null ? nibblearray : this.getDataLayer(i, false);
    }

    protected abstract int getLightValue(long i);

    protected int getStoredLevel(long i) {
        long j = SectionPosition.blockToSection(i);
        NibbleArray nibblearray = this.getDataLayer(j, true);

        return nibblearray.get(SectionPosition.sectionRelative(BlockPosition.getX(i)), SectionPosition.sectionRelative(BlockPosition.getY(i)), SectionPosition.sectionRelative(BlockPosition.getZ(i)));
    }

    protected void setStoredLevel(long i, int j) {
        long k = SectionPosition.blockToSection(i);
        NibbleArray nibblearray;

        if (this.changedSections.add(k)) {
            nibblearray = this.updatingSectionData.copyDataLayer(k);
        } else {
            nibblearray = this.getDataLayer(k, true);
        }

        nibblearray.set(SectionPosition.sectionRelative(BlockPosition.getX(i)), SectionPosition.sectionRelative(BlockPosition.getY(i)), SectionPosition.sectionRelative(BlockPosition.getZ(i)), j);
        LongSet longset = this.sectionsAffectedByLightUpdates;

        Objects.requireNonNull(this.sectionsAffectedByLightUpdates);
        SectionPosition.aroundAndAtBlockPos(i, longset::add);
    }

    protected void markSectionAndNeighborsAsAffected(long i) {
        int j = SectionPosition.x(i);
        int k = SectionPosition.y(i);
        int l = SectionPosition.z(i);

        for (int i1 = -1; i1 <= 1; ++i1) {
            for (int j1 = -1; j1 <= 1; ++j1) {
                for (int k1 = -1; k1 <= 1; ++k1) {
                    this.sectionsAffectedByLightUpdates.add(SectionPosition.asLong(j + j1, k + k1, l + i1));
                }
            }
        }

    }

    protected NibbleArray createDataLayer(long i) {
        NibbleArray nibblearray = (NibbleArray) this.queuedSections.get(i);

        return nibblearray != null ? nibblearray : new NibbleArray();
    }

    protected boolean hasInconsistencies() {
        return this.hasInconsistencies;
    }

    protected void markNewInconsistencies(LightEngine<M, ?> lightengine) {
        if (this.hasInconsistencies) {
            this.hasInconsistencies = false;
            LongIterator longiterator = this.toRemove.iterator();

            long i;
            NibbleArray nibblearray;

            while (longiterator.hasNext()) {
                i = (Long) longiterator.next();
                NibbleArray nibblearray1 = (NibbleArray) this.queuedSections.remove(i);

                nibblearray = this.updatingSectionData.removeLayer(i);
                if (this.columnsToRetainQueuedDataFor.contains(SectionPosition.getZeroNode(i))) {
                    if (nibblearray1 != null) {
                        this.queuedSections.put(i, nibblearray1);
                    } else if (nibblearray != null) {
                        this.queuedSections.put(i, nibblearray);
                    }
                }
            }

            this.updatingSectionData.clearCache();
            longiterator = this.toRemove.iterator();

            while (longiterator.hasNext()) {
                i = (Long) longiterator.next();
                this.onNodeRemoved(i);
                this.changedSections.add(i);
            }

            this.toRemove.clear();
            ObjectIterator objectiterator = Long2ObjectMaps.fastIterator(this.queuedSections);

            while (objectiterator.hasNext()) {
                Entry<NibbleArray> entry = (Entry) objectiterator.next();
                long j = entry.getLongKey();

                if (this.storingLightForSection(j)) {
                    nibblearray = (NibbleArray) entry.getValue();
                    if (this.updatingSectionData.getLayer(j) != nibblearray) {
                        this.updatingSectionData.setLayer(j, nibblearray);
                        this.changedSections.add(j);
                    }

                    objectiterator.remove();
                }
            }

            this.updatingSectionData.clearCache();
        }
    }

    protected void onNodeAdded(long i) {}

    protected void onNodeRemoved(long i) {}

    protected void setLightEnabled(long i, boolean flag) {
        if (flag) {
            this.columnsWithSources.add(i);
        } else {
            this.columnsWithSources.remove(i);
        }

    }

    protected boolean lightOnInSection(long i) {
        long j = SectionPosition.getZeroNode(i);

        return this.columnsWithSources.contains(j);
    }

    public void retainData(long i, boolean flag) {
        if (flag) {
            this.columnsToRetainQueuedDataFor.add(i);
        } else {
            this.columnsToRetainQueuedDataFor.remove(i);
        }

    }

    protected void queueSectionData(long i, @Nullable NibbleArray nibblearray) {
        if (nibblearray != null) {
            this.queuedSections.put(i, nibblearray);
            this.hasInconsistencies = true;
        } else {
            this.queuedSections.remove(i);
        }

    }

    protected void updateSectionStatus(long i, boolean flag) {
        byte b0 = this.sectionStates.get(i);
        byte b1 = LightEngineStorage.a.hasData(b0, !flag);

        if (b0 != b1) {
            this.putSectionState(i, b1);
            int j = flag ? -1 : 1;

            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
                    for (int i1 = -1; i1 <= 1; ++i1) {
                        if (k != 0 || l != 0 || i1 != 0) {
                            long j1 = SectionPosition.offset(i, k, l, i1);
                            byte b2 = this.sectionStates.get(j1);

                            this.putSectionState(j1, LightEngineStorage.a.neighborCount(b2, LightEngineStorage.a.neighborCount(b2) + j));
                        }
                    }
                }
            }

        }
    }

    protected void putSectionState(long i, byte b0) {
        if (b0 != 0) {
            if (this.sectionStates.put(i, b0) == 0) {
                this.initializeSection(i);
            }
        } else if (this.sectionStates.remove(i) != 0) {
            this.removeSection(i);
        }

    }

    private void initializeSection(long i) {
        if (!this.toRemove.remove(i)) {
            this.updatingSectionData.setLayer(i, this.createDataLayer(i));
            this.changedSections.add(i);
            this.onNodeAdded(i);
            this.markSectionAndNeighborsAsAffected(i);
            this.hasInconsistencies = true;
        }

    }

    private void removeSection(long i) {
        this.toRemove.add(i);
        this.hasInconsistencies = true;
    }

    protected void swapSectionMap() {
        if (!this.changedSections.isEmpty()) {
            M m0 = this.updatingSectionData.copy();

            m0.disableCache();
            this.visibleSectionData = m0;
            this.changedSections.clear();
        }

        if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
            LongIterator longiterator = this.sectionsAffectedByLightUpdates.iterator();

            while (longiterator.hasNext()) {
                long i = longiterator.nextLong();

                this.chunkSource.onLightUpdate(this.layer, SectionPosition.of(i));
            }

            this.sectionsAffectedByLightUpdates.clear();
        }

    }

    public LightEngineStorage.b getDebugSectionType(long i) {
        return LightEngineStorage.a.type(this.sectionStates.get(i));
    }

    protected static class a {

        public static final byte EMPTY = 0;
        private static final int MIN_NEIGHBORS = 0;
        private static final int MAX_NEIGHBORS = 26;
        private static final byte HAS_DATA_BIT = 32;
        private static final byte NEIGHBOR_COUNT_BITS = 31;

        protected a() {}

        public static byte hasData(byte b0, boolean flag) {
            return (byte) (flag ? b0 | 32 : b0 & -33);
        }

        public static byte neighborCount(byte b0, int i) {
            if (i >= 0 && i <= 26) {
                return (byte) (b0 & -32 | i & 31);
            } else {
                throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
            }
        }

        public static boolean hasData(byte b0) {
            return (b0 & 32) != 0;
        }

        public static int neighborCount(byte b0) {
            return b0 & 31;
        }

        public static LightEngineStorage.b type(byte b0) {
            return b0 == 0 ? LightEngineStorage.b.EMPTY : (hasData(b0) ? LightEngineStorage.b.LIGHT_AND_DATA : LightEngineStorage.b.LIGHT_ONLY);
        }
    }

    public static enum b {

        EMPTY("2"), LIGHT_ONLY("1"), LIGHT_AND_DATA("0");

        private final String display;

        private b(String s) {
            this.display = s;
        }

        public String display() {
            return this.display;
        }
    }
}
