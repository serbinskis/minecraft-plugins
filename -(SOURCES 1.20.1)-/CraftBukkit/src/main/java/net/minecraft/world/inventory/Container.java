package net.minecraft.world.inventory;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import org.slf4j.Logger;

// CraftBukkit start
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
// CraftBukkit end

public abstract class Container {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SLOT_CLICKED_OUTSIDE = -999;
    public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;
    public static final int QUICKCRAFT_TYPE_GREEDY = 1;
    public static final int QUICKCRAFT_TYPE_CLONE = 2;
    public static final int QUICKCRAFT_HEADER_START = 0;
    public static final int QUICKCRAFT_HEADER_CONTINUE = 1;
    public static final int QUICKCRAFT_HEADER_END = 2;
    public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;
    public NonNullList<ItemStack> lastSlots = NonNullList.create();
    public NonNullList<Slot> slots = NonNullList.create();
    private final List<ContainerProperty> dataSlots = Lists.newArrayList();
    private ItemStack carried;
    public NonNullList<ItemStack> remoteSlots;
    private final IntList remoteDataSlots;
    private ItemStack remoteCarried;
    private int stateId;
    @Nullable
    private final Containers<?> menuType;
    public final int containerId;
    private int quickcraftType;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots;
    private final List<ICrafting> containerListeners;
    @Nullable
    private ContainerSynchronizer synchronizer;
    private boolean suppressRemoteUpdates;

    // CraftBukkit start
    public boolean checkReachable = true;
    public abstract InventoryView getBukkitView();
    public void transferTo(Container other, org.bukkit.craftbukkit.entity.CraftHumanEntity player) {
        InventoryView source = this.getBukkitView(), destination = other.getBukkitView();
        ((CraftInventory) source.getTopInventory()).getInventory().onClose(player);
        ((CraftInventory) source.getBottomInventory()).getInventory().onClose(player);
        ((CraftInventory) destination.getTopInventory()).getInventory().onOpen(player);
        ((CraftInventory) destination.getBottomInventory()).getInventory().onOpen(player);
    }
    private IChatBaseComponent title;
    public final IChatBaseComponent getTitle() {
        Preconditions.checkState(this.title != null, "Title not set");
        return this.title;
    }
    public final void setTitle(IChatBaseComponent title) {
        Preconditions.checkState(this.title == null, "Title already set");
        this.title = title;
    }
    // CraftBukkit end

    protected Container(@Nullable Containers<?> containers, int i) {
        this.carried = ItemStack.EMPTY;
        this.remoteSlots = NonNullList.create();
        this.remoteDataSlots = new IntArrayList();
        this.remoteCarried = ItemStack.EMPTY;
        this.quickcraftType = -1;
        this.quickcraftSlots = Sets.newHashSet();
        this.containerListeners = Lists.newArrayList();
        this.menuType = containers;
        this.containerId = i;
    }

