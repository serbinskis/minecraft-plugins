package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.SpawnerCreature;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathMode;

public interface SpawnPlacementTypes {

    SpawnPlacementType NO_RESTRICTIONS = (iworldreader, blockposition, entitytypes) -> {
        return true;
    };
    SpawnPlacementType IN_WATER = (iworldreader, blockposition, entitytypes) -> {
        if (entitytypes != null && iworldreader.getWorldBorder().isWithinBounds(blockposition)) {
            BlockPosition blockposition1 = blockposition.above();

            return iworldreader.getFluidState(blockposition).is(TagsFluid.WATER) && !iworldreader.getBlockState(blockposition1).isRedstoneConductor(iworldreader, blockposition1);
        } else {
            return false;
        }
    };
    SpawnPlacementType IN_LAVA = (iworldreader, blockposition, entitytypes) -> {
        return entitytypes != null && iworldreader.getWorldBorder().isWithinBounds(blockposition) ? iworldreader.getFluidState(blockposition).is(TagsFluid.LAVA) : false;
    };
    SpawnPlacementType ON_GROUND = new SpawnPlacementType() {
        @Override
        public boolean isSpawnPositionOk(IWorldReader iworldreader, BlockPosition blockposition, @Nullable EntityTypes<?> entitytypes) {
            if (entitytypes != null && iworldreader.getWorldBorder().isWithinBounds(blockposition)) {
                BlockPosition blockposition1 = blockposition.above();
                BlockPosition blockposition2 = blockposition.below();
                IBlockData iblockdata = iworldreader.getBlockState(blockposition2);

                return !iblockdata.isValidSpawn(iworldreader, blockposition2, entitytypes) ? false : this.isValidEmptySpawnBlock(iworldreader, blockposition, entitytypes) && this.isValidEmptySpawnBlock(iworldreader, blockposition1, entitytypes);
            } else {
                return false;
            }
        }

        private boolean isValidEmptySpawnBlock(IWorldReader iworldreader, BlockPosition blockposition, EntityTypes<?> entitytypes) {
            IBlockData iblockdata = iworldreader.getBlockState(blockposition);

            return SpawnerCreature.isValidEmptySpawnBlock(iworldreader, blockposition, iblockdata, iblockdata.getFluidState(), entitytypes);
        }

        @Override
        public BlockPosition adjustSpawnPosition(IWorldReader iworldreader, BlockPosition blockposition) {
            BlockPosition blockposition1 = blockposition.below();

            return iworldreader.getBlockState(blockposition1).isPathfindable(PathMode.LAND) ? blockposition1 : blockposition;
        }
    };
}
