package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.dispenser.DispenseBehaviorItem;
import net.minecraft.core.dispenser.IDispenseBehavior;
import net.minecraft.core.dispenser.SourceBlock;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityDispenser;
import net.minecraft.world.level.block.entity.TileEntityDropper;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import org.slf4j.Logger;

// CraftBukkit start
import net.minecraft.world.InventoryLargeChest;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
// CraftBukkit end

public class BlockDropper extends BlockDispenser {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<BlockDropper> CODEC = simpleCodec(BlockDropper::new);
    private static final IDispenseBehavior DISPENSE_BEHAVIOUR = new DispenseBehaviorItem(true); // CraftBukkit

    @Override
    public MapCodec<BlockDropper> codec() {
        return BlockDropper.CODEC;
    }

    public BlockDropper(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected IDispenseBehavior getDispenseMethod(World world, ItemStack itemstack) {
        return BlockDropper.DISPENSE_BEHAVIOUR;
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityDropper(blockposition, iblockdata);
    }

    @Override
    public void dispenseFrom(WorldServer worldserver, IBlockData iblockdata, BlockPosition blockposition) {
        TileEntityDispenser tileentitydispenser = (TileEntityDispenser) worldserver.getBlockEntity(blockposition, TileEntityTypes.DROPPER).orElse(null); // CraftBukkit - decompile error

        if (tileentitydispenser == null) {
            BlockDropper.LOGGER.warn("Ignoring dispensing attempt for Dropper without matching block entity at {}", blockposition);
        } else {
            SourceBlock sourceblock = new SourceBlock(worldserver, blockposition, iblockdata, tileentitydispenser);
            int i = tileentitydispenser.getRandomSlot(worldserver.random);

            if (i < 0) {
                worldserver.levelEvent(1001, blockposition, 0);
            } else {
                ItemStack itemstack = tileentitydispenser.getItem(i);

                if (!itemstack.isEmpty()) {
                    EnumDirection enumdirection = (EnumDirection) worldserver.getBlockState(blockposition).getValue(BlockDropper.FACING);
                    IInventory iinventory = TileEntityHopper.getContainerAt(worldserver, blockposition.relative(enumdirection));
                    ItemStack itemstack1;

                    if (iinventory == null) {
                        itemstack1 = BlockDropper.DISPENSE_BEHAVIOUR.dispense(sourceblock, itemstack);
                    } else {
                        // CraftBukkit start - Fire event when pushing items into other inventories
                        CraftItemStack oitemstack = CraftItemStack.asCraftMirror(itemstack.copy().split(1));

                        org.bukkit.inventory.Inventory destinationInventory;
                        // Have to special case large chests as they work oddly
                        if (iinventory instanceof InventoryLargeChest) {
                            destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
                        } else {
                            destinationInventory = iinventory.getOwner().getInventory();
                        }

                        InventoryMoveItemEvent event = new InventoryMoveItemEvent(tileentitydispenser.getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
                        worldserver.getCraftServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            return;
                        }
                        itemstack1 = TileEntityHopper.addItem(tileentitydispenser, iinventory, CraftItemStack.asNMSCopy(event.getItem()), enumdirection.getOpposite());
                        if (event.getItem().equals(oitemstack) && itemstack1.isEmpty()) {
                            // CraftBukkit end
                            itemstack1 = itemstack.copy();
                            itemstack1.shrink(1);
                        } else {
                            itemstack1 = itemstack.copy();
                        }
                    }

                    tileentitydispenser.setItem(i, itemstack1);
                }
            }
        }
    }
}
