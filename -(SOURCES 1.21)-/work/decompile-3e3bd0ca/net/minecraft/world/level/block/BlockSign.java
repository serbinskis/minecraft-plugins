package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyWood;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public abstract class BlockSign extends BlockTileEntity implements IBlockWaterlogged {

    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    protected static final float AABB_OFFSET = 4.0F;
    protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    private final BlockPropertyWood type;

    protected BlockSign(BlockPropertyWood blockpropertywood, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.type = blockpropertywood;
    }

    @Override
    protected abstract MapCodec<? extends BlockSign> codec();

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(BlockSign.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockSign.SHAPE;
    }

    @Override
    public boolean isPossibleToRespawnInThis(IBlockData iblockdata) {
        return true;
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntitySign(blockposition, iblockdata);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntitySign tileentitysign) {
            Item item = itemstack.getItem();
            SignApplicator signapplicator;

            if (item instanceof SignApplicator signapplicator1) {
                signapplicator = signapplicator1;
            } else {
                signapplicator = null;
            }

            SignApplicator signapplicator2 = signapplicator;
            boolean flag = signapplicator2 != null && entityhuman.mayBuild();

            if (!world.isClientSide) {
                if (flag && !tileentitysign.isWaxed() && !this.otherPlayerIsEditingSign(entityhuman, tileentitysign)) {
                    boolean flag1 = tileentitysign.isFacingFrontText(entityhuman);

                    if (signapplicator2.canApplyToSign(tileentitysign.getText(flag1), entityhuman) && signapplicator2.tryApplyToSign(world, tileentitysign, flag1, entityhuman)) {
                        tileentitysign.executeClickCommandsIfPresent(entityhuman, world, blockposition, flag1);
                        entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
                        world.gameEvent((Holder) GameEvent.BLOCK_CHANGE, tileentitysign.getBlockPos(), GameEvent.a.of(entityhuman, tileentitysign.getBlockState()));
                        itemstack.consume(1, entityhuman);
                        return ItemInteractionResult.SUCCESS;
                    } else {
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }
                } else {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            } else {
                return !flag && !tileentitysign.isWaxed() ? ItemInteractionResult.CONSUME : ItemInteractionResult.SUCCESS;
            }
        } else {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntitySign tileentitysign) {
            if (world.isClientSide) {
                SystemUtils.pauseInIde(new IllegalStateException("Expected to only call this on server"));
            }

            boolean flag = tileentitysign.isFacingFrontText(entityhuman);
            boolean flag1 = tileentitysign.executeClickCommandsIfPresent(entityhuman, world, blockposition, flag);

            if (tileentitysign.isWaxed()) {
                world.playSound((EntityHuman) null, tileentitysign.getBlockPos(), tileentitysign.getSignInteractionFailedSoundEvent(), SoundCategory.BLOCKS);
                return EnumInteractionResult.SUCCESS;
            } else if (flag1) {
                return EnumInteractionResult.SUCCESS;
            } else if (!this.otherPlayerIsEditingSign(entityhuman, tileentitysign) && entityhuman.mayBuild() && this.hasEditableText(entityhuman, tileentitysign, flag)) {
                this.openTextEdit(entityhuman, tileentitysign, flag);
                return EnumInteractionResult.SUCCESS;
            } else {
                return EnumInteractionResult.PASS;
            }
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    private boolean hasEditableText(EntityHuman entityhuman, TileEntitySign tileentitysign, boolean flag) {
        SignText signtext = tileentitysign.getText(flag);

        return Arrays.stream(signtext.getMessages(entityhuman.isTextFilteringEnabled())).allMatch((ichatbasecomponent) -> {
            return ichatbasecomponent.equals(CommonComponents.EMPTY) || ichatbasecomponent.getContents() instanceof LiteralContents;
        });
    }

    public abstract float getYRotationDegrees(IBlockData iblockdata);

    public Vec3D getSignHitboxCenterPosition(IBlockData iblockdata) {
        return new Vec3D(0.5D, 0.5D, 0.5D);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockSign.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    public BlockPropertyWood type() {
        return this.type;
    }

    public static BlockPropertyWood getWoodType(Block block) {
        BlockPropertyWood blockpropertywood;

        if (block instanceof BlockSign) {
            blockpropertywood = ((BlockSign) block).type();
        } else {
            blockpropertywood = BlockPropertyWood.OAK;
        }

        return blockpropertywood;
    }

    public void openTextEdit(EntityHuman entityhuman, TileEntitySign tileentitysign, boolean flag) {
        tileentitysign.setAllowedPlayerEditor(entityhuman.getUUID());
        entityhuman.openTextEdit(tileentitysign, flag);
    }

    private boolean otherPlayerIsEditingSign(EntityHuman entityhuman, TileEntitySign tileentitysign) {
        UUID uuid = tileentitysign.getPlayerWhoMayEdit();

        return uuid != null && !uuid.equals(entityhuman.getUUID());
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return createTickerHelper(tileentitytypes, TileEntityTypes.SIGN, TileEntitySign::tick);
    }
}
