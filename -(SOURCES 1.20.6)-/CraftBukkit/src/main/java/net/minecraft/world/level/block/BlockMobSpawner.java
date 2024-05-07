package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockMobSpawner extends BlockTileEntity {

    public static final MapCodec<BlockMobSpawner> CODEC = simpleCodec(BlockMobSpawner::new);

    @Override
    public MapCodec<BlockMobSpawner> codec() {
        return BlockMobSpawner.CODEC;
    }

    protected BlockMobSpawner(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityMobSpawner(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return createTickerHelper(tileentitytypes, TileEntityTypes.MOB_SPAWNER, world.isClientSide ? TileEntityMobSpawner::clientTick : TileEntityMobSpawner::serverTick);
    }

    @Override
    protected void spawnAfterBreak(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
        super.spawnAfterBreak(iblockdata, worldserver, blockposition, itemstack, flag);
        // CraftBukkit start - Delegate to getExpDrop
    }

    @Override
    public int getExpDrop(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
        if (flag) {
            int i = 15 + worldserver.random.nextInt(15) + worldserver.random.nextInt(15);

            // this.popExperience(worldserver, blockposition, i);
            return i;
        }

        return 0;
        // CraftBukkit end
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        Spawner.appendHoverText(itemstack, list, "SpawnData");
    }
}
