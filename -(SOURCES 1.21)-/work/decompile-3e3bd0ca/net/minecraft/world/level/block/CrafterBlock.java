package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.BlockPropertyJigsawOrientation;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.dispenser.DispenseBehaviorItem;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeCrafting;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class CrafterBlock extends BlockTileEntity {

    public static final MapCodec<CrafterBlock> CODEC = simpleCodec(CrafterBlock::new);
    public static final BlockStateBoolean CRAFTING = BlockProperties.CRAFTING;
    public static final BlockStateBoolean TRIGGERED = BlockProperties.TRIGGERED;
    private static final BlockStateEnum<BlockPropertyJigsawOrientation> ORIENTATION = BlockProperties.ORIENTATION;
    private static final int MAX_CRAFTING_TICKS = 6;
    private static final int CRAFTING_TICK_DELAY = 4;
    private static final RecipeCache RECIPE_CACHE = new RecipeCache(10);
    private static final int CRAFTER_ADVANCEMENT_DIAMETER = 17;

    public CrafterBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(CrafterBlock.ORIENTATION, BlockPropertyJigsawOrientation.NORTH_UP)).setValue(CrafterBlock.TRIGGERED, false)).setValue(CrafterBlock.CRAFTING, false));
    }

    @Override
    protected MapCodec<CrafterBlock> codec() {
        return CrafterBlock.CODEC;
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof CrafterBlockEntity crafterblockentity) {
            return crafterblockentity.getRedstoneSignal();
        } else {
            return 0;
        }
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        boolean flag1 = world.hasNeighborSignal(blockposition);
        boolean flag2 = (Boolean) iblockdata.getValue(CrafterBlock.TRIGGERED);
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (flag1 && !flag2) {
            world.scheduleTick(blockposition, (Block) this, 4);
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CrafterBlock.TRIGGERED, true), 2);
            this.setBlockEntityTriggered(tileentity, true);
        } else if (!flag1 && flag2) {
            world.setBlock(blockposition, (IBlockData) ((IBlockData) iblockdata.setValue(CrafterBlock.TRIGGERED, false)).setValue(CrafterBlock.CRAFTING, false), 2);
            this.setBlockEntityTriggered(tileentity, false);
        }

    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        this.dispenseFrom(iblockdata, worldserver, blockposition);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return world.isClientSide ? null : createTickerHelper(tileentitytypes, TileEntityTypes.CRAFTER, CrafterBlockEntity::serverTick);
    }

    private void setBlockEntityTriggered(@Nullable TileEntity tileentity, boolean flag) {
        if (tileentity instanceof CrafterBlockEntity crafterblockentity) {
            crafterblockentity.setTriggered(flag);
        }

    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        CrafterBlockEntity crafterblockentity = new CrafterBlockEntity(blockposition, iblockdata);

        crafterblockentity.setTriggered(iblockdata.hasProperty(CrafterBlock.TRIGGERED) && (Boolean) iblockdata.getValue(CrafterBlock.TRIGGERED));
        return crafterblockentity;
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        EnumDirection enumdirection = blockactioncontext.getNearestLookingDirection().getOpposite();
        EnumDirection enumdirection1;

        switch (enumdirection) {
            case DOWN:
                enumdirection1 = blockactioncontext.getHorizontalDirection().getOpposite();
                break;
            case UP:
                enumdirection1 = blockactioncontext.getHorizontalDirection();
                break;
            case NORTH:
            case SOUTH:
            case WEST:
            case EAST:
                enumdirection1 = EnumDirection.UP;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        EnumDirection enumdirection2 = enumdirection1;

        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(CrafterBlock.ORIENTATION, BlockPropertyJigsawOrientation.fromFrontAndTop(enumdirection, enumdirection2))).setValue(CrafterBlock.TRIGGERED, blockactioncontext.getLevel().hasNeighborSignal(blockactioncontext.getClickedPos()));
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        if ((Boolean) iblockdata.getValue(CrafterBlock.TRIGGERED)) {
            world.scheduleTick(blockposition, (Block) this, 4);
        }

    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        InventoryUtils.dropContentsOnDestroy(iblockdata, iblockdata1, world, blockposition);
        super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof CrafterBlockEntity) {
                entityhuman.openMenu((CrafterBlockEntity) tileentity);
            }

            return EnumInteractionResult.CONSUME;
        }
    }

    protected void dispenseFrom(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition) {
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);

        if (tileentity instanceof CrafterBlockEntity crafterblockentity) {
            CraftingInput craftinginput = crafterblockentity.asCraftInput();
            Optional optional = getPotentialResults(worldserver, craftinginput);

            if (optional.isEmpty()) {
                worldserver.levelEvent(1050, blockposition, 0);
            } else {
                RecipeHolder<RecipeCrafting> recipeholder = (RecipeHolder) optional.get();
                ItemStack itemstack = ((RecipeCrafting) recipeholder.value()).assemble(craftinginput, worldserver.registryAccess());

                if (itemstack.isEmpty()) {
                    worldserver.levelEvent(1050, blockposition, 0);
                } else {
                    crafterblockentity.setCraftingTicksRemaining(6);
                    worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(CrafterBlock.CRAFTING, true), 2);
                    itemstack.onCraftedBySystem(worldserver);
                    this.dispenseItem(worldserver, blockposition, crafterblockentity, itemstack, iblockdata, recipeholder);
                    Iterator iterator = ((RecipeCrafting) recipeholder.value()).getRemainingItems(craftinginput).iterator();

                    while (iterator.hasNext()) {
                        ItemStack itemstack1 = (ItemStack) iterator.next();

                        if (!itemstack1.isEmpty()) {
                            this.dispenseItem(worldserver, blockposition, crafterblockentity, itemstack1, iblockdata, recipeholder);
                        }
                    }

                    crafterblockentity.getItems().forEach((itemstack2) -> {
                        if (!itemstack2.isEmpty()) {
                            itemstack2.shrink(1);
                        }
                    });
                    crafterblockentity.setChanged();
                }
            }
        }
    }

    public static Optional<RecipeHolder<RecipeCrafting>> getPotentialResults(World world, CraftingInput craftinginput) {
        return CrafterBlock.RECIPE_CACHE.get(world, craftinginput);
    }

    private void dispenseItem(WorldServer worldserver, BlockPosition blockposition, CrafterBlockEntity crafterblockentity, ItemStack itemstack, IBlockData iblockdata, RecipeHolder<RecipeCrafting> recipeholder) {
        EnumDirection enumdirection = ((BlockPropertyJigsawOrientation) iblockdata.getValue(CrafterBlock.ORIENTATION)).front();
        IInventory iinventory = TileEntityHopper.getContainerAt(worldserver, blockposition.relative(enumdirection));
        ItemStack itemstack1 = itemstack.copy();

        if (iinventory != null && (iinventory instanceof CrafterBlockEntity || itemstack.getCount() > iinventory.getMaxStackSize(itemstack))) {
            while (!itemstack1.isEmpty()) {
                ItemStack itemstack2 = itemstack1.copyWithCount(1);
                ItemStack itemstack3 = TileEntityHopper.addItem(crafterblockentity, iinventory, itemstack2, enumdirection.getOpposite());

                if (!itemstack3.isEmpty()) {
                    break;
                }

                itemstack1.shrink(1);
            }
        } else if (iinventory != null) {
            while (!itemstack1.isEmpty()) {
                int i = itemstack1.getCount();

                itemstack1 = TileEntityHopper.addItem(crafterblockentity, iinventory, itemstack1, enumdirection.getOpposite());
                if (i == itemstack1.getCount()) {
                    break;
                }
            }
        }

        if (!itemstack1.isEmpty()) {
            Vec3D vec3d = Vec3D.atCenterOf(blockposition);
            Vec3D vec3d1 = vec3d.relative(enumdirection, 0.7D);

            DispenseBehaviorItem.spawnItem(worldserver, itemstack1, 6, enumdirection, vec3d1);
            Iterator iterator = worldserver.getEntitiesOfClass(EntityPlayer.class, AxisAlignedBB.ofSize(vec3d, 17.0D, 17.0D, 17.0D)).iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                CriterionTriggers.CRAFTER_RECIPE_CRAFTED.trigger(entityplayer, recipeholder.id(), crafterblockentity.getItems());
            }

            worldserver.levelEvent(1049, blockposition, 0);
            worldserver.levelEvent(2010, blockposition, enumdirection.get3DDataValue());
        }

    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(CrafterBlock.ORIENTATION, enumblockrotation.rotation().rotate((BlockPropertyJigsawOrientation) iblockdata.getValue(CrafterBlock.ORIENTATION)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return (IBlockData) iblockdata.setValue(CrafterBlock.ORIENTATION, enumblockmirror.rotation().rotate((BlockPropertyJigsawOrientation) iblockdata.getValue(CrafterBlock.ORIENTATION)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(CrafterBlock.ORIENTATION, CrafterBlock.TRIGGERED, CrafterBlock.CRAFTING);
    }
}
