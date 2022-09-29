package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.state.IBlockData;

public class ItemSign extends ItemBlockWallable {

    public static BlockPosition openSign; // CraftBukkit

    public ItemSign(Item.Info item_info, Block block, Block block1) {
        super(block, block1, item_info);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPosition blockposition, World world, @Nullable EntityHuman entityhuman, ItemStack itemstack, IBlockData iblockdata) {
        boolean flag = super.updateCustomBlockEntityTag(blockposition, world, entityhuman, itemstack, iblockdata);

        if (!world.isClientSide && !flag && entityhuman != null) {
            // CraftBukkit start - SPIGOT-4678
            // entityhuman.openTextEdit((TileEntitySign) world.getBlockEntity(blockposition));
            ItemSign.openSign = blockposition;
            // CraftBukkit end
        }

        return flag;
    }
}
