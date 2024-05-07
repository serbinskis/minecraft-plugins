package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Iterator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3D;

public class ContainerGrindstone extends Container {

    public static final int MAX_NAME_LENGTH = 35;
    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private final IInventory resultSlots;
    final IInventory repairSlots;
    private final ContainerAccess access;

    public ContainerGrindstone(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerGrindstone(int i, PlayerInventory playerinventory, final ContainerAccess containeraccess) {
        super(Containers.GRINDSTONE, i);
        this.resultSlots = new InventoryCraftResult();
        this.repairSlots = new InventorySubcontainer(2) {
            @Override
            public void setChanged() {
                super.setChanged();
                ContainerGrindstone.this.slotsChanged(this);
            }
        };
        this.access = containeraccess;
        this.addSlot(new Slot(this, this.repairSlots, 0, 49, 19) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.isDamageableItem() || EnchantmentManager.hasAnyEnchantments(itemstack);
            }
        });
        this.addSlot(new Slot(this, this.repairSlots, 1, 49, 40) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.isDamageableItem() || EnchantmentManager.hasAnyEnchantments(itemstack);
            }
        });
        this.addSlot(new Slot(this.resultSlots, 2, 129, 34) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return false;
            }

            @Override
            public void onTake(EntityHuman entityhuman, ItemStack itemstack) {
                containeraccess.execute((world, blockposition) -> {
                    if (world instanceof WorldServer) {
                        EntityExperienceOrb.award((WorldServer) world, Vec3D.atCenterOf(blockposition), this.getExperienceAmount(world));
                    }

                    world.levelEvent(1042, blockposition, 0);
                });
                ContainerGrindstone.this.repairSlots.setItem(0, ItemStack.EMPTY);
                ContainerGrindstone.this.repairSlots.setItem(1, ItemStack.EMPTY);
            }

            private int getExperienceAmount(World world) {
                int j = 0;

                j += this.getExperienceFromItem(ContainerGrindstone.this.repairSlots.getItem(0));
                j += this.getExperienceFromItem(ContainerGrindstone.this.repairSlots.getItem(1));
                if (j > 0) {
                    int k = (int) Math.ceil((double) j / 2.0D);

                    return k + world.random.nextInt(k);
                } else {
                    return 0;
                }
            }

            private int getExperienceFromItem(ItemStack itemstack) {
                int j = 0;
                ItemEnchantments itemenchantments = EnchantmentManager.getEnchantmentsForCrafting(itemstack);
                Iterator iterator = itemenchantments.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<Holder<Enchantment>> entry = (Entry) iterator.next();
                    Enchantment enchantment = (Enchantment) ((Holder) entry.getKey()).value();
                    int k = entry.getIntValue();

                    if (!enchantment.isCurse()) {
                        j += enchantment.getMinCost(k);
                    }
                }

                return j;
            }
        });

        int j;

        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerinventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerinventory, j, 8 + j * 18, 142));
        }

    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        super.slotsChanged(iinventory);
        if (iinventory == this.repairSlots) {
            this.createResult();
        }

    }

    private void createResult() {
        this.resultSlots.setItem(0, this.computeResult(this.repairSlots.getItem(0), this.repairSlots.getItem(1)));
        this.broadcastChanges();
    }

    private ItemStack computeResult(ItemStack itemstack, ItemStack itemstack1) {
        boolean flag = !itemstack.isEmpty() || !itemstack1.isEmpty();

        if (!flag) {
            return ItemStack.EMPTY;
        } else if (itemstack.getCount() <= 1 && itemstack1.getCount() <= 1) {
            boolean flag1 = !itemstack.isEmpty() && !itemstack1.isEmpty();

            if (!flag1) {
                ItemStack itemstack2 = !itemstack.isEmpty() ? itemstack : itemstack1;

                return !EnchantmentManager.hasAnyEnchantments(itemstack2) ? ItemStack.EMPTY : this.removeNonCursesFrom(itemstack2.copy());
            } else {
                return this.mergeItems(itemstack, itemstack1);
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack mergeItems(ItemStack itemstack, ItemStack itemstack1) {
        if (!itemstack.is(itemstack1.getItem())) {
            return ItemStack.EMPTY;
        } else {
            int i = Math.max(itemstack.getMaxDamage(), itemstack1.getMaxDamage());
            int j = itemstack.getMaxDamage() - itemstack.getDamageValue();
            int k = itemstack1.getMaxDamage() - itemstack1.getDamageValue();
            int l = j + k + i * 5 / 100;
            byte b0 = 1;

            if (!itemstack.isDamageableItem()) {
                if (itemstack.getMaxStackSize() < 2 || !ItemStack.matches(itemstack, itemstack1)) {
                    return ItemStack.EMPTY;
                }

                b0 = 2;
            }

            ItemStack itemstack2 = itemstack.copyWithCount(b0);

            if (itemstack2.isDamageableItem()) {
                itemstack2.set(DataComponents.MAX_DAMAGE, i);
                itemstack2.setDamageValue(Math.max(i - l, 0));
            }

            this.mergeEnchantsFrom(itemstack2, itemstack1);
            return this.removeNonCursesFrom(itemstack2);
        }
    }

    private void mergeEnchantsFrom(ItemStack itemstack, ItemStack itemstack1) {
        EnchantmentManager.updateEnchantments(itemstack, (itemenchantments_a) -> {
            ItemEnchantments itemenchantments = EnchantmentManager.getEnchantmentsForCrafting(itemstack1);
            Iterator iterator = itemenchantments.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<Holder<Enchantment>> entry = (Entry) iterator.next();
                Enchantment enchantment = (Enchantment) ((Holder) entry.getKey()).value();

                if (!enchantment.isCurse() || itemenchantments_a.getLevel(enchantment) == 0) {
                    itemenchantments_a.upgrade(enchantment, entry.getIntValue());
                }
            }

        });
    }

    private ItemStack removeNonCursesFrom(ItemStack itemstack) {
        ItemEnchantments itemenchantments = EnchantmentManager.updateEnchantments(itemstack, (itemenchantments_a) -> {
            itemenchantments_a.removeIf((holder) -> {
                return !((Enchantment) holder.value()).isCurse();
            });
        });

        if (itemstack.is(Items.ENCHANTED_BOOK) && itemenchantments.isEmpty()) {
            itemstack = itemstack.transmuteCopy(Items.BOOK, itemstack.getCount());
        }

        int i = 0;

        for (int j = 0; j < itemenchantments.size(); ++j) {
            i = ContainerAnvil.calculateIncreasedRepairCost(i);
        }

        itemstack.set(DataComponents.REPAIR_COST, i);
        return itemstack;
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.access.execute((world, blockposition) -> {
            this.clearContainer(entityhuman, this.repairSlots);
        });
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return stillValid(this.access, entityhuman, Blocks.GRINDSTONE);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            ItemStack itemstack2 = this.repairSlots.getItem(0);
            ItemStack itemstack3 = this.repairSlots.getItem(1);

            if (i == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (i != 0 && i != 1) {
                if (!itemstack2.isEmpty() && !itemstack3.isEmpty()) {
                    if (i >= 3 && i < 30) {
                        if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
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
}
