package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockSign;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.state.IBlockData;

public class ItemSign extends ItemBlockWallable {

    public ItemSign(Item.Info item_info, Block block, Block block1) {
        super(block, block1, item_info, EnumDirection.DOWN);
    }

    public ItemSign(Item.Info item_info, Block block, Block block1, EnumDirection enumdirection) {
        super(block, block1, item_info, enumdirection);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPosition blockposition, World world, @Nullable EntityHuman entityhuman, ItemStack itemstack, IBlockData iblockdata) {
        boolean flag = super.updateCustomBlockEntityTag(blockposition, world, entityhuman, itemstack, iblockdata);

        if (!world.isClientSide && !flag && entityhuman != null) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntitySign) {
                TileEntitySign tileentitysign = (TileEntitySign) tileentity;
                Block block = world.getBlockState(blockposition).getBlock();

                if (block instanceof BlockSign) {
                    BlockSign blocksign = (BlockSign) block;

                    blocksign.openTextEdit(entityhuman, tileentitysign, true);
                }
            }
        }

        return flag;
    }
}