    protected static boolean stillValid(ContainerAccess containeraccess, EntityHuman entityhuman, Block block) {
        return (Boolean) containeraccess.evaluate((world, blockposition) -> {
            return !world.getBlockState(blockposition).is(block) ? false : entityhuman.distanceToSqr((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    public Containers<?> getType() {
        if (this.menuType == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        } else {
            return this.menuType;
        }
    }

    protected static void checkContainerSize(IInventory iinventory, int i) {
        int j = iinventory.getContainerSize();

        if (j < i) {
            throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + i);
        }
    }

    protected static void checkContainerDataCount(IContainerProperties icontainerproperties, int i) {
        int j = icontainerproperties.getCount();

        if (j < i) {
            throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + i);
        }
    }

    public boolean isValidSlotIndex(int i) {
        return i == -1 || i == -999 || i < this.slots.size();
    }

    protected Slot addSlot(Slot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
        this.lastSlots.add(ItemStack.EMPTY);
        this.remoteSlots.add(ItemStack.EMPTY);
        return slot;
    }

    protected ContainerProperty addDataSlot(ContainerProperty containerproperty) {
        this.dataSlots.add(containerproperty);
        this.remoteDataSlots.add(0);
        return containerproperty;
    }

    protected void addDataSlots(IContainerProperties icontainerproperties) {
        for (int i = 0; i < icontainerproperties.getCount(); ++i) {
            this.addDataSlot(ContainerProperty.forContainer(icontainerproperties, i));
        }

    }

    public void addSlotListener(ICrafting icrafting) {
        if (!this.containerListeners.contains(icrafting)) {
            this.containerListeners.add(icrafting);
            this.broadcastChanges();
        }
    }

    public void setSynchronizer(ContainerSynchronizer containersynchronizer) {
        this.synchronizer = containersynchronizer;
        this.sendAllDataToRemote();
    }

    public void sendAllDataToRemote() {
        int i = 0;

        int j;

        for (j = this.slots.size(); i < j; ++i) {
            this.remoteSlots.set(i, ((Slot) this.slots.get(i)).getItem().copy());
        }

        this.remoteCarried = this.getCarried().copy();
        i = 0;

        for (j = this.dataSlots.size(); i < j; ++i) {
            this.remoteDataSlots.set(i, ((ContainerProperty) this.dataSlots.get(i)).get());
        }

        if (this.synchronizer != null) {
            this.synchronizer.sendInitialData(this, this.remoteSlots, this.remoteCarried, this.remoteDataSlots.toIntArray());
        }

    }

    // CraftBukkit start
    public void broadcastCarriedItem() {
        this.remoteCarried = this.getCarried().copy();
        if (this.synchronizer != null) {
            this.synchronizer.sendCarriedChange(this, this.remoteCarried);
        }
    }
    // CraftBukkit end

    public void removeSlotListener(ICrafting icrafting) {
        this.containerListeners.remove(icrafting);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        Iterator iterator = this.slots.iterator();

        while (iterator.hasNext()) {
            Slot slot = (Slot) iterator.next();

            nonnulllist.add(slot.getItem());
        }

        return nonnulllist;
    }

    public void broadcastChanges() {
        int i;

        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemstack = ((Slot) this.slots.get(i)).getItem();

            Objects.requireNonNull(itemstack);
            Supplier<ItemStack> supplier = Suppliers.memoize(itemstack::copy);

            this.triggerSlotListeners(i, itemstack, supplier);
            this.synchronizeSlotToRemote(i, itemstack, supplier);
        }

        this.synchronizeCarriedToRemote();

        for (i = 0; i < this.dataSlots.size(); ++i) {
            ContainerProperty containerproperty = (ContainerProperty) this.dataSlots.get(i);
            int j = containerproperty.get();

            if (containerproperty.checkAndClearUpdateFlag()) {
                this.updateDataSlotListeners(i, j);
            }

            this.synchronizeDataSlotToRemote(i, j);
        }

    }

    public void broadcastFullState() {
        int i;

        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemstack = ((Slot) this.slots.get(i)).getItem();

            Objects.requireNonNull(itemstack);
            this.triggerSlotListeners(i, itemstack, itemstack::copy);
        }

        for (i = 0; i < this.dataSlots.size(); ++i) {
            ContainerProperty containerproperty = (ContainerProperty) this.dataSlots.get(i);

            if (containerproperty.checkAndClearUpdateFlag()) {
                this.updateDataSlotListeners(i, containerproperty.get());
            }
        }

        this.sendAllDataToRemote();
    }

    private void updateDataSlotListeners(int i, int j) {
        Iterator iterator = this.containerListeners.iterator();

        while (iterator.hasNext()) {
            ICrafting icrafting = (ICrafting) iterator.next();

            icrafting.dataChanged(this, i, j);
        }

    }

    private void triggerSlotListeners(int i, ItemStack itemstack, Supplier<ItemStack> supplier) {
        ItemStack itemstack1 = (ItemStack) this.lastSlots.get(i);

        if (!ItemStack.matches(itemstack1, itemstack)) {
            ItemStack itemstack2 = (ItemStack) supplier.get();

            this.lastSlots.set(i, itemstack2);
            Iterator iterator = this.containerListeners.iterator();

            while (iterator.hasNext()) {
                ICrafting icrafting = (ICrafting) iterator.next();

                icrafting.slotChanged(this, i, itemstack2);
            }
        }

    }

    private void synchronizeSlotToRemote(int i, ItemStack itemstack, Supplier<ItemStack> supplier) {
        if (!this.suppressRemoteUpdates) {
            ItemStack itemstack1 = (ItemStack) this.remoteSlots.get(i);

            if (!ItemStack.matches(itemstack1, itemstack)) {
                ItemStack itemstack2 = (ItemStack) supplier.get();

                this.remoteSlots.set(i, itemstack2);
                if (this.synchronizer != null) {
                    this.synchronizer.sendSlotChange(this, i, itemstack2);
                }
            }

        }
    }

    private void synchronizeDataSlotToRemote(int i, int j) {
        if (!this.suppressRemoteUpdates) {
            int k = this.remoteDataSlots.getInt(i);

            if (k != j) {
                this.remoteDataSlots.set(i, j);
                if (this.synchronizer != null) {
                    this.synchronizer.sendDataChange(this, i, j);
                }
            }

        }
    }

    private void synchronizeCarriedToRemote() {
        if (!this.suppressRemoteUpdates) {
            if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
                this.remoteCarried = this.getCarried().copy();
                if (this.synchronizer != null) {
                    this.synchronizer.sendCarriedChange(this, this.remoteCarried);
                }
            }

        }
    }

