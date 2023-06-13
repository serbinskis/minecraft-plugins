package net.minecraft.world.level.lighting;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.NibbleArray;
import org.jetbrains.annotations.VisibleForTesting;

public final class LightEngineSky extends LightEngine<LightEngineStorageSky.a, LightEngineStorageSky> {

    private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.a.decreaseAllDirections(15);
    private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.a.decreaseSkipOneDirection(15, EnumDirection.UP);
    private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.a.increaseSkipOneDirection(15, false, EnumDirection.UP);
    private final BlockPosition.MutableBlockPosition mutablePos;
    private final ChunkSkyLightSources emptyChunkSources;

    public LightEngineSky(ILightAccess ilightaccess) {
        this(ilightaccess, new LightEngineStorageSky(ilightaccess));
    }

    @VisibleForTesting
    protected LightEngineSky(ILightAccess ilightaccess, LightEngineStorageSky lightenginestoragesky) {
        super(ilightaccess, lightenginestoragesky);
        this.mutablePos = new BlockPosition.MutableBlockPosition();
        this.emptyChunkSources = new ChunkSkyLightSources(ilightaccess.getLevel());
    }

    private static boolean isSourceLevel(int i) {
        return i == 15;
    }

    private int getLowestSourceY(int i, int j, int k) {
        ChunkSkyLightSources chunkskylightsources = this.getChunkSources(SectionPosition.blockToSectionCoord(i), SectionPosition.blockToSectionCoord(j));

        return chunkskylightsources == null ? k : chunkskylightsources.getLowestSourceY(SectionPosition.sectionRelative(i), SectionPosition.sectionRelative(j));
    }

    @Nullable
    private ChunkSkyLightSources getChunkSources(int i, int j) {
        LightChunk lightchunk = this.chunkSource.getChunkForLighting(i, j);

        return lightchunk != null ? lightchunk.getSkyLightSources() : null;
    }

    @Override
    protected void checkNode(long i) {
        int j = BlockPosition.getX(i);
        int k = BlockPosition.getY(i);
        int l = BlockPosition.getZ(i);
        long i1 = SectionPosition.blockToSection(i);
        int j1 = ((LightEngineStorageSky) this.storage).lightOnInSection(i1) ? this.getLowestSourceY(j, l, Integer.MAX_VALUE) : Integer.MAX_VALUE;

        if (j1 != Integer.MAX_VALUE) {
            this.updateSourcesInColumn(j, l, j1);
        }

        if (((LightEngineStorageSky) this.storage).storingLightForSection(i1)) {
            boolean flag = k >= j1;

            if (flag) {
                this.enqueueDecrease(i, LightEngineSky.REMOVE_SKY_SOURCE_ENTRY);
                this.enqueueIncrease(i, LightEngineSky.ADD_SKY_SOURCE_ENTRY);
            } else {
                int k1 = ((LightEngineStorageSky) this.storage).getStoredLevel(i);

                if (k1 > 0) {
                    ((LightEngineStorageSky) this.storage).setStoredLevel(i, 0);
                    this.enqueueDecrease(i, LightEngine.a.decreaseAllDirections(k1));
                } else {
                    this.enqueueDecrease(i, LightEngineSky.PULL_LIGHT_IN_ENTRY);
                }
            }

        }
    }

    private void updateSourcesInColumn(int i, int j, int k) {
        int l = SectionPosition.sectionToBlockCoord(((LightEngineStorageSky) this.storage).getBottomSectionY());

        this.removeSourcesBelow(i, j, k, l);
        this.addSourcesAbove(i, j, k, l);
    }

    private void removeSourcesBelow(int i, int j, int k, int l) {
        if (k > l) {
            int i1 = SectionPosition.blockToSectionCoord(i);
            int j1 = SectionPosition.blockToSectionCoord(j);
            int k1 = k - 1;

            for (int l1 = SectionPosition.blockToSectionCoord(k1); ((LightEngineStorageSky) this.storage).hasLightDataAtOrBelow(l1); --l1) {
                if (((LightEngineStorageSky) this.storage).storingLightForSection(SectionPosition.asLong(i1, l1, j1))) {
                    int i2 = SectionPosition.sectionToBlockCoord(l1);
                    int j2 = i2 + 15;

                    for (int k2 = Math.min(j2, k1); k2 >= i2; --k2) {
                        long l2 = BlockPosition.asLong(i, k2, j);

                        if (!isSourceLevel(((LightEngineStorageSky) this.storage).getStoredLevel(l2))) {
                            return;
                        }

                        ((LightEngineStorageSky) this.storage).setStoredLevel(l2, 0);
                        this.enqueueDecrease(l2, k2 == k - 1 ? LightEngineSky.REMOVE_TOP_SKY_SOURCE_ENTRY : LightEngineSky.REMOVE_SKY_SOURCE_ENTRY);
                    }
                }
            }

        }
    }

