package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBase;

public class BlockSkullPlayerWall extends BlockSkullWall {

    public static final MapCodec<BlockSkullPlayerWall> CODEC = simpleCodec(BlockSkullPlayerWall::new);

    @Override
    public MapCodec<BlockSkullPlayerWall> codec() {
        return BlockSkullPlayerWall.CODEC;
    }

    protected BlockSkullPlayerWall(BlockBase.Info blockbase_info) {
        super(BlockSkull.Type.PLAYER, blockbase_info);
    }
}
