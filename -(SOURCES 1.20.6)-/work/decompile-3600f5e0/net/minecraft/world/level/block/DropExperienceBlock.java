package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class DropExperienceBlock extends Block {

    public static final MapCodec<DropExperienceBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(IntProvider.codec(0, 10).fieldOf("experience").forGetter((dropexperienceblock) -> {
            return dropexperienceblock.xpRange;
        }), propertiesCodec()).apply(instance, DropExperienceBlock::new);
    });
    private final IntProvider xpRange;

    @Override
    public MapCodec<? extends DropExperienceBlock> codec() {
        return DropExperienceBlock.CODEC;
    }

    public DropExperienceBlock(IntProvider intprovider, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.xpRange = intprovider;
    }

    @Override
    protected void spawnAfterBreak(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
        super.spawnAfterBreak(iblockdata, worldserver, blockposition, itemstack, flag);
        if (flag) {
            this.tryDropExperience(worldserver, blockposition, itemstack, this.xpRange);
        }

    }
}