    private void addSourcesAbove(int i, int j, int k, int l) {
        int i1 = SectionPosition.blockToSectionCoord(i);
        int j1 = SectionPosition.blockToSectionCoord(j);
        int k1 = Math.max(Math.max(this.getLowestSourceY(i - 1, j, Integer.MIN_VALUE), this.getLowestSourceY(i + 1, j, Integer.MIN_VALUE)), Math.max(this.getLowestSourceY(i, j - 1, Integer.MIN_VALUE), this.getLowestSourceY(i, j + 1, Integer.MIN_VALUE)));
        int l1 = Math.max(k, l);

        for (long i2 = SectionPosition.asLong(i1, SectionPosition.blockToSectionCoord(l1), j1); !((LightEngineStorageSky) this.storage).isAboveData(i2); i2 = SectionPosition.offset(i2, EnumDirection.UP)) {
            if (((LightEngineStorageSky) this.storage).storingLightForSection(i2)) {
                int j2 = SectionPosition.sectionToBlockCoord(SectionPosition.y(i2));
                int k2 = j2 + 15;

                for (int l2 = Math.max(j2, l1); l2 <= k2; ++l2) {
                    long i3 = BlockPosition.asLong(i, l2, j);

                    if (isSourceLevel(((LightEngineStorageSky) this.storage).getStoredLevel(i3))) {
                        return;
                    }

                    ((LightEngineStorageSky) this.storage).setStoredLevel(i3, 15);
                    if (l2 < k1 || l2 == k) {
                        this.enqueueIncrease(i3, LightEngineSky.ADD_SKY_SOURCE_ENTRY);
                    }
                }
            }
        }

    }

