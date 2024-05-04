package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class VaultBlock extends BlockTileEntity {

    public static final MapCodec<VaultBlock> CODEC = simpleCodec(VaultBlock::new);
    public static final IBlockState<VaultState> STATE = BlockProperties.VAULT_STATE;
    public static final BlockStateDirection FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateBoolean OMINOUS = BlockProperties.OMINOUS;

    @Override
    public MapCodec<VaultBlock> codec() {
        return VaultBlock.CODEC;
    }

    public VaultBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(VaultBlock.FACING, EnumDirection.NORTH)).setValue(VaultBlock.STATE, VaultState.INACTIVE)).setValue(VaultBlock.OMINOUS, false));
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!itemstack.isEmpty() && iblockdata.getValue(VaultBlock.STATE) == VaultState.ACTIVE) {
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;
                TileEntity tileentity = worldserver.getBlockEntity(blockposition);

                if (tileentity instanceof VaultBlockEntity) {
                    VaultBlockEntity vaultblockentity = (VaultBlockEntity) tileentity;

                    VaultBlockEntity.b.tryInsertKey(worldserver, blockposition, iblockdata, vaultblockentity.getConfig(), vaultblockentity.getServerData(), vaultblockentity.getSharedData(), entityhuman, itemstack);
                    return ItemInteractionResult.SUCCESS;
                } else {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            } else {
                return ItemInteractionResult.CONSUME;
            }
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new VaultBlockEntity(blockposition, iblockdata);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(VaultBlock.FACING, VaultBlock.STATE, VaultBlock.OMINOUS);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        BlockEntityTicker blockentityticker;

        if (world instanceof WorldServer worldserver) {
            blockentityticker = createTickerHelper(tileentitytypes, TileEntityTypes.VAULT, (world1, blockposition, iblockdata1, vaultblockentity) -> {
                VaultBlockEntity.b.tick(worldserver, blockposition, iblockdata1, vaultblockentity.getConfig(), vaultblockentity.getServerData(), vaultblockentity.getSharedData());
            });
        } else {
            blockentityticker = createTickerHelper(tileentitytypes, TileEntityTypes.VAULT, (world1, blockposition, iblockdata1, vaultblockentity) -> {
                VaultBlockEntity.a.tick(world1, blockposition, iblockdata1, vaultblockentity.getClientData(), vaultblockentity.getSharedData());
            });
        }

        return blockentityticker;
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(VaultBlock.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(VaultBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(VaultBlock.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(VaultBlock.FACING)));
    }

    @Override
    public EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }
}
