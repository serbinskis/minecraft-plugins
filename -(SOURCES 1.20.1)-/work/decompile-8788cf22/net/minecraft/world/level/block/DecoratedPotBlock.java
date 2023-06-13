package net.minecraft.world.level.block;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock extends BlockTileEntity implements IBlockWaterlogged {

    public static final MinecraftKey SHERDS_DYNAMIC_DROP_ID = new MinecraftKey("sherds");
    private static final VoxelShape BOUNDING_BOX = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final BlockStateDirection HORIZONTAL_FACING = BlockProperties.HORIZONTAL_FACING;
    private static final BlockStateBoolean CRACKED = BlockProperties.CRACKED;
    private static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;

    protected DecoratedPotBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(DecoratedPotBlock.HORIZONTAL_FACING, EnumDirection.NORTH)).setValue(DecoratedPotBlock.WATERLOGGED, false)).setValue(DecoratedPotBlock.CRACKED, false));
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(DecoratedPotBlock.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        return (IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(DecoratedPotBlock.HORIZONTAL_FACING, blockactioncontext.getHorizontalDirection())).setValue(DecoratedPotBlock.WATERLOGGED, fluid.getType() == FluidTypes.WATER)).setValue(DecoratedPotBlock.CRACKED, false);
    }

    @Override
    public boolean isPathfindable(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, PathMode pathmode) {
        return false;
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return DecoratedPotBlock.BOUNDING_BOX;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(DecoratedPotBlock.HORIZONTAL_FACING, DecoratedPotBlock.WATERLOGGED, DecoratedPotBlock.CRACKED);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new DecoratedPotBlockEntity(blockposition, iblockdata);
    }

    @Override
    public List<ItemStack> getDrops(IBlockData iblockdata, LootParams.a lootparams_a) {
        TileEntity tileentity = (TileEntity) lootparams_a.getOptionalParameter(LootContextParameters.BLOCK_ENTITY);

        if (tileentity instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity decoratedpotblockentity = (DecoratedPotBlockEntity) tileentity;

            lootparams_a.withDynamicDrop(DecoratedPotBlock.SHERDS_DYNAMIC_DROP_ID, (consumer) -> {
                decoratedpotblockentity.getDecorations().sorted().map(Item::getDefaultInstance).forEach(consumer);
            });
        }

        return super.getDrops(iblockdata, lootparams_a);
    }

    @Override
    public void playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        ItemStack itemstack = entityhuman.getMainHandItem();
        IBlockData iblockdata1 = iblockdata;

        if (itemstack.is(TagsItem.BREAKS_DECORATED_POTS) && !EnchantmentManager.hasSilkTouch(itemstack)) {
            iblockdata1 = (IBlockData) iblockdata.setValue(DecoratedPotBlock.CRACKED, true);
            world.setBlock(blockposition, iblockdata1, 4);
        }

        super.playerWillDestroy(world, blockposition, iblockdata1, entityhuman);
    }

    @Override
    public Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(DecoratedPotBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    public SoundEffectType getSoundType(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(DecoratedPotBlock.CRACKED) ? SoundEffectType.DECORATED_POT_CRACKED : SoundEffectType.DECORATED_POT;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable IBlockAccess iblockaccess, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, iblockaccess, list, tooltipflag);
        DecoratedPotBlockEntity.a decoratedpotblockentity_a = DecoratedPotBlockEntity.a.load(ItemBlock.getBlockEntityData(itemstack));

        if (!decoratedpotblockentity_a.equals(DecoratedPotBlockEntity.a.EMPTY)) {
            list.add(CommonComponents.EMPTY);
            Stream.of(decoratedpotblockentity_a.front(), decoratedpotblockentity_a.left(), decoratedpotblockentity_a.right(), decoratedpotblockentity_a.back()).forEach((item) -> {
                list.add((new ItemStack(item, 1)).getHoverName().plainCopy().withStyle(EnumChatFormat.GRAY));
            });
        }
    }
}
