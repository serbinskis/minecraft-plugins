package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockWitherSkullWall extends BlockSkullWall {

    public static final MapCodec<BlockWitherSkullWall> CODEC = simpleCodec(BlockWitherSkullWall::new);

    @Override
    public MapCodec<BlockWitherSkullWall> codec() {
        return BlockWitherSkullWall.CODEC;
    }

    protected BlockWitherSkullWall(BlockBase.Info blockbase_info) {
        super(BlockSkull.Type.WITHER_SKELETON, blockbase_info);
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        BlockWitherSkull.checkSpawn(world, blockposition);
    }
}
