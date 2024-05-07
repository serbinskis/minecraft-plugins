package net.minecraft.world.inventory;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.MerchantWrapper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.IMerchant;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantRecipe;
import net.minecraft.world.item.trading.MerchantRecipeList;

public class ContainerMerchant extends Container {

    protected static final int PAYMENT1_SLOT = 0;
    protected static final int PAYMENT2_SLOT = 1;
    protected static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private static final int SELLSLOT1_X = 136;
    private static final int SELLSLOT2_X = 162;
    private static final int BUYSLOT_X = 220;
    private static final int ROW_Y = 37;
    private final IMerchant trader;
    private final InventoryMerchant tradeContainer;
    private int merchantLevel;
    private boolean showProgressBar;
    private boolean canRestock;

    public ContainerMerchant(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, new MerchantWrapper(playerinventory.player));
    }

    public ContainerMerchant(int i, PlayerInventory playerinventory, IMerchant imerchant) {
        super(Containers.MERCHANT, i);
        this.trader = imerchant;
        this.tradeContainer = new InventoryMerchant(imerchant);
        this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
        this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
        this.addSlot(new SlotMerchantResult(playerinventory.player, imerchant, this.tradeContainer, 2, 220, 37));

        int j;

        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerinventory, k + j * 9 + 9, 108 + k * 18, 84 + j * 18));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerinventory, j, 108 + j * 18, 142));
        }

    }

    public void setShowProgressBar(boolean flag) {
        this.showProgressBar = flag;
    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(iinventory);
    }

    public void setSelectionHint(int i) {
        this.tradeContainer.setSelectionHint(i);
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return this.trader.getTradingPlayer() == entityhuman;
    }

    public int getTraderXp() {
        return this.trader.getVillagerXp();
    }

    public int getFutureTraderXp() {
        return this.tradeContainer.getFutureXp();
    }

    public void setXp(int i) {
        this.trader.overrideXp(i);
    }

    public int getTraderLevel() {
        return this.merchantLevel;
    }

    public void setMerchantLevel(int i) {
        this.merchantLevel = i;
    }

    public void setCanRestock(boolean flag) {
        this.canRestock = flag;
    }

    public boolean canRestock() {
        return this.canRestock;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
                this.playTradeSound();
            } else if (i != 0 && i != 1) {
                if (i >= 3 && i < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(entityhuman, itemstack1);
        }

        return itemstack;
    }

    private void playTradeSound() {
        if (!this.trader.isClientSide()) {
            Entity entity = (Entity) this.trader;

            entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F, false);
        }

    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.trader.setTradingPlayer((EntityHuman) null);
        if (!this.trader.isClientSide()) {
            if (entityhuman.isAlive() && (!(entityhuman instanceof EntityPlayer) || !((EntityPlayer) entityhuman).hasDisconnected())) {
                if (entityhuman instanceof EntityPlayer) {
                    entityhuman.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(0));
                    entityhuman.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(1));
                }
            } else {
                ItemStack itemstack = this.tradeContainer.removeItemNoUpdate(0);

                if (!itemstack.isEmpty()) {
                    entityhuman.drop(itemstack, false);
                }

                itemstack = this.tradeContainer.removeItemNoUpdate(1);
                if (!itemstack.isEmpty()) {
                    entityhuman.drop(itemstack, false);
                }
            }

        }
    }

    public void tryMoveItems(int i) {
        if (i >= 0 && this.getOffers().size() > i) {
            ItemStack itemstack = this.tradeContainer.getItem(0);

            if (!itemstack.isEmpty()) {
                if (!this.moveItemStackTo(itemstack, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(0, itemstack);
            }

            ItemStack itemstack1 = this.tradeContainer.getItem(1);

            if (!itemstack1.isEmpty()) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(1, itemstack1);
            }

            if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
                MerchantRecipe merchantrecipe = (MerchantRecipe) this.getOffers().get(i);

                this.moveFromInventoryToPaymentSlot(0, merchantrecipe.getItemCostA());
                merchantrecipe.getItemCostB().ifPresent((itemcost) -> {
                    this.moveFromInventoryToPaymentSlot(1, itemcost);
                });
            }

        }
    }

    private void moveFromInventoryToPaymentSlot(int i, ItemCost itemcost) {
        for (int j = 3; j < 39; ++j) {
            ItemStack itemstack = ((Slot) this.slots.get(j)).getItem();

            if (!itemstack.isEmpty() && itemcost.test(itemstack)) {
                ItemStack itemstack1 = this.tradeContainer.getItem(i);

                if (itemstack1.isEmpty() || ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                    int k = itemstack.getMaxStackSize();
                    int l = Math.min(k - itemstack1.getCount(), itemstack.getCount());
                    ItemStack itemstack2 = itemstack.copyWithCount(itemstack1.getCount() + l);

                    itemstack.shrink(l);
                    this.tradeContainer.setItem(i, itemstack2);
                    if (itemstack2.getCount() >= k) {
                        break;
                    }
                }
            }
        }

    }

    public void setOffers(MerchantRecipeList merchantrecipelist) {
        this.trader.overrideOffers(merchantrecipelist);
    }

    public MerchantRecipeList getOffers() {
        return this.trader.getOffers();
    }

    public boolean showProgressBar() {
        return this.showProgressBar;
    }
}
