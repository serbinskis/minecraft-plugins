package net.minecraft.world.level.block;

import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
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

    protected BlockSign(BlockBase.Info blockbase_info, BlockPropertyWood blockpropertywood) {
        super(blockbase_info);
        this.type = blockpropertywood;
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(BlockSign.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
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
    public EnumInteractionResult use(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        Item item = itemstack.getItem();
        Item item1 = itemstack.getItem();
        SignApplicator signapplicator;

        if (item1 instanceof SignApplicator) {
            SignApplicator signapplicator1 = (SignApplicator) item1;

            signapplicator = signapplicator1;
        } else {
            signapplicator = null;
        }

        SignApplicator signapplicator2 = signapplicator;
        boolean flag = signapplicator2 != null && entityhuman.mayBuild();
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntitySign) {
            TileEntitySign tileentitysign = (TileEntitySign) tileentity;

            if (!world.isClientSide) {
                boolean flag1 = tileentitysign.isFacingFrontText(entityhuman);
                SignText signtext = tileentitysign.getText(flag1);
                boolean flag2 = tileentitysign.executeClickCommandsIfPresent(entityhuman, world, blockposition, flag1);

                if (tileentitysign.isWaxed()) {
                    world.playSound((EntityHuman) null, tileentitysign.getBlockPos(), SoundEffects.WAXED_SIGN_INTERACT_FAIL, SoundCategory.BLOCKS);
                    return EnumInteractionResult.PASS;
                } else if (flag && !this.otherPlayerIsEditingSign(entityhuman, tileentitysign) && signapplicator2.canApplyToSign(signtext, entityhuman) && signapplicator2.tryApplyToSign(world, tileentitysign, flag1, entityhuman)) {
                    if (!entityhuman.isCreative()) {
                        itemstack.shrink(1);
                    }

                    world.gameEvent(GameEvent.BLOCK_CHANGE, tileentitysign.getBlockPos(), GameEvent.a.of(entityhuman, tileentitysign.getBlockState()));
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
                    return EnumInteractionResult.SUCCESS;
                } else if (flag2) {
                    return EnumInteractionResult.SUCCESS;
                } else if (!this.otherPlayerIsEditingSign(entityhuman, tileentitysign) && entityhuman.mayBuild() && this.hasEditableText(entityhuman, tileentitysign, flag1)) {
                    this.openTextEdit(entityhuman, tileentitysign, flag1);
                    return EnumInteractionResult.SUCCESS;
                } else {
                    return EnumInteractionResult.PASS;
                }
            } else {
                return !flag && !tileentitysign.isWaxed() ? EnumInteractionResult.CONSUME : EnumInteractionResult.SUCCESS;
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
    public Fluid getFluidState(IBlockData iblockdata) {
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
