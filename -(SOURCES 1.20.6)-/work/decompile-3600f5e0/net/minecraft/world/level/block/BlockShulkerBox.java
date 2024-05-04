package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.EntityShulker;
import net.minecraft.world.entity.monster.piglin.PiglinAI;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityShulkerBox;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockShulkerBox extends BlockTileEntity {

    public static final MapCodec<BlockShulkerBox> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnumColor.CODEC.optionalFieldOf("color").forGetter((blockshulkerbox) -> {
            return Optional.ofNullable(blockshulkerbox.color);
        }), propertiesCodec()).apply(instance, (optional, blockbase_info) -> {
            return new BlockShulkerBox((EnumColor) optional.orElse((Object) null), blockbase_info);
        });
    });
    private static final IChatBaseComponent UNKNOWN_CONTENTS = IChatBaseComponent.translatable("container.shulkerBox.unknownContents");
    private static final float OPEN_AABB_SIZE = 1.0F;
    private static final VoxelShape UP_OPEN_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape DOWN_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    private static final VoxelShape WES_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_OPEN_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    private static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    private static final Map<EnumDirection, VoxelShape> OPEN_SHAPE_BY_DIRECTION = (Map) SystemUtils.make(Maps.newEnumMap(EnumDirection.class), (enummap) -> {
        enummap.put(EnumDirection.NORTH, BlockShulkerBox.NORTH_OPEN_AABB);
        enummap.put(EnumDirection.EAST, BlockShulkerBox.EAST_OPEN_AABB);
        enummap.put(EnumDirection.SOUTH, BlockShulkerBox.SOUTH_OPEN_AABB);
        enummap.put(EnumDirection.WEST, BlockShulkerBox.WES_OPEN_AABB);
        enummap.put(EnumDirection.UP, BlockShulkerBox.UP_OPEN_AABB);
        enummap.put(EnumDirection.DOWN, BlockShulkerBox.DOWN_OPEN_AABB);
    });
    public static final BlockStateEnum<EnumDirection> FACING = BlockDirectional.FACING;
    public static final MinecraftKey CONTENTS = new MinecraftKey("contents");
    @Nullable
    public final EnumColor color;

    @Override
    public MapCodec<BlockShulkerBox> codec() {
        return BlockShulkerBox.CODEC;
    }

    public BlockShulkerBox(@Nullable EnumColor enumcolor, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.color = enumcolor;
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockShulkerBox.FACING, EnumDirection.UP));
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityShulkerBox(this.color, blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return createTickerHelper(tileentitytypes, TileEntityTypes.SHULKER_BOX, TileEntityShulkerBox::tick);
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else if (entityhuman.isSpectator()) {
            return EnumInteractionResult.CONSUME;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityShulkerBox) {
                TileEntityShulkerBox tileentityshulkerbox = (TileEntityShulkerBox) tileentity;

                if (canOpen(iblockdata, world, blockposition, tileentityshulkerbox)) {
                    entityhuman.openMenu(tileentityshulkerbox);
                    entityhuman.awardStat(StatisticList.OPEN_SHULKER_BOX);
                    PiglinAI.angerNearbyPiglins(entityhuman, true);
                }

                return EnumInteractionResult.CONSUME;
            } else {
                return EnumInteractionResult.PASS;
            }
        }
    }

    private static boolean canOpen(IBlockData iblockdata, World world, BlockPosition blockposition, TileEntityShulkerBox tileentityshulkerbox) {
        if (tileentityshulkerbox.getAnimationStatus() != TileEntityShulkerBox.AnimationPhase.CLOSED) {
            return true;
        } else {
            AxisAlignedBB axisalignedbb = EntityShulker.getProgressDeltaAabb(1.0F, (EnumDirection) iblockdata.getValue(BlockShulkerBox.FACING), 0.0F, 0.5F).move(blockposition).deflate(1.0E-6D);

            return world.noCollision(axisalignedbb);
        }
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockShulkerBox.FACING, blockactioncontext.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockShulkerBox.FACING);
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityShulkerBox tileentityshulkerbox) {
            if (!world.isClientSide && entityhuman.isCreative() && !tileentityshulkerbox.isEmpty()) {
                ItemStack itemstack = getColoredItemStack(this.getColor());

                itemstack.applyComponents(tileentity.collectComponents());
                EntityItem entityitem = new EntityItem(world, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, itemstack);

                entityitem.setDefaultPickUpDelay();
                world.addFreshEntity(entityitem);
            } else {
                tileentityshulkerbox.unpackLootTable(entityhuman);
            }
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    @Override
    protected List<ItemStack> getDrops(IBlockData iblockdata, LootParams.a lootparams_a) {
        TileEntity tileentity = (TileEntity) lootparams_a.getOptionalParameter(LootContextParameters.BLOCK_ENTITY);

        if (tileentity instanceof TileEntityShulkerBox tileentityshulkerbox) {
            lootparams_a = lootparams_a.withDynamicDrop(BlockShulkerBox.CONTENTS, (consumer) -> {
                for (int i = 0; i < tileentityshulkerbox.getContainerSize(); ++i) {
                    consumer.accept(tileentityshulkerbox.getItem(i));
                }

            });
        }

        return super.getDrops(iblockdata, lootparams_a);
    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata.is(iblockdata1.getBlock())) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityShulkerBox) {
                world.updateNeighbourForOutputSignal(blockposition, iblockdata.getBlock());
            }

            super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        if (itemstack.has(DataComponents.CONTAINER_LOOT)) {
            list.add(BlockShulkerBox.UNKNOWN_CONTENTS);
        }

        int i = 0;
        int j = 0;
        Iterator iterator = ((ItemContainerContents) itemstack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).nonEmptyItems().iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack1 = (ItemStack) iterator.next();

            ++j;
            if (i <= 4) {
                ++i;
                list.add(IChatBaseComponent.translatable("container.shulkerBox.itemCount", itemstack1.getHoverName(), itemstack1.getCount()));
            }
        }

        if (j - i > 0) {
            list.add(IChatBaseComponent.translatable("container.shulkerBox.more", j - i).withStyle(EnumChatFormat.ITALIC));
        }

    }

    @Override
    protected VoxelShape getBlockSupportShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityShulkerBox tileentityshulkerbox) {
            if (!tileentityshulkerbox.isClosed()) {
                return (VoxelShape) BlockShulkerBox.OPEN_SHAPE_BY_DIRECTION.get(((EnumDirection) iblockdata.getValue(BlockShulkerBox.FACING)).getOpposite());
            }
        }

        return VoxelShapes.block();
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        return tileentity instanceof TileEntityShulkerBox ? VoxelShapes.create(((TileEntityShulkerBox) tileentity).getBoundingBox(iblockdata)) : VoxelShapes.block();
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return false;
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(blockposition));
    }

    @Override
    public ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        ItemStack itemstack = super.getCloneItemStack(iworldreader, blockposition, iblockdata);

        iworldreader.getBlockEntity(blockposition, TileEntityTypes.SHULKER_BOX).ifPresent((tileentityshulkerbox) -> {
            tileentityshulkerbox.saveToItem(itemstack, iworldreader.registryAccess());
        });
        return itemstack;
    }

    @Nullable
    public static EnumColor getColorFromItem(Item item) {
        return getColorFromBlock(Block.byItem(item));
    }

    @Nullable
    public static EnumColor getColorFromBlock(Block block) {
        return block instanceof BlockShulkerBox ? ((BlockShulkerBox) block).getColor() : null;
    }

    public static Block getBlockByColor(@Nullable EnumColor enumcolor) {
        if (enumcolor == null) {
            return Blocks.SHULKER_BOX;
        } else {
            Block block;

            switch (enumcolor) {
                case WHITE:
                    block = Blocks.WHITE_SHULKER_BOX;
                    break;
                case ORANGE:
                    block = Blocks.ORANGE_SHULKER_BOX;
                    break;
                case MAGENTA:
                    block = Blocks.MAGENTA_SHULKER_BOX;
                    break;
                case LIGHT_BLUE:
                    block = Blocks.LIGHT_BLUE_SHULKER_BOX;
                    break;
                case YELLOW:
                    block = Blocks.YELLOW_SHULKER_BOX;
                    break;
                case LIME:
                    block = Blocks.LIME_SHULKER_BOX;
                    break;
                case PINK:
                    block = Blocks.PINK_SHULKER_BOX;
                    break;
                case GRAY:
                    block = Blocks.GRAY_SHULKER_BOX;
                    break;
                case LIGHT_GRAY:
                    block = Blocks.LIGHT_GRAY_SHULKER_BOX;
                    break;
                case CYAN:
                    block = Blocks.CYAN_SHULKER_BOX;
                    break;
                case BLUE:
                    block = Blocks.BLUE_SHULKER_BOX;
                    break;
                case BROWN:
                    block = Blocks.BROWN_SHULKER_BOX;
                    break;
                case GREEN:
                    block = Blocks.GREEN_SHULKER_BOX;
                    break;
                case RED:
                    block = Blocks.RED_SHULKER_BOX;
                    break;
                case BLACK:
                    block = Blocks.BLACK_SHULKER_BOX;
                    break;
                case PURPLE:
                    block = Blocks.PURPLE_SHULKER_BOX;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return block;
        }
    }

    @Nullable
    public EnumColor getColor() {
        return this.color;
    }

    public static ItemStack getColoredItemStack(@Nullable EnumColor enumcolor) {
        return new ItemStack(getBlockByColor(enumcolor));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockShulkerBox.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockShulkerBox.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockShulkerBox.FACING)));
    }
}