    @Override
    protected void propagateIncrease(long i, long j, int k) {
        IBlockData iblockdata = null;
        int l = this.countEmptySectionsBelowIfAtBorder(i);
        EnumDirection[] aenumdirection = LightEngineSky.PROPAGATION_DIRECTIONS;
        int i1 = aenumdirection.length;

        for (int j1 = 0; j1 < i1; ++j1) {
            EnumDirection enumdirection = aenumdirection[j1];

            if (LightEngine.a.shouldPropagateInDirection(j, enumdirection)) {
                long k1 = BlockPosition.offset(i, enumdirection);

                if (((LightEngineStorageSky) this.storage).storingLightForSection(SectionPosition.blockToSection(k1))) {
                    int l1 = ((LightEngineStorageSky) this.storage).getStoredLevel(k1);
                    int i2 = k - 1;

                    if (i2 > l1) {
                        this.mutablePos.set(k1);
                        IBlockData iblockdata1 = this.getState(this.mutablePos);
                        int j2 = k - this.getOpacity(iblockdata1, this.mutablePos);

                        if (j2 > l1) {
                            if (iblockdata == null) {
                                iblockdata = LightEngine.a.isFromEmptyShape(j) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(i));
                            }

                            if (!this.shapeOccludes(i, iblockdata, k1, iblockdata1, enumdirection)) {
                                ((LightEngineStorageSky) this.storage).setStoredLevel(k1, j2);
                                if (j2 > 1) {
                                    this.enqueueIncrease(k1, LightEngine.a.increaseSkipOneDirection(j2, isEmptyShape(iblockdata1), enumdirection.getOpposite()));
                                }

                                this.propagateFromEmptySections(k1, enumdirection, j2, true, l);
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void propagateDecrease(long i, long j) {
        int k = this.countEmptySectionsBelowIfAtBorder(i);
        int l = LightEngine.a.getFromLevel(j);
        EnumDirection[] aenumdirection = LightEngineSky.PROPAGATION_DIRECTIONS;
        int i1 = aenumdirection.length;

        for (int j1 = 0; j1 < i1; ++j1) {
            EnumDirection enumdirection = aenumdirection[j1];

            if (LightEngine.a.shouldPropagateInDirection(j, enumdirection)) {
                long k1 = BlockPosition.offset(i, enumdirection);

                if (((LightEngineStorageSky) this.storage).storingLightForSection(SectionPosition.blockToSection(k1))) {
                    int l1 = ((LightEngineStorageSky) this.storage).getStoredLevel(k1);

                    if (l1 != 0) {
                        if (l1 <= l - 1) {
                            ((LightEngineStorageSky) this.storage).setStoredLevel(k1, 0);
                            this.enqueueDecrease(k1, LightEngine.a.decreaseSkipOneDirection(l1, enumdirection.getOpposite()));
                            this.propagateFromEmptySections(k1, enumdirection, l1, false, k);
                        } else {
                            this.enqueueIncrease(k1, LightEngine.a.increaseOnlyOneDirection(l1, false, enumdirection.getOpposite()));
                        }
                    }
                }
            }
        }

    }

    private int countEmptySectionsBelowIfAtBorder(long i) {
        int j = BlockPosition.getY(i);
        int k = SectionPosition.sectionRelative(j);

        if (k != 0) {
            return 0;
        } else {
            int l = BlockPosition.getX(i);
            int i1 = BlockPosition.getZ(i);
            int j1 = SectionPosition.sectionRelative(l);
            int k1 = SectionPosition.sectionRelative(i1);

            if (j1 != 0 && j1 != 15 && k1 != 0 && k1 != 15) {
                return 0;
            } else {
                int l1 = SectionPosition.blockToSectionCoord(l);
                int i2 = SectionPosition.blockToSectionCoord(j);
                int j2 = SectionPosition.blockToSectionCoord(i1);

                int k2;

                for (k2 = 0; !((LightEngineStorageSky) this.storage).storingLightForSection(SectionPosition.asLong(l1, i2 - k2 - 1, j2)) && ((LightEngineStorageSky) this.storage).hasLightDataAtOrBelow(i2 - k2 - 1); ++k2) {
                    ;
                }

                return k2;
            }
        }
    }

    private void propagateFromEmptySections(long i, EnumDirection enumdirection, int j, boolean flag, int k) {
        if (k != 0) {
            int l = BlockPosition.getX(i);
            int i1 = BlockPosition.getZ(i);

            if (crossedSectionEdge(enumdirection, SectionPosition.sectionRelative(l), SectionPosition.sectionRelative(i1))) {
                int j1 = BlockPosition.getY(i);
                int k1 = SectionPosition.blockToSectionCoord(l);
                int l1 = SectionPosition.blockToSectionCoord(i1);
                int i2 = SectionPosition.blockToSectionCoord(j1) - 1;
                int j2 = i2 - k + 1;

                while (i2 >= j2) {
                    if (!((LightEngineStorageSky) this.storage).storingLightForSection(SectionPosition.asLong(k1, i2, l1))) {
                        --i2;
                    } else {
                        int k2 = SectionPosition.sectionToBlockCoord(i2);

                        for (int l2 = 15; l2 >= 0; --l2) {
                            long i3 = BlockPosition.asLong(l, k2 + l2, i1);

                            if (flag) {
                                ((LightEngineStorageSky) this.storage).setStoredLevel(i3, j);
                                if (j > 1) {
                                    this.enqueueIncrease(i3, LightEngine.a.increaseSkipOneDirection(j, true, enumdirection.getOpposite()));
                                }
                            } else {
                                ((LightEngineStorageSky) this.storage).setStoredLevel(i3, 0);
                                this.enqueueDecrease(i3, LightEngine.a.decreaseSkipOneDirection(j, enumdirection.getOpposite()));
                            }
                        }

                        --i2;
                    }
                }

            }
        }
    }

    private static boolean crossedSectionEdge(EnumDirection enumdirection, int i, int j) {
        boolean flag;

        switch (enumdirection) {
            case NORTH:
                flag = j == 15;
                break;
            case SOUTH:
                flag = j == 0;
                break;
            case WEST:
                flag = i == 15;
                break;
            case EAST:
                flag = i == 0;
                break;
            default:
                flag = false;
        }

        return flag;
    }

    @Override
    public void setLightEnabled(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        super.setLightEnabled(chunkcoordintpair, flag);
        if (flag) {
            ChunkSkyLightSources chunkskylightsources = (ChunkSkyLightSources) Objects.requireNonNullElse(this.getChunkSources(chunkcoordintpair.x, chunkcoordintpair.z), this.emptyChunkSources);
            int i = chunkskylightsources.getHighestLowestSourceY() - 1;
            int j = SectionPosition.blockToSectionCoord(i) + 1;
            long k = SectionPosition.getZeroNode(chunkcoordintpair.x, chunkcoordintpair.z);
            int l = ((LightEngineStorageSky) this.storage).getTopSectionY(k);
            int i1 = Math.max(((LightEngineStorageSky) this.storage).getBottomSectionY(), j);

            for (int j1 = l - 1; j1 >= i1; --j1) {
                NibbleArray nibblearray = ((LightEngineStorageSky) this.storage).getDataLayerToWrite(SectionPosition.asLong(chunkcoordintpair.x, j1, chunkcoordintpair.z));

                if (nibblearray != null && nibblearray.isEmpty()) {
                    nibblearray.fill(15);
                }
            }
        }

    }

    @Override
    public void propagateLightSources(ChunkCoordIntPair chunkcoordintpair) {
        long i = SectionPosition.getZeroNode(chunkcoordintpair.x, chunkcoordintpair.z);

        ((LightEngineStorageSky) this.storage).setLightEnabled(i, true);
        ChunkSkyLightSources chunkskylightsources = (ChunkSkyLightSources) Objects.requireNonNullElse(this.getChunkSources(chunkcoordintpair.x, chunkcoordintpair.z), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources1 = (ChunkSkyLightSources) Objects.requireNonNullElse(this.getChunkSources(chunkcoordintpair.x, chunkcoordintpair.z - 1), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources2 = (ChunkSkyLightSources) Objects.requireNonNullElse(this.getChunkSources(chunkcoordintpair.x, chunkcoordintpair.z + 1), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources3 = (ChunkSkyLightSources) Objects.requireNonNullElse(this.getChunkSources(chunkcoordintpair.x - 1, chunkcoordintpair.z), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources4 = (ChunkSkyLightSources) Objects.requireNonNullElse(this.getChunkSources(chunkcoordintpair.x + 1, chunkcoordintpair.z), this.emptyChunkSources);
        int j = ((LightEngineStorageSky) this.storage).getTopSectionY(i);
        int k = ((LightEngineStorageSky) this.storage).getBottomSectionY();
        int l = SectionPosition.sectionToBlockCoord(chunkcoordintpair.x);
        int i1 = SectionPosition.sectionToBlockCoord(chunkcoordintpair.z);

        for (int j1 = j - 1; j1 >= k; --j1) {
            long k1 = SectionPosition.asLong(chunkcoordintpair.x, j1, chunkcoordintpair.z);
            NibbleArray nibblearray = ((LightEngineStorageSky) this.storage).getDataLayerToWrite(k1);

            if (nibblearray != null) {
                int l1 = SectionPosition.sectionToBlockCoord(j1);
                int i2 = l1 + 15;
                boolean flag = false;

                for (int j2 = 0; j2 < 16; ++j2) {
                    for (int k2 = 0; k2 < 16; ++k2) {
                        int l2 = chunkskylightsources.getLowestSourceY(k2, j2);

                        if (l2 <= i2) {
                            int i3 = j2 == 0 ? chunkskylightsources1.getLowestSourceY(k2, 15) : chunkskylightsources.getLowestSourceY(k2, j2 - 1);
                            int j3 = j2 == 15 ? chunkskylightsources2.getLowestSourceY(k2, 0) : chunkskylightsources.getLowestSourceY(k2, j2 + 1);
                            int k3 = k2 == 0 ? chunkskylightsources3.getLowestSourceY(15, j2) : chunkskylightsources.getLowestSourceY(k2 - 1, j2);
                            int l3 = k2 == 15 ? chunkskylightsources4.getLowestSourceY(0, j2) : chunkskylightsources.getLowestSourceY(k2 + 1, j2);
                            int i4 = Math.max(Math.max(i3, j3), Math.max(k3, l3));

                            for (int j4 = i2; j4 >= Math.max(l1, l2); --j4) {
                                nibblearray.set(k2, SectionPosition.sectionRelative(j4), j2, 15);
                                if (j4 == l2 || j4 < i4) {
                                    long k4 = BlockPosition.asLong(l + k2, j4, i1 + j2);

                                    this.enqueueIncrease(k4, LightEngine.a.increaseSkySourceInDirections(j4 == l2, j4 < i3, j4 < j3, j4 < k3, j4 < l3));
                                }
                            }

                            if (l2 < l1) {
                                flag = true;
                            }
                        }
                    }
                }

                if (!flag) {
                    break;
                }
            }
        }

    }
}