    public void setRemoteSlot(int i, ItemStack itemstack) {
        this.remoteSlots.set(i, itemstack.copy());
    }

    public void setRemoteSlotNoCopy(int i, ItemStack itemstack) {
        if (i >= 0 && i < this.remoteSlots.size()) {
            this.remoteSlots.set(i, itemstack);
        } else {
            Container.LOGGER.debug("Incorrect slot index: {} available slots: {}", i, this.remoteSlots.size());
        }
    }

    public void setRemoteCarried(ItemStack itemstack) {
        this.remoteCarried = itemstack.copy();
    }

    public boolean clickMenuButton(EntityHuman entityhuman, int i) {
        return false;
    }

    public Slot getSlot(int i) {
        return (Slot) this.slots.get(i);
    }

    public abstract ItemStack quickMoveStack(EntityHuman entityhuman, int i);

    public void clicked(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
        try {
            this.doClick(i, j, inventoryclicktype, entityhuman);
        } catch (Exception exception) {
            CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Click info");

            crashreportsystemdetails.setDetail("Menu Type", () -> {
                return this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>";
            });
            crashreportsystemdetails.setDetail("Menu Class", () -> {
                return this.getClass().getCanonicalName();
            });
            crashreportsystemdetails.setDetail("Slot Count", (Object) this.slots.size());
            crashreportsystemdetails.setDetail("Slot", (Object) i);
            crashreportsystemdetails.setDetail("Button", (Object) j);
            crashreportsystemdetails.setDetail("Type", (Object) inventoryclicktype);
            throw new ReportedException(crashreport);
        }
    }

