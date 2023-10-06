package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.core.dispenser.DispenseBehaviorItem;
import net.minecraft.core.dispenser.IDispenseBehavior;
import net.minecraft.core.dispenser.SourceBlock;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityDispenser;
import net.minecraft.world.level.block.entity.TileEntityDropper;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import org.slf4j.Logger;

public class BlockDispenser extends BlockTileEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BlockStateDirection FACING = BlockDirectional.FACING;
    public static final BlockStateBoolean TRIGGERED = BlockProperties.TRIGGERED;
    public static final Map<Item, IDispenseBehavior> DISPENSER_REGISTRY = (Map) SystemUtils.make(new Object2ObjectOpenHashMap(), (object2objectopenhashmap) -> {
        object2objectopenhashmap.defaultReturnValue(new DispenseBehaviorItem());
    });
    private static final int TRIGGER_DURATION = 4;

    public static void registerBehavior(IMaterial imaterial, IDispenseBehavior idispensebehavior) {
        BlockDispenser.DISPENSER_REGISTRY.put(imaterial.asItem(), idispensebehavior);
    }

    protected BlockDispenser(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockDispenser.FACING, EnumDirection.NORTH)).setValue(BlockDispenser.TRIGGERED, false));
    }

    @Override
    public EnumInteractionResult use(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityDispenser) {
                entityhuman.openMenu((TileEntityDispenser) tileentity);
                if (tileentity instanceof TileEntityDropper) {
                    entityhuman.awardStat(StatisticList.INSPECT_DROPPER);
                } else {
                    entityhuman.awardStat(StatisticList.INSPECT_DISPENSER);
                }
            }

            return EnumInteractionResult.CONSUME;
        }
    }

    public void dispenseFrom(WorldServer worldserver, IBlockData iblockdata, BlockPosition blockposition) {
        TileEntityDispenser tileentitydispenser = (TileEntityDispenser) worldserver.getBlockEntity(blockposition, TileEntityTypes.DISPENSER).orElse((Object) null);

        if (tileentitydispenser == null) {
            BlockDispenser.LOGGER.warn("Ignoring dispensing attempt for Dispenser without matching block entity at {}", blockposition);
        } else {
            SourceBlock sourceblock = new SourceBlock(worldserver, blockposition, iblockdata, tileentitydispenser);
            int i = tileentitydispenser.getRandomSlot(worldserver.random);

            if (i < 0) {
                worldserver.levelEvent(1001, blockposition, 0);
                worldserver.gameEvent(GameEvent.BLOCK_ACTIVATE, blockposition, GameEvent.a.of(tileentitydispenser.getBlockState()));
            } else {
                ItemStack itemstack = tileentitydispenser.getItem(i);
                IDispenseBehavior idispensebehavior = this.getDispenseMethod(itemstack);

                if (idispensebehavior != IDispenseBehavior.NOOP) {
                    tileentitydispenser.setItem(i, idispensebehavior.dispense(sourceblock, itemstack));
                }

            }
        }
    }

    protected IDispenseBehavior getDispenseMethod(ItemStack itemstack) {
        return (IDispenseBehavior) BlockDispenser.DISPENSER_REGISTRY.get(itemstack.getItem());
    }

    @Override
    public void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        boolean flag1 = world.hasNeighborSignal(blockposition) || world.hasNeighborSignal(blockposition.above());
        boolean flag2 = (Boolean) iblockdata.getValue(BlockDispenser.TRIGGERED);

        if (flag1 && !flag2) {
            world.scheduleTick(blockposition, (Block) this, 4);
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockDispenser.TRIGGERED, true), 2);
        } else if (!flag1 && flag2) {
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockDispenser.TRIGGERED, false), 2);
        }

    }

    @Override
    public void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        this.dispenseFrom(worldserver, iblockdata, blockposition);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityDispenser(blockposition, iblockdata);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockDispenser.FACING, blockactioncontext.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        if (itemstack.hasCustomHoverName()) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityDispenser) {
                ((TileEntityDispenser) tileentity).setCustomName(itemstack.getHoverName());
            }
        }

    }

    @Override
    public void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata.is(iblockdata1.getBlock())) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityDispenser) {
                InventoryUtils.dropContents(world, blockposition, (IInventory) ((TileEntityDispenser) tileentity));
                world.updateNeighbourForOutputSignal(blockposition, this);
            }

            super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
        }
    }

    public static IPosition getDispensePosition(SourceBlock sourceblock) {
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);

        return sourceblock.center().add(0.7D * (double) enumdirection.getStepX(), 0.7D * (double) enumdirection.getStepY(), 0.7D * (double) enumdirection.getStepZ());
    }

    @Override
    public boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(blockposition));
    }

    @Override
    public EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockDispenser.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockDispenser.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockDispenser.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockDispenser.FACING, BlockDispenser.TRIGGERED);
    }
}
