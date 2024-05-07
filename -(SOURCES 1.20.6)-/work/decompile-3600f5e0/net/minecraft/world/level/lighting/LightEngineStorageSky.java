package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.NibbleArray;

public class LightEngineStorageSky extends LightEngineStorage<LightEngineStorageSky.a> {

    protected LightEngineStorageSky(ILightAccess ilightaccess) {
        super(EnumSkyBlock.SKY, ilightaccess, new LightEngineStorageSky.a(new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
    }

    @Override
    protected int getLightValue(long i) {
        return this.getLightValue(i, false);
    }

    protected int getLightValue(long i, boolean flag) {
        long j = SectionPosition.blockToSection(i);
        int k = SectionPosition.y(j);
        LightEngineStorageSky.a lightenginestoragesky_a = flag ? (LightEngineStorageSky.a) this.updatingSectionData : (LightEngineStorageSky.a) this.visibleSectionData;
        int l = lightenginestoragesky_a.topSections.get(SectionPosition.getZeroNode(j));

        if (l != lightenginestoragesky_a.currentLowestY && k < l) {
            NibbleArray nibblearray = this.getDataLayer(lightenginestoragesky_a, j);

            if (nibblearray == null) {
                for (i = BlockPosition.getFlatIndex(i); nibblearray == null; nibblearray = this.getDataLayer(lightenginestoragesky_a, j)) {
                    ++k;
                    if (k >= l) {
                        return 15;
                    }

                    j = SectionPosition.offset(j, EnumDirection.UP);
                }
            }

            return nibblearray.get(SectionPosition.sectionRelative(BlockPosition.getX(i)), SectionPosition.sectionRelative(BlockPosition.getY(i)), SectionPosition.sectionRelative(BlockPosition.getZ(i)));
        } else {
            return flag && !this.lightOnInSection(j) ? 0 : 15;
        }
    }

    @Override
    protected void onNodeAdded(long i) {
        int j = SectionPosition.y(i);

        if (((LightEngineStorageSky.a) this.updatingSectionData).currentLowestY > j) {
            ((LightEngineStorageSky.a) this.updatingSectionData).currentLowestY = j;
            ((LightEngineStorageSky.a) this.updatingSectionData).topSections.defaultReturnValue(((LightEngineStorageSky.a) this.updatingSectionData).currentLowestY);
        }

        long k = SectionPosition.getZeroNode(i);
        int l = ((LightEngineStorageSky.a) this.updatingSectionData).topSections.get(k);

        if (l < j + 1) {
            ((LightEngineStorageSky.a) this.updatingSectionData).topSections.put(k, j + 1);
        }

    }

    @Override
    protected void onNodeRemoved(long i) {
        long j = SectionPosition.getZeroNode(i);
        int k = SectionPosition.y(i);

        if (((LightEngineStorageSky.a) this.updatingSectionData).topSections.get(j) == k + 1) {
            long l;

            for (l = i; !this.storingLightForSection(l) && this.hasLightDataAtOrBelow(k); l = SectionPosition.offset(l, EnumDirection.DOWN)) {
                --k;
            }

            if (this.storingLightForSection(l)) {
                ((LightEngineStorageSky.a) this.updatingSectionData).topSections.put(j, k + 1);
            } else {
                ((LightEngineStorageSky.a) this.updatingSectionData).topSections.remove(j);
            }
        }

    }

    @Override
    protected NibbleArray createDataLayer(long i) {
        NibbleArray nibblearray = (NibbleArray) this.queuedSections.get(i);

        if (nibblearray != null) {
            return nibblearray;
        } else {
            int j = ((LightEngineStorageSky.a) this.updatingSectionData).topSections.get(SectionPosition.getZeroNode(i));

            if (j != ((LightEngineStorageSky.a) this.updatingSectionData).currentLowestY && SectionPosition.y(i) < j) {
                NibbleArray nibblearray1;

                for (long k = SectionPosition.offset(i, EnumDirection.UP); (nibblearray1 = this.getDataLayer(k, true)) == null; k = SectionPosition.offset(k, EnumDirection.UP)) {
                    ;
                }

                return repeatFirstLayer(nibblearray1);
            } else {
                return this.lightOnInSection(i) ? new NibbleArray(15) : new NibbleArray();
            }
        }
    }

    private static NibbleArray repeatFirstLayer(NibbleArray nibblearray) {
        if (nibblearray.isDefinitelyHomogenous()) {
            return nibblearray.copy();
        } else {
            byte[] abyte = nibblearray.getData();
            byte[] abyte1 = new byte[2048];

            for (int i = 0; i < 16; ++i) {
                System.arraycopy(abyte, 0, abyte1, i * 128, 128);
            }

            return new NibbleArray(abyte1);
        }
    }

    protected boolean hasLightDataAtOrBelow(int i) {
        return i >= ((LightEngineStorageSky.a) this.updatingSectionData).currentLowestY;
    }

    protected boolean isAboveData(long i) {
        long j = SectionPosition.getZeroNode(i);
        int k = ((LightEngineStorageSky.a) this.updatingSectionData).topSections.get(j);

        return k == ((LightEngineStorageSky.a) this.updatingSectionData).currentLowestY || SectionPosition.y(i) >= k;
    }

    protected int getTopSectionY(long i) {
        return ((LightEngineStorageSky.a) this.updatingSectionData).topSections.get(i);
    }

    protected int getBottomSectionY() {
        return ((LightEngineStorageSky.a) this.updatingSectionData).currentLowestY;
    }

    protected static final class a extends LightEngineStorageArray<LightEngineStorageSky.a> {

        int currentLowestY;
        final Long2IntOpenHashMap topSections;

        public a(Long2ObjectOpenHashMap<NibbleArray> long2objectopenhashmap, Long2IntOpenHashMap long2intopenhashmap, int i) {
            super(long2objectopenhashmap);
            this.topSections = long2intopenhashmap;
            long2intopenhashmap.defaultReturnValue(i);
            this.currentLowestY = i;
        }

        @Override
        public LightEngineStorageSky.a copy() {
            return new LightEngineStorageSky.a(this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }
    }
}
