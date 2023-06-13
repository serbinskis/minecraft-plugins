package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootParams;

public class BlockSkullPlayerWall extends BlockSkullWall {

    protected BlockSkullPlayerWall(BlockBase.Info blockbase_info) {
        super(BlockSkull.Type.PLAYER, blockbase_info);
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        Blocks.PLAYER_HEAD.setPlacedBy(world, blockposition, iblockdata, entityliving, itemstack);
    }

    @Override
    public List<ItemStack> getDrops(IBlockData iblockdata, LootParams.a lootparams_a) {
        return Blocks.PLAYER_HEAD.getDrops(iblockdata, lootparams_a);
    }
}
