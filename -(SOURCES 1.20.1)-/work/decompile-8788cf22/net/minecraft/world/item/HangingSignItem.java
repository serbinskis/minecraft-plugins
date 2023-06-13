package net.minecraft.world.item;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.IBlockData;

public class HangingSignItem extends ItemSign {

    public HangingSignItem(Block block, Block block1, Item.Info item_info) {
        super(item_info, block, block1, EnumDirection.UP);
    }

    @Override
    protected boolean canPlace(IWorldReader iworldreader, IBlockData iblockdata, BlockPosition blockposition) {
        Block block = iblockdata.getBlock();

        if (block instanceof WallHangingSignBlock) {
            WallHangingSignBlock wallhangingsignblock = (WallHangingSignBlock) block;

            if (!wallhangingsignblock.canPlace(iblockdata, iworldreader, blockposition)) {
                return false;
            }
        }

        return super.canPlace(iworldreader, iblockdata, blockposition);
    }
}
