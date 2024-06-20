package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBase;

public class BlockSkullPlayer extends BlockSkull {

    public static final MapCodec<BlockSkullPlayer> CODEC = simpleCodec(BlockSkullPlayer::new);

    @Override
    public MapCodec<BlockSkullPlayer> codec() {
        return BlockSkullPlayer.CODEC;
    }

    protected BlockSkullPlayer(BlockBase.Info blockbase_info) {
        super(BlockSkull.Type.PLAYER, blockbase_info);
    }
}
