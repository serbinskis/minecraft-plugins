package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockIce extends BlockHalfTransparent {

    public static final MapCodec<BlockIce> CODEC = simpleCodec(BlockIce::new);

    @Override
    public MapCodec<? extends BlockIce> codec() {
        return BlockIce.CODEC;
    }

    public BlockIce(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    public static IBlockData meltsInto() {
        return Blocks.WATER.defaultBlockState();
    }

    @Override
    public void playerDestroy(World world, EntityHuman entityhuman, BlockPosition blockposition, IBlockData iblockdata, @Nullable TileEntity tileentity, ItemStack itemstack) {
        super.playerDestroy(world, entityhuman, blockposition, iblockdata, tileentity, itemstack);
        if (EnchantmentManager.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
            if (world.dimensionType().ultraWarm()) {
                world.removeBlock(blockposition, false);
                return;
            }

            IBlockData iblockdata1 = world.getBlockState(blockposition.below());

            if (iblockdata1.blocksMotion() || iblockdata1.liquid()) {
                world.setBlockAndUpdate(blockposition, meltsInto());
            }
        }

    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (worldserver.getBrightness(EnumSkyBlock.BLOCK, blockposition) > 11 - iblockdata.getLightBlock(worldserver, blockposition)) {
            this.melt(iblockdata, worldserver, blockposition);
        }

    }

    protected void melt(IBlockData iblockdata, World world, BlockPosition blockposition) {
        if (world.dimensionType().ultraWarm()) {
            world.removeBlock(blockposition, false);
        } else {
            world.setBlockAndUpdate(blockposition, meltsInto());
            world.neighborChanged(blockposition, meltsInto().getBlock(), blockposition);
        }
    }
}
