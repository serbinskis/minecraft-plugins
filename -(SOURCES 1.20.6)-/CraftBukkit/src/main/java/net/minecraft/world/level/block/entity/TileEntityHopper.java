package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.IInventoryHolder;
import net.minecraft.world.IWorldInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerHopper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockChest;
import net.minecraft.world.level.block.BlockHopper;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;

// CraftBukkit start
import net.minecraft.world.InventoryLargeChest;
import net.minecraft.world.entity.vehicle.EntityMinecartHopper;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
// CraftBukkit end

public class TileEntityHopper extends TileEntityLootable implements IHopper {

    public static final int MOVE_ITEM_SPEED = 8;
    public static final int HOPPER_CONTAINER_SIZE = 5;
    private static final int[][] CACHED_SLOTS = new int[54][];
    private NonNullList<ItemStack> items;
    private int cooldownTime;
    private long tickedGameTime;
    private EnumDirection facing;

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public List<ItemStack> getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }
    // CraftBukkit end

    public TileEntityHopper(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.HOPPER, blockposition, iblockdata);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        this.cooldownTime = -1;
        this.facing = (EnumDirection) iblockdata.getValue(BlockHopper.FACING);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(nbttagcompound)) {
            ContainerUtil.loadAllItems(nbttagcompound, this.items, holderlookup_a);
        }

        this.cooldownTime = nbttagcompound.getInt("TransferCooldown");
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        if (!this.trySaveLootTable(nbttagcompound)) {
            ContainerUtil.saveAllItems(nbttagcompound, this.items, holderlookup_a);
        }

        nbttagcompound.putInt("TransferCooldown", this.cooldownTime);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        this.unpackLootTable((EntityHuman) null);
        return ContainerUtil.removeItem(this.getItems(), i, j);
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.unpackLootTable((EntityHuman) null);
        this.getItems().set(i, itemstack);
        itemstack.limitSize(this.getMaxStackSize(itemstack));
    }

    @Override
    public void setBlockState(IBlockData iblockdata) {
        super.setBlockState(iblockdata);
        this.facing = (EnumDirection) iblockdata.getValue(BlockHopper.FACING);
    }

    @Override
    protected IChatBaseComponent getDefaultName() {
        return IChatBaseComponent.translatable("container.hopper");
    }

    public static void pushItemsTick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityHopper tileentityhopper) {
        --tileentityhopper.cooldownTime;
        tileentityhopper.tickedGameTime = world.getGameTime();
        if (!tileentityhopper.isOnCooldown()) {
            tileentityhopper.setCooldown(0);
            tryMoveItems(world, blockposition, iblockdata, tileentityhopper, () -> {
                return suckInItems(world, tileentityhopper);
            });
        }

    }

    private static boolean tryMoveItems(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityHopper tileentityhopper, BooleanSupplier booleansupplier) {
        if (world.isClientSide) {
            return false;
        } else {
            if (!tileentityhopper.isOnCooldown() && (Boolean) iblockdata.getValue(BlockHopper.ENABLED)) {
                boolean flag = false;

                if (!tileentityhopper.isEmpty()) {
                    flag = ejectItems(world, blockposition, tileentityhopper);
                }

                if (!tileentityhopper.inventoryFull()) {
                    flag |= booleansupplier.getAsBoolean();
                }

                if (flag) {
                    tileentityhopper.setCooldown(8);
                    setChanged(world, blockposition, iblockdata);
                    return true;
                }
            }

            return false;
        }
    }

    private boolean inventoryFull() {
        Iterator iterator = this.items.iterator();

        ItemStack itemstack;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            itemstack = (ItemStack) iterator.next();
        } while (!itemstack.isEmpty() && itemstack.getCount() == itemstack.getMaxStackSize());

        return false;
    }

    private static boolean ejectItems(World world, BlockPosition blockposition, TileEntityHopper tileentityhopper) {
        IInventory iinventory = getAttachedContainer(world, blockposition, tileentityhopper);

        if (iinventory == null) {
            return false;
        } else {
            EnumDirection enumdirection = tileentityhopper.facing.getOpposite();

            if (isFullContainer(iinventory, enumdirection)) {
                return false;
            } else {
                for (int i = 0; i < tileentityhopper.getContainerSize(); ++i) {
                    ItemStack itemstack = tileentityhopper.getItem(i);

                    if (!itemstack.isEmpty()) {
                        int j = itemstack.getCount();
                        // CraftBukkit start - Call event when pushing items into other inventories
                        CraftItemStack oitemstack = CraftItemStack.asCraftMirror(tileentityhopper.removeItem(i, 1));

                        Inventory destinationInventory;
                        // Have to special case large chests as they work oddly
                        if (iinventory instanceof InventoryLargeChest) {
                            destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
                        } else if (iinventory.getOwner() != null) {
                            destinationInventory = iinventory.getOwner().getInventory();
                        } else {
                            destinationInventory = new CraftInventory(iinventory);
                        }

                        InventoryMoveItemEvent event = new InventoryMoveItemEvent(tileentityhopper.getOwner().getInventory(), oitemstack, destinationInventory, true);
                        world.getCraftServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            tileentityhopper.setItem(i, itemstack);
                            tileentityhopper.setCooldown(8); // Delay hopper checks
                            return false;
                        }
                        ItemStack itemstack1 = addItem(tileentityhopper, iinventory, CraftItemStack.asNMSCopy(event.getItem()), enumdirection);
                        // CraftBukkit end

                        if (itemstack1.isEmpty()) {
                            iinventory.setChanged();
                            return true;
                        }

                        itemstack.setCount(j);
                        if (j == 1) {
                            tileentityhopper.setItem(i, itemstack);
                        }
                    }
                }

                return false;
            }
        }
    }

    private static int[] getSlots(IInventory iinventory, EnumDirection enumdirection) {
        if (iinventory instanceof IWorldInventory iworldinventory) {
            return iworldinventory.getSlotsForFace(enumdirection);
        } else {
            int i = iinventory.getContainerSize();

            if (i < TileEntityHopper.CACHED_SLOTS.length) {
                int[] aint = TileEntityHopper.CACHED_SLOTS[i];

                if (aint != null) {
                    return aint;
                } else {
                    int[] aint1 = createFlatSlots(i);

                    TileEntityHopper.CACHED_SLOTS[i] = aint1;
                    return aint1;
                }
            } else {
                return createFlatSlots(i);
            }
        }
    }

    private static int[] createFlatSlots(int i) {
        int[] aint = new int[i];

        for (int j = 0; j < aint.length; aint[j] = j++) {
            ;
        }

        return aint;
    }

    private static boolean isFullContainer(IInventory iinventory, EnumDirection enumdirection) {
        int[] aint = getSlots(iinventory, enumdirection);
        int[] aint1 = aint;
        int i = aint.length;

        for (int j = 0; j < i; ++j) {
            int k = aint1[j];
            ItemStack itemstack = iinventory.getItem(k);

            if (itemstack.getCount() < itemstack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    public static boolean suckInItems(World world, IHopper ihopper) {
        BlockPosition blockposition = BlockPosition.containing(ihopper.getLevelX(), ihopper.getLevelY() + 1.0D, ihopper.getLevelZ());
        IBlockData iblockdata = world.getBlockState(blockposition);
        IInventory iinventory = getSourceContainer(world, ihopper, blockposition, iblockdata);

        if (iinventory != null) {
            EnumDirection enumdirection = EnumDirection.DOWN;
            int[] aint = getSlots(iinventory, enumdirection);
            int i = aint.length;

            for (int j = 0; j < i; ++j) {
                int k = aint[j];

                if (tryTakeInItemFromSlot(ihopper, iinventory, k, enumdirection)) {
                    return true;
                }
            }

            return false;
        } else {
            boolean flag = ihopper.isGridAligned() && iblockdata.isCollisionShapeFullBlock(world, blockposition) && !iblockdata.is(TagsBlock.DOES_NOT_BLOCK_HOPPERS);

            if (!flag) {
                Iterator iterator = getItemsAtAndAbove(world, ihopper).iterator();

                while (iterator.hasNext()) {
                    EntityItem entityitem = (EntityItem) iterator.next();

                    if (addItem(ihopper, entityitem)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static boolean tryTakeInItemFromSlot(IHopper ihopper, IInventory iinventory, int i, EnumDirection enumdirection) {
        ItemStack itemstack = iinventory.getItem(i);

        if (!itemstack.isEmpty() && canTakeItemFromContainer(ihopper, iinventory, itemstack, i, enumdirection)) {
            int j = itemstack.getCount();
            // CraftBukkit start - Call event on collection of items from inventories into the hopper
            CraftItemStack oitemstack = CraftItemStack.asCraftMirror(iinventory.removeItem(i, 1));

            Inventory sourceInventory;
            // Have to special case large chests as they work oddly
            if (iinventory instanceof InventoryLargeChest) {
                sourceInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
            } else if (iinventory.getOwner() != null) {
                sourceInventory = iinventory.getOwner().getInventory();
            } else {
                sourceInventory = new CraftInventory(iinventory);
            }

            InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack, ihopper.getOwner().getInventory(), false);

            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                iinventory.setItem(i, itemstack);

                if (ihopper instanceof TileEntityHopper) {
                    ((TileEntityHopper) ihopper).setCooldown(8); // Delay hopper checks
                }

                return false;
            }
            ItemStack itemstack1 = addItem(iinventory, ihopper, CraftItemStack.asNMSCopy(event.getItem()), null);
            // CraftBukkit end

            if (itemstack1.isEmpty()) {
                iinventory.setChanged();
                return true;
            }

            itemstack.setCount(j);
            if (j == 1) {
                iinventory.setItem(i, itemstack);
            }
        }

        return false;
    }

    public static boolean addItem(IInventory iinventory, EntityItem entityitem) {
        boolean flag = false;
        // CraftBukkit start
        InventoryPickupItemEvent event = new InventoryPickupItemEvent(iinventory.getOwner().getInventory(), (org.bukkit.entity.Item) entityitem.getBukkitEntity());
        entityitem.level().getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        // CraftBukkit end
        ItemStack itemstack = entityitem.getItem().copy();
        ItemStack itemstack1 = addItem((IInventory) null, iinventory, itemstack, (EnumDirection) null);

        if (itemstack1.isEmpty()) {
            flag = true;
            entityitem.setItem(ItemStack.EMPTY);
            entityitem.discard(EntityRemoveEvent.Cause.PICKUP); // CraftBukkit - add Bukkit remove cause
        } else {
            entityitem.setItem(itemstack1);
        }

        return flag;
    }

    public static ItemStack addItem(@Nullable IInventory iinventory, IInventory iinventory1, ItemStack itemstack, @Nullable EnumDirection enumdirection) {
        int i;

        if (iinventory1 instanceof IWorldInventory iworldinventory) {
            if (enumdirection != null) {
                int[] aint = iworldinventory.getSlotsForFace(enumdirection);

                for (i = 0; i < aint.length && !itemstack.isEmpty(); ++i) {
                    itemstack = tryMoveInItem(iinventory, iinventory1, itemstack, aint[i], enumdirection);
                }

                return itemstack;
            }
        }

        int j = iinventory1.getContainerSize();

        for (i = 0; i < j && !itemstack.isEmpty(); ++i) {
            itemstack = tryMoveInItem(iinventory, iinventory1, itemstack, i, enumdirection);
        }

        return itemstack;
    }

    private static boolean canPlaceItemInContainer(IInventory iinventory, ItemStack itemstack, int i, @Nullable EnumDirection enumdirection) {
        if (!iinventory.canPlaceItem(i, itemstack)) {
            return false;
        } else {
            boolean flag;

            if (iinventory instanceof IWorldInventory) {
                IWorldInventory iworldinventory = (IWorldInventory) iinventory;

                if (!iworldinventory.canPlaceItemThroughFace(i, itemstack, enumdirection)) {
                    flag = false;
                    return flag;
                }
            }

            flag = true;
            return flag;
        }
    }

    private static boolean canTakeItemFromContainer(IInventory iinventory, IInventory iinventory1, ItemStack itemstack, int i, EnumDirection enumdirection) {
        if (!iinventory1.canTakeItem(iinventory, i, itemstack)) {
            return false;
        } else {
            boolean flag;

            if (iinventory1 instanceof IWorldInventory) {
                IWorldInventory iworldinventory = (IWorldInventory) iinventory1;

                if (!iworldinventory.canTakeItemThroughFace(i, itemstack, enumdirection)) {
                    flag = false;
                    return flag;
                }
            }

            flag = true;
            return flag;
        }
    }

    private static ItemStack tryMoveInItem(@Nullable IInventory iinventory, IInventory iinventory1, ItemStack itemstack, int i, @Nullable EnumDirection enumdirection) {
        ItemStack itemstack1 = iinventory1.getItem(i);

        if (canPlaceItemInContainer(iinventory1, itemstack, i, enumdirection)) {
            boolean flag = false;
            boolean flag1 = iinventory1.isEmpty();

            if (itemstack1.isEmpty()) {
                iinventory1.setItem(i, itemstack);
                itemstack = ItemStack.EMPTY;
                flag = true;
            } else if (canMergeItems(itemstack1, itemstack)) {
                int j = itemstack.getMaxStackSize() - itemstack1.getCount();
                int k = Math.min(itemstack.getCount(), j);

                itemstack.shrink(k);
                itemstack1.grow(k);
                flag = k > 0;
            }

            if (flag) {
                if (flag1 && iinventory1 instanceof TileEntityHopper) {
                    TileEntityHopper tileentityhopper = (TileEntityHopper) iinventory1;

                    if (!tileentityhopper.isOnCustomCooldown()) {
                        byte b0 = 0;

                        if (iinventory instanceof TileEntityHopper) {
                            TileEntityHopper tileentityhopper1 = (TileEntityHopper) iinventory;

                            if (tileentityhopper.tickedGameTime >= tileentityhopper1.tickedGameTime) {
                                b0 = 1;
                            }
                        }

                        tileentityhopper.setCooldown(8 - b0);
                    }
                }

                iinventory1.setChanged();
            }
        }

        return itemstack;
    }

    // CraftBukkit start
    @Nullable
    private static IInventory runHopperInventorySearchEvent(IInventory inventory, CraftBlock hopper, CraftBlock searchLocation, HopperInventorySearchEvent.ContainerType containerType) {
        HopperInventorySearchEvent event = new HopperInventorySearchEvent((inventory != null) ? new CraftInventory(inventory) : null, containerType, hopper, searchLocation);
        Bukkit.getServer().getPluginManager().callEvent(event);
        CraftInventory craftInventory = (CraftInventory) event.getInventory();
        return (craftInventory != null) ? craftInventory.getInventory() : null;
    }
    // CraftBukkit end

    @Nullable
    private static IInventory getAttachedContainer(World world, BlockPosition blockposition, TileEntityHopper tileentityhopper) {
        // CraftBukkit start
        BlockPosition searchPosition = blockposition.relative(tileentityhopper.facing);
        IInventory inventory = getContainerAt(world, searchPosition);

        CraftBlock hopper = CraftBlock.at(world, blockposition);
        CraftBlock searchBlock = CraftBlock.at(world, searchPosition);
        return runHopperInventorySearchEvent(inventory, hopper, searchBlock, HopperInventorySearchEvent.ContainerType.DESTINATION);
        // CraftBukkit end
    }

    @Nullable
    private static IInventory getSourceContainer(World world, IHopper ihopper, BlockPosition blockposition, IBlockData iblockdata) {
        // CraftBukkit start
        IInventory inventory = getContainerAt(world, blockposition, iblockdata, ihopper.getLevelX(), ihopper.getLevelY() + 1.0D, ihopper.getLevelZ());

        BlockPosition blockPosition = BlockPosition.containing(ihopper.getLevelX(), ihopper.getLevelY(), ihopper.getLevelZ());
        CraftBlock hopper = CraftBlock.at(world, blockPosition);
        CraftBlock container = CraftBlock.at(world, blockPosition.above());
        return runHopperInventorySearchEvent(inventory, hopper, container, HopperInventorySearchEvent.ContainerType.SOURCE);
        // CraftBukkit end
    }

    public static List<EntityItem> getItemsAtAndAbove(World world, IHopper ihopper) {
        AxisAlignedBB axisalignedbb = ihopper.getSuckAabb().move(ihopper.getLevelX() - 0.5D, ihopper.getLevelY() - 0.5D, ihopper.getLevelZ() - 0.5D);

        return world.getEntitiesOfClass(EntityItem.class, axisalignedbb, IEntitySelector.ENTITY_STILL_ALIVE);
    }

    @Nullable
    public static IInventory getContainerAt(World world, BlockPosition blockposition) {
        return getContainerAt(world, blockposition, world.getBlockState(blockposition), (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D);
    }

    @Nullable
    private static IInventory getContainerAt(World world, BlockPosition blockposition, IBlockData iblockdata, double d0, double d1, double d2) {
        IInventory iinventory = getBlockContainer(world, blockposition, iblockdata);

        if (iinventory == null) {
            iinventory = getEntityContainer(world, d0, d1, d2);
        }

        return iinventory;
    }

    @Nullable
    private static IInventory getBlockContainer(World world, BlockPosition blockposition, IBlockData iblockdata) {
        Block block = iblockdata.getBlock();

        if (block instanceof IInventoryHolder) {
            return ((IInventoryHolder) block).getContainer(iblockdata, world, blockposition);
        } else {
            if (iblockdata.hasBlockEntity()) {
                TileEntity tileentity = world.getBlockEntity(blockposition);

                if (tileentity instanceof IInventory) {
                    IInventory iinventory = (IInventory) tileentity;

                    if (iinventory instanceof TileEntityChest && block instanceof BlockChest) {
                        iinventory = BlockChest.getContainer((BlockChest) block, iblockdata, world, blockposition, true);
                    }

                    return iinventory;
                }
            }

            return null;
        }
    }

    @Nullable
    private static IInventory getEntityContainer(World world, double d0, double d1, double d2) {
        List<Entity> list = world.getEntities((Entity) null, new AxisAlignedBB(d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, d0 + 0.5D, d1 + 0.5D, d2 + 0.5D), IEntitySelector.CONTAINER_ENTITY_SELECTOR);

        return !list.isEmpty() ? (IInventory) list.get(world.random.nextInt(list.size())) : null;
    }

    private static boolean canMergeItems(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.getCount() <= itemstack.getMaxStackSize() && ItemStack.isSameItemSameComponents(itemstack, itemstack1);
    }

    @Override
    public double getLevelX() {
        return (double) this.worldPosition.getX() + 0.5D;
    }

    @Override
    public double getLevelY() {
        return (double) this.worldPosition.getY() + 0.5D;
    }

    @Override
    public double getLevelZ() {
        return (double) this.worldPosition.getZ() + 0.5D;
    }

    @Override
    public boolean isGridAligned() {
        return true;
    }

    private void setCooldown(int i) {
        this.cooldownTime = i;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private boolean isOnCustomCooldown() {
        return this.cooldownTime > 8;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonnulllist) {
        this.items = nonnulllist;
    }

    public static void entityInside(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity, TileEntityHopper tileentityhopper) {
        if (entity instanceof EntityItem entityitem) {
            if (!entityitem.getItem().isEmpty() && entity.getBoundingBox().move((double) (-blockposition.getX()), (double) (-blockposition.getY()), (double) (-blockposition.getZ())).intersects(tileentityhopper.getSuckAabb())) {
                tryMoveItems(world, blockposition, iblockdata, tileentityhopper, () -> {
                    return addItem(tileentityhopper, entityitem);
                });
            }
        }

    }

    @Override
    protected Container createMenu(int i, PlayerInventory playerinventory) {
        return new ContainerHopper(i, playerinventory, this);
    }
}
