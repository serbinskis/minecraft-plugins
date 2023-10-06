package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.LightChunk;

public final class LightEngineBlock extends LightEngine<LightEngineStorageBlock.a, LightEngineStorageBlock> {

    private final BlockPosition.MutableBlockPosition mutablePos;

    public LightEngineBlock(ILightAccess ilightaccess) {
        this(ilightaccess, new LightEngineStorageBlock(ilightaccess));
    }

    @VisibleForTesting
    public LightEngineBlock(ILightAccess ilightaccess, LightEngineStorageBlock lightenginestorageblock) {
        super(ilightaccess, lightenginestorageblock);
        this.mutablePos = new BlockPosition.MutableBlockPosition();
    }

    @Override
    protected void checkNode(long i) {
        long j = SectionPosition.blockToSection(i);

        if (((LightEngineStorageBlock) this.storage).storingLightForSection(j)) {
            IBlockData iblockdata = this.getState(this.mutablePos.set(i));
            int k = this.getEmission(i, iblockdata);
            int l = ((LightEngineStorageBlock) this.storage).getStoredLevel(i);

            if (k < l) {
                ((LightEngineStorageBlock) this.storage).setStoredLevel(i, 0);
                this.enqueueDecrease(i, LightEngine.a.decreaseAllDirections(l));
            } else {
                this.enqueueDecrease(i, LightEngineBlock.PULL_LIGHT_IN_ENTRY);
            }

            if (k > 0) {
                this.enqueueIncrease(i, LightEngine.a.increaseLightFromEmission(k, isEmptyShape(iblockdata)));
            }

        }
    }

    @Override
    protected void propagateIncrease(long i, long j, int k) {
        IBlockData iblockdata = null;
        EnumDirection[] aenumdirection = LightEngineBlock.PROPAGATION_DIRECTIONS;
        int l = aenumdirection.length;

        for (int i1 = 0; i1 < l; ++i1) {
            EnumDirection enumdirection = aenumdirection[i1];

            if (LightEngine.a.shouldPropagateInDirection(j, enumdirection)) {
                long j1 = BlockPosition.offset(i, enumdirection);

                if (((LightEngineStorageBlock) this.storage).storingLightForSection(SectionPosition.blockToSection(j1))) {
                    int k1 = ((LightEngineStorageBlock) this.storage).getStoredLevel(j1);
                    int l1 = k - 1;

                    if (l1 > k1) {
                        this.mutablePos.set(j1);
                        IBlockData iblockdata1 = this.getState(this.mutablePos);
                        int i2 = k - this.getOpacity(iblockdata1, this.mutablePos);

                        if (i2 > k1) {
                            if (iblockdata == null) {
                                iblockdata = LightEngine.a.isFromEmptyShape(j) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(i));
                            }

                            if (!this.shapeOccludes(i, iblockdata, j1, iblockdata1, enumdirection)) {
                                ((LightEngineStorageBlock) this.storage).setStoredLevel(j1, i2);
                                if (i2 > 1) {
                                    this.enqueueIncrease(j1, LightEngine.a.increaseSkipOneDirection(i2, isEmptyShape(iblockdata1), enumdirection.getOpposite()));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void propagateDecrease(long i, long j) {
        int k = LightEngine.a.getFromLevel(j);
        EnumDirection[] aenumdirection = LightEngineBlock.PROPAGATION_DIRECTIONS;
        int l = aenumdirection.length;

        for (int i1 = 0; i1 < l; ++i1) {
            EnumDirection enumdirection = aenumdirection[i1];

            if (LightEngine.a.shouldPropagateInDirection(j, enumdirection)) {
                long j1 = BlockPosition.offset(i, enumdirection);

                if (((LightEngineStorageBlock) this.storage).storingLightForSection(SectionPosition.blockToSection(j1))) {
                    int k1 = ((LightEngineStorageBlock) this.storage).getStoredLevel(j1);

                    if (k1 != 0) {
                        if (k1 <= k - 1) {
                            IBlockData iblockdata = this.getState(this.mutablePos.set(j1));
                            int l1 = this.getEmission(j1, iblockdata);

                            ((LightEngineStorageBlock) this.storage).setStoredLevel(j1, 0);
                            if (l1 < k1) {
                                this.enqueueDecrease(j1, LightEngine.a.decreaseSkipOneDirection(k1, enumdirection.getOpposite()));
                            }

                            if (l1 > 0) {
                                this.enqueueIncrease(j1, LightEngine.a.increaseLightFromEmission(l1, isEmptyShape(iblockdata)));
                            }
                        } else {
                            this.enqueueIncrease(j1, LightEngine.a.increaseOnlyOneDirection(k1, false, enumdirection.getOpposite()));
                        }
                    }
                }
            }
        }

    }

    private int getEmission(long i, IBlockData iblockdata) {
        int j = iblockdata.getLightEmission();

        return j > 0 && ((LightEngineStorageBlock) this.storage).lightOnInSection(SectionPosition.blockToSection(i)) ? j : 0;
    }

    @Override
    public void propagateLightSources(ChunkCoordIntPair chunkcoordintpair) {
        this.setLightEnabled(chunkcoordintpair, true);
        LightChunk lightchunk = this.chunkSource.getChunkForLighting(chunkcoordintpair.x, chunkcoordintpair.z);

        if (lightchunk != null) {
            lightchunk.findBlockLightSources((blockposition, iblockdata) -> {
                int i = iblockdata.getLightEmission();

                this.enqueueIncrease(blockposition.asLong(), LightEngine.a.increaseLightFromEmission(i, isEmptyShape(iblockdata)));
            });
        }

    }
}