    private void doClick(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
        PlayerInventory playerinventory = entityhuman.getInventory();
        Slot slot;
        ItemStack itemstack;
        int k;
        ItemStack itemstack1;
        int l;

        if (inventoryclicktype == InventoryClickType.QUICK_CRAFT) {
            int i1 = this.quickcraftStatus;

            this.quickcraftStatus = getQuickcraftHeader(j);
            if ((i1 != 1 || this.quickcraftStatus != 2) && i1 != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(j);
                if (isValidQuickcraftType(this.quickcraftType, entityhuman)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                slot = (Slot) this.slots.get(i);
                itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && (this.quickcraftType == 2 || itemstack.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (false && this.quickcraftSlots.size() == 1) { // CraftBukkit - treat everything as a drag since we are unable to easily call InventoryClickEvent instead
                        k = ((Slot) this.quickcraftSlots.iterator().next()).index;
                        this.resetQuickCraft();
                        this.doClick(k, this.quickcraftType, InventoryClickType.PICKUP, entityhuman);
                        return;
                    }

                    itemstack1 = this.getCarried().copy();
                    if (itemstack1.isEmpty()) {
                        this.resetQuickCraft();
                        return;
                    }

                    l = this.getCarried().getCount();
                    Iterator iterator = this.quickcraftSlots.iterator();

                    Map<Integer, ItemStack> draggedSlots = new HashMap<Integer, ItemStack>(); // CraftBukkit - Store slots from drag in map (raw slot id -> new stack)
                    while (iterator.hasNext()) {
                        Slot slot1 = (Slot) iterator.next();
                        ItemStack itemstack2 = this.getCarried();

                        if (slot1 != null && canItemQuickReplace(slot1, itemstack2, true) && slot1.mayPlace(itemstack2) && (this.quickcraftType == 2 || itemstack2.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(slot1)) {
                            int j1 = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                            int k1 = Math.min(itemstack1.getMaxStackSize(), slot1.getMaxStackSize(itemstack1));
                            int l1 = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemstack1) + j1, k1);

                            l -= l1 - j1;
                            // slot1.setByPlayer(itemstack1.copyWithCount(l1));
                            draggedSlots.put(slot1.index, itemstack1.copyWithCount(l1)); // CraftBukkit - Put in map instead of setting
                        }
                    }

                    // CraftBukkit start - InventoryDragEvent
                    InventoryView view = getBukkitView();
                    org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemstack1);
                    newcursor.setAmount(l);
                    Map<Integer, org.bukkit.inventory.ItemStack> eventmap = new HashMap<Integer, org.bukkit.inventory.ItemStack>();
                    for (Map.Entry<Integer, ItemStack> ditem : draggedSlots.entrySet()) {
                        eventmap.put(ditem.getKey(), CraftItemStack.asBukkitCopy(ditem.getValue()));
                    }

                    // It's essential that we set the cursor to the new value here to prevent item duplication if a plugin closes the inventory.
                    ItemStack oldCursor = this.getCarried();
                    this.setCarried(CraftItemStack.asNMSCopy(newcursor));

                    InventoryDragEvent event = new InventoryDragEvent(view, (newcursor.getType() != org.bukkit.Material.AIR ? newcursor : null), CraftItemStack.asBukkitCopy(oldCursor), this.quickcraftType == 1, eventmap);
                    entityhuman.level().getCraftServer().getPluginManager().callEvent(event);

                    // Whether or not a change was made to the inventory that requires an update.
                    boolean needsUpdate = event.getResult() != Result.DEFAULT;

                    if (event.getResult() != Result.DENY) {
                        for (Map.Entry<Integer, ItemStack> dslot : draggedSlots.entrySet()) {
                            view.setItem(dslot.getKey(), CraftItemStack.asBukkitCopy(dslot.getValue()));
                        }
                        // The only time the carried item will be set to null is if the inventory is closed by the server.
                        // If the inventory is closed by the server, then the cursor items are dropped.  This is why we change the cursor early.
                        if (this.getCarried() != null) {
                            this.setCarried(CraftItemStack.asNMSCopy(event.getCursor()));
                            needsUpdate = true;
                        }
                    } else {
                        this.setCarried(oldCursor);
                    }

                    if (needsUpdate && entityhuman instanceof EntityPlayer) {
                        this.sendAllDataToRemote();
                    }
                    // CraftBukkit end
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else {
            int i2;

            if ((inventoryclicktype == InventoryClickType.PICKUP || inventoryclicktype == InventoryClickType.QUICK_MOVE) && (j == 0 || j == 1)) {
                ClickAction clickaction = j == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;

                if (i == -999) {
                    if (!this.getCarried().isEmpty()) {
                        if (clickaction == ClickAction.PRIMARY) {
                            // CraftBukkit start
                            ItemStack carried = this.getCarried();
                            this.setCarried(ItemStack.EMPTY);
                            entityhuman.drop(carried, true);
                            // CraftBukkit start
                        } else {
                            entityhuman.drop(this.getCarried().split(1), true);
                        }
                    }
                } else if (inventoryclicktype == InventoryClickType.QUICK_MOVE) {
                    if (i < 0) {
                        return;
                    }

                    slot = (Slot) this.slots.get(i);
                    if (!slot.mayPickup(entityhuman)) {
                        return;
                    }

                    for (itemstack = this.quickMoveStack(entityhuman, i); !itemstack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemstack); itemstack = this.quickMoveStack(entityhuman, i)) {
                        ;
                    }
                } else {
                    if (i < 0) {
                        return;
                    }

                    slot = (Slot) this.slots.get(i);
                    itemstack = slot.getItem();
                    ItemStack itemstack3 = this.getCarried();

                    entityhuman.updateTutorialInventoryAction(itemstack3, slot.getItem(), clickaction);
                    if (!this.tryItemClickBehaviourOverride(entityhuman, clickaction, slot, itemstack, itemstack3)) {
                        if (itemstack.isEmpty()) {
                            if (!itemstack3.isEmpty()) {
                                i2 = clickaction == ClickAction.PRIMARY ? itemstack3.getCount() : 1;
                                this.setCarried(slot.safeInsert(itemstack3, i2));
                            }
                        } else if (slot.mayPickup(entityhuman)) {
                            if (itemstack3.isEmpty()) {
                                i2 = clickaction == ClickAction.PRIMARY ? itemstack.getCount() : (itemstack.getCount() + 1) / 2;
                                Optional<ItemStack> optional = slot.tryRemove(i2, Integer.MAX_VALUE, entityhuman);

                                optional.ifPresent((itemstack4) -> {
                                    this.setCarried(itemstack4);
                                    slot.onTake(entityhuman, itemstack4);
                                });
                            } else if (slot.mayPlace(itemstack3)) {
                                if (ItemStack.isSameItemSameTags(itemstack, itemstack3)) {
                                    i2 = clickaction == ClickAction.PRIMARY ? itemstack3.getCount() : 1;
                                    this.setCarried(slot.safeInsert(itemstack3, i2));
                                } else if (itemstack3.getCount() <= slot.getMaxStackSize(itemstack3)) {
                                    this.setCarried(itemstack);
                                    slot.setByPlayer(itemstack3);
                                }
                            } else if (ItemStack.isSameItemSameTags(itemstack, itemstack3)) {
                                Optional<ItemStack> optional1 = slot.tryRemove(itemstack.getCount(), itemstack3.getMaxStackSize() - itemstack3.getCount(), entityhuman);

                                optional1.ifPresent((itemstack4) -> {
                                    itemstack3.grow(itemstack4.getCount());
                                    slot.onTake(entityhuman, itemstack4);
                                });
                            }
                        }
                    }

                    slot.setChanged();
                    // CraftBukkit start - Make sure the client has the right slot contents
                    if (entityhuman instanceof EntityPlayer && slot.getMaxStackSize() != 64) {
                        ((EntityPlayer) entityhuman).connection.send(new PacketPlayOutSetSlot(this.containerId, this.incrementStateId(), slot.index, slot.getItem()));
                        // Updating a crafting inventory makes the client reset the result slot, have to send it again
                        if (this.getBukkitView().getType() == InventoryType.WORKBENCH || this.getBukkitView().getType() == InventoryType.CRAFTING) {
                            ((EntityPlayer) entityhuman).connection.send(new PacketPlayOutSetSlot(this.containerId, this.incrementStateId(), 0, this.getSlot(0).getItem()));
                        }
                    }
                    // CraftBukkit end
                }
            } else {
                Slot slot2;
                int j2;

                if (inventoryclicktype == InventoryClickType.SWAP) {
                    slot2 = (Slot) this.slots.get(i);
                    itemstack1 = playerinventory.getItem(j);
                    itemstack = slot2.getItem();
                    if (!itemstack1.isEmpty() || !itemstack.isEmpty()) {
                        if (itemstack1.isEmpty()) {
                            if (slot2.mayPickup(entityhuman)) {
                                playerinventory.setItem(j, itemstack);
                                slot2.onSwapCraft(itemstack.getCount());
                                slot2.setByPlayer(ItemStack.EMPTY);
                                slot2.onTake(entityhuman, itemstack);
                            }
                        } else if (itemstack.isEmpty()) {
                            if (slot2.mayPlace(itemstack1)) {
                                j2 = slot2.getMaxStackSize(itemstack1);
                                if (itemstack1.getCount() > j2) {
                                    slot2.setByPlayer(itemstack1.split(j2));
                                } else {
                                    playerinventory.setItem(j, ItemStack.EMPTY);
                                    slot2.setByPlayer(itemstack1);
                                }
                            }
                        } else if (slot2.mayPickup(entityhuman) && slot2.mayPlace(itemstack1)) {
                            j2 = slot2.getMaxStackSize(itemstack1);
                            if (itemstack1.getCount() > j2) {
                                slot2.setByPlayer(itemstack1.split(j2));
                                slot2.onTake(entityhuman, itemstack);
                                if (!playerinventory.add(itemstack)) {
                                    entityhuman.drop(itemstack, true);
                                }
                            } else {
                                playerinventory.setItem(j, itemstack);
                                slot2.setByPlayer(itemstack1);
                                slot2.onTake(entityhuman, itemstack);
                            }
                        }
                    }
                } else if (inventoryclicktype == InventoryClickType.CLONE && entityhuman.getAbilities().instabuild && this.getCarried().isEmpty() && i >= 0) {
                    slot2 = (Slot) this.slots.get(i);
                    if (slot2.hasItem()) {
                        itemstack1 = slot2.getItem();
                        this.setCarried(itemstack1.copyWithCount(itemstack1.getMaxStackSize()));
                    }
                } else if (inventoryclicktype == InventoryClickType.THROW && this.getCarried().isEmpty() && i >= 0) {
                    slot2 = (Slot) this.slots.get(i);
                    k = j == 0 ? 1 : slot2.getItem().getCount();
                    itemstack = slot2.safeTake(k, Integer.MAX_VALUE, entityhuman);
                    entityhuman.drop(itemstack, true);
                } else if (inventoryclicktype == InventoryClickType.PICKUP_ALL && i >= 0) {
                    slot2 = (Slot) this.slots.get(i);
                    itemstack1 = this.getCarried();
                    if (!itemstack1.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(entityhuman))) {
                        l = j == 0 ? 0 : this.slots.size() - 1;
                        j2 = j == 0 ? 1 : -1;

                        for (i2 = 0; i2 < 2; ++i2) {
                            for (int k2 = l; k2 >= 0 && k2 < this.slots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); k2 += j2) {
                                Slot slot3 = (Slot) this.slots.get(k2);

                                if (slot3.hasItem() && canItemQuickReplace(slot3, itemstack1, true) && slot3.mayPickup(entityhuman) && this.canTakeItemForPickAll(itemstack1, slot3)) {
                                    ItemStack itemstack4 = slot3.getItem();

                                    if (i2 != 0 || itemstack4.getCount() != itemstack4.getMaxStackSize()) {
                                        ItemStack itemstack5 = slot3.safeTake(itemstack4.getCount(), itemstack1.getMaxStackSize() - itemstack1.getCount(), entityhuman);

                                        itemstack1.grow(itemstack5.getCount());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private boolean tryItemClickBehaviourOverride(EntityHuman entityhuman, ClickAction clickaction, Slot slot, ItemStack itemstack, ItemStack itemstack1) {
        FeatureFlagSet featureflagset = entityhuman.level().enabledFeatures();

        return itemstack1.isItemEnabled(featureflagset) && itemstack1.overrideStackedOnOther(slot, clickaction, entityhuman) ? true : itemstack.isItemEnabled(featureflagset) && itemstack.overrideOtherStackedOnMe(itemstack1, slot, clickaction, entityhuman, this.createCarriedSlotAccess());
    }

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return Container.this.getCarried();
            }

            @Override
            public boolean set(ItemStack itemstack) {
                Container.this.setCarried(itemstack);
                return true;
            }
        };
    }

    public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
        return true;
    }

    public void removed(EntityHuman entityhuman) {
        if (entityhuman instanceof EntityPlayer) {
            ItemStack itemstack = this.getCarried();

            if (!itemstack.isEmpty()) {
                this.setCarried(ItemStack.EMPTY); // CraftBukkit - SPIGOT-4556 - from below
                if (entityhuman.isAlive() && !((EntityPlayer) entityhuman).hasDisconnected()) {
                    entityhuman.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    entityhuman.drop(itemstack, false);
                }

                // this.setCarried(ItemStack.EMPTY); // CraftBukkit - moved up
            }
        }

    }

    protected void clearContainer(EntityHuman entityhuman, IInventory iinventory) {
        int i;

        if (entityhuman.isAlive() && (!(entityhuman instanceof EntityPlayer) || !((EntityPlayer) entityhuman).hasDisconnected())) {
            for (i = 0; i < iinventory.getContainerSize(); ++i) {
                PlayerInventory playerinventory = entityhuman.getInventory();

                if (playerinventory.player instanceof EntityPlayer) {
                    playerinventory.placeItemBackInInventory(iinventory.removeItemNoUpdate(i));
                }
            }

        } else {
            for (i = 0; i < iinventory.getContainerSize(); ++i) {
                entityhuman.drop(iinventory.removeItemNoUpdate(i), false);
            }

        }
    }

    public void slotsChanged(IInventory iinventory) {
        this.broadcastChanges();
    }

    public void setItem(int i, int j, ItemStack itemstack) {
        this.getSlot(i).set(itemstack);
        this.stateId = j;
    }

    public void initializeContents(int i, List<ItemStack> list, ItemStack itemstack) {
        for (int j = 0; j < list.size(); ++j) {
            this.getSlot(j).set((ItemStack) list.get(j));
        }

        this.carried = itemstack;
        this.stateId = i;
    }

    public void setData(int i, int j) {
        ((ContainerProperty) this.dataSlots.get(i)).set(j);
    }

    public abstract boolean stillValid(EntityHuman entityhuman);

    protected boolean moveItemStackTo(ItemStack itemstack, int i, int j, boolean flag) {
        boolean flag1 = false;
        int k = i;

        if (flag) {
            k = j - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (itemstack.isStackable()) {
            while (!itemstack.isEmpty()) {
                if (flag) {
                    if (k < i) {
                        break;
                    }
                } else if (k >= j) {
                    break;
                }

                slot = (Slot) this.slots.get(k);
                itemstack1 = slot.getItem();
                if (!itemstack1.isEmpty() && ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
                    int l = itemstack1.getCount() + itemstack.getCount();

                    if (l <= itemstack.getMaxStackSize()) {
                        itemstack.setCount(0);
                        itemstack1.setCount(l);
                        slot.setChanged();
                        flag1 = true;
                    } else if (itemstack1.getCount() < itemstack.getMaxStackSize()) {
                        itemstack.shrink(itemstack.getMaxStackSize() - itemstack1.getCount());
                        itemstack1.setCount(itemstack.getMaxStackSize());
                        slot.setChanged();
                        flag1 = true;
                    }
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (!itemstack.isEmpty()) {
            if (flag) {
                k = j - 1;
            } else {
                k = i;
            }

            while (true) {
                if (flag) {
                    if (k < i) {
                        break;
                    }
                } else if (k >= j) {
                    break;
                }

                slot = (Slot) this.slots.get(k);
                itemstack1 = slot.getItem();
                if (itemstack1.isEmpty() && slot.mayPlace(itemstack)) {
                    if (itemstack.getCount() > slot.getMaxStackSize()) {
                        slot.setByPlayer(itemstack.split(slot.getMaxStackSize()));
                    } else {
                        slot.setByPlayer(itemstack.split(itemstack.getCount()));
                    }

                    slot.setChanged();
                    flag1 = true;
                    break;
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        return flag1;
    }

    public static int getQuickcraftType(int i) {
        return i >> 2 & 3;
    }

    public static int getQuickcraftHeader(int i) {
        return i & 3;
    }

    public static int getQuickcraftMask(int i, int j) {
        return i & 3 | (j & 3) << 2;
    }

    public static boolean isValidQuickcraftType(int i, EntityHuman entityhuman) {
        return i == 0 ? true : (i == 1 ? true : i == 2 && entityhuman.getAbilities().instabuild);
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemstack, boolean flag) {
        boolean flag1 = slot == null || !slot.hasItem();

        return !flag1 && ItemStack.isSameItemSameTags(itemstack, slot.getItem()) ? slot.getItem().getCount() + (flag ? 0 : itemstack.getCount()) <= itemstack.getMaxStackSize() : flag1;
    }

    public static int getQuickCraftPlaceCount(Set<Slot> set, int i, ItemStack itemstack) {
        int j;

        switch (i) {
            case 0:
                j = MathHelper.floor((float) itemstack.getCount() / (float) set.size());
                break;
            case 1:
                j = 1;
                break;
            case 2:
                j = itemstack.getItem().getMaxStackSize();
                break;
            default:
                j = itemstack.getCount();
        }

        return j;
    }

    public boolean canDragTo(Slot slot) {
        return true;
    }

    public static int getRedstoneSignalFromBlockEntity(@Nullable TileEntity tileentity) {
        return tileentity instanceof IInventory ? getRedstoneSignalFromContainer((IInventory) tileentity) : 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable IInventory iinventory) {
        if (iinventory == null) {
            return 0;
        } else {
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < iinventory.getContainerSize(); ++j) {
                ItemStack itemstack = iinventory.getItem(j);

                if (!itemstack.isEmpty()) {
                    f += (float) itemstack.getCount() / (float) Math.min(iinventory.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float) iinventory.getContainerSize();
            return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }

    public void setCarried(ItemStack itemstack) {
        this.carried = itemstack;
    }

    public ItemStack getCarried() {
        // CraftBukkit start
        if (this.carried.isEmpty()) {
            this.setCarried(ItemStack.EMPTY);
        }
        // CraftBukkit end
        return this.carried;
    }

    public void suppressRemoteUpdates() {
        this.suppressRemoteUpdates = true;
    }

    public void resumeRemoteUpdates() {
        this.suppressRemoteUpdates = false;
    }

    public void transferState(Container container) {
        Table<IInventory, Integer, Integer> table = HashBasedTable.create();

        Slot slot;
        int i;

        for (i = 0; i < container.slots.size(); ++i) {
            slot = (Slot) container.slots.get(i);
            table.put(slot.container, slot.getContainerSlot(), i);
        }

        for (i = 0; i < this.slots.size(); ++i) {
            slot = (Slot) this.slots.get(i);
            Integer integer = (Integer) table.get(slot.container, slot.getContainerSlot());

            if (integer != null) {
                this.lastSlots.set(i, (ItemStack) container.lastSlots.get(integer));
                this.remoteSlots.set(i, (ItemStack) container.remoteSlots.get(integer));
            }
        }

    }

    public OptionalInt findSlot(IInventory iinventory, int i) {
        for (int j = 0; j < this.slots.size(); ++j) {
            Slot slot = (Slot) this.slots.get(j);

            if (slot.container == iinventory && i == slot.getContainerSlot()) {
                return OptionalInt.of(j);
            }
        }

        return OptionalInt.empty();
    }

    public int getStateId() {
        return this.stateId;
    }

    public int incrementStateId() {
        this.stateId = this.stateId + 1 & 32767;
        return this.stateId;
    }
}
