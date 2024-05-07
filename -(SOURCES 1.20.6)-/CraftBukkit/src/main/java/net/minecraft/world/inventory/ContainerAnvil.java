package net.minecraft.world.inventory;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.util.UtilColor;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.BlockAnvil;
import net.minecraft.world.level.block.state.IBlockData;
import org.slf4j.Logger;

// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
// CraftBukkit end

public class ContainerAnvil extends ContainerAnvilAbstract {

    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_COST = false;
    public static final int MAX_NAME_LENGTH = 50;
    public int repairItemCountCost;
    @Nullable
    public String itemName;
    public final ContainerProperty cost;
    private static final int COST_FAIL = 0;
    private static final int COST_BASE = 1;
    private static final int COST_ADDED_BASE = 1;
    private static final int COST_REPAIR_MATERIAL = 1;
    private static final int COST_REPAIR_SACRIFICE = 2;
    private static final int COST_INCOMPATIBLE_PENALTY = 1;
    private static final int COST_RENAME = 1;
    private static final int INPUT_SLOT_X_PLACEMENT = 27;
    private static final int ADDITIONAL_SLOT_X_PLACEMENT = 76;
    private static final int RESULT_SLOT_X_PLACEMENT = 134;
    private static final int SLOT_Y_PLACEMENT = 47;
    // CraftBukkit start
    public static final int DEFAULT_DENIED_COST = -1;
    public int maximumRepairCost = 40;
    private CraftInventoryView bukkitEntity;
    // CraftBukkit end

    public ContainerAnvil(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerAnvil(int i, PlayerInventory playerinventory, ContainerAccess containeraccess) {
        super(Containers.ANVIL, i, playerinventory, containeraccess);
        this.cost = ContainerProperty.standalone();
        this.addDataSlot(this.cost);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 27, 47, (itemstack) -> {
            return true;
        }).withSlot(1, 76, 47, (itemstack) -> {
            return true;
        }).withResultSlot(2, 134, 47).build();
    }

    @Override
    protected boolean isValidBlock(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.ANVIL);
    }

    @Override
    protected boolean mayPickup(EntityHuman entityhuman, boolean flag) {
        return (entityhuman.hasInfiniteMaterials() || entityhuman.experienceLevel >= this.cost.get()) && this.cost.get() > ContainerAnvil.DEFAULT_DENIED_COST && flag; // CraftBukkit - allow cost 0 like a free item
    }

    @Override
    protected void onTake(EntityHuman entityhuman, ItemStack itemstack) {
        if (!entityhuman.getAbilities().instabuild) {
            entityhuman.giveExperienceLevels(-this.cost.get());
        }

        this.inputSlots.setItem(0, ItemStack.EMPTY);
        if (this.repairItemCountCost > 0) {
            ItemStack itemstack1 = this.inputSlots.getItem(1);

            if (!itemstack1.isEmpty() && itemstack1.getCount() > this.repairItemCountCost) {
                itemstack1.shrink(this.repairItemCountCost);
                this.inputSlots.setItem(1, itemstack1);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
        }

        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit - use a variable for set a cost for denied item
        this.access.execute((world, blockposition) -> {
            IBlockData iblockdata = world.getBlockState(blockposition);

            if (!entityhuman.hasInfiniteMaterials() && iblockdata.is(TagsBlock.ANVIL) && entityhuman.getRandom().nextFloat() < 0.12F) {
                IBlockData iblockdata1 = BlockAnvil.damage(iblockdata);

                if (iblockdata1 == null) {
                    world.removeBlock(blockposition, false);
                    world.levelEvent(1029, blockposition, 0);
                } else {
                    world.setBlock(blockposition, iblockdata1, 2);
                    world.levelEvent(1030, blockposition, 0);
                }
            } else {
                world.levelEvent(1030, blockposition, 0);
            }

        });
    }

    @Override
    public void createResult() {
        ItemStack itemstack = this.inputSlots.getItem(0);

        this.cost.set(1);
        int i = 0;
        long j = 0L;
        byte b0 = 0;

        if (!itemstack.isEmpty() && EnchantmentManager.canStoreEnchantments(itemstack)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = this.inputSlots.getItem(1);
            ItemEnchantments.a itemenchantments_a = new ItemEnchantments.a(EnchantmentManager.getEnchantmentsForCrafting(itemstack1));

            j += (long) (Integer) itemstack.getOrDefault(DataComponents.REPAIR_COST, 0) + (long) (Integer) itemstack2.getOrDefault(DataComponents.REPAIR_COST, 0);
            this.repairItemCountCost = 0;
            int k;

            if (!itemstack2.isEmpty()) {
                boolean flag = itemstack2.has(DataComponents.STORED_ENCHANTMENTS);
                int l;
                int i1;

                if (itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(itemstack, itemstack2)) {
                    k = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    if (k <= 0) {
                        org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
                        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit - use a variable for set a cost for denied item
                        return;
                    }

                    for (i1 = 0; k > 0 && i1 < itemstack2.getCount(); ++i1) {
                        l = itemstack1.getDamageValue() - k;
                        itemstack1.setDamageValue(l);
                        ++i;
                        k = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    }

                    this.repairItemCountCost = i1;
                } else {
                    if (!flag && (!itemstack1.is(itemstack2.getItem()) || !itemstack1.isDamageableItem())) {
                        org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
                        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit - use a variable for set a cost for denied item
                        return;
                    }

                    if (itemstack1.isDamageableItem() && !flag) {
                        k = itemstack.getMaxDamage() - itemstack.getDamageValue();
                        i1 = itemstack2.getMaxDamage() - itemstack2.getDamageValue();
                        l = i1 + itemstack1.getMaxDamage() * 12 / 100;
                        int j1 = k + l;
                        int k1 = itemstack1.getMaxDamage() - j1;

                        if (k1 < 0) {
                            k1 = 0;
                        }

                        if (k1 < itemstack1.getDamageValue()) {
                            itemstack1.setDamageValue(k1);
                            i += 2;
                        }
                    }

                    ItemEnchantments itemenchantments = EnchantmentManager.getEnchantmentsForCrafting(itemstack2);
                    boolean flag1 = false;
                    boolean flag2 = false;
                    Iterator iterator = itemenchantments.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Entry<Holder<Enchantment>> entry = (Entry) iterator.next();
                        Holder<Enchantment> holder = (Holder) entry.getKey();
                        Enchantment enchantment = (Enchantment) holder.value();
                        int l1 = itemenchantments_a.getLevel(enchantment);
                        int i2 = entry.getIntValue();

                        i2 = l1 == i2 ? i2 + 1 : Math.max(i2, l1);
                        boolean flag3 = enchantment.canEnchant(itemstack);

                        if (this.player.getAbilities().instabuild || itemstack.is(Items.ENCHANTED_BOOK)) {
                            flag3 = true;
                        }

                        Iterator iterator1 = itemenchantments_a.keySet().iterator();

                        while (iterator1.hasNext()) {
                            Holder<Enchantment> holder1 = (Holder) iterator1.next();

                            if (!holder1.equals(holder) && !enchantment.isCompatibleWith((Enchantment) holder1.value())) {
                                flag3 = false;
                                ++i;
                            }
                        }

                        if (!flag3) {
                            flag2 = true;
                        } else {
                            flag1 = true;
                            if (i2 > enchantment.getMaxLevel()) {
                                i2 = enchantment.getMaxLevel();
                            }

                            itemenchantments_a.set(enchantment, i2);
                            int j2 = enchantment.getAnvilCost();

                            if (flag) {
                                j2 = Math.max(1, j2 / 2);
                            }

                            i += j2 * i2;
                            if (itemstack.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }

                    if (flag2 && !flag1) {
                        org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
                        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit - use a variable for set a cost for denied item
                        return;
                    }
                }
            }

            if (this.itemName != null && !UtilColor.isBlank(this.itemName)) {
                if (!this.itemName.equals(itemstack.getHoverName().getString())) {
                    b0 = 1;
                    i += b0;
                    itemstack1.set(DataComponents.CUSTOM_NAME, IChatBaseComponent.literal(this.itemName));
                }
            } else if (itemstack.has(DataComponents.CUSTOM_NAME)) {
                b0 = 1;
                i += b0;
                itemstack1.remove(DataComponents.CUSTOM_NAME);
            }

            int k2 = (int) MathHelper.clamp(j + (long) i, 0L, 2147483647L);

            this.cost.set(k2);
            if (i <= 0) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (b0 == i && b0 > 0 && this.cost.get() >= maximumRepairCost) { // CraftBukkit
                this.cost.set(maximumRepairCost - 1); // CraftBukkit
            }

            if (this.cost.get() >= maximumRepairCost && !this.player.getAbilities().instabuild) { // CraftBukkit
                itemstack1 = ItemStack.EMPTY;
            }

            if (!itemstack1.isEmpty()) {
                k = (Integer) itemstack1.getOrDefault(DataComponents.REPAIR_COST, 0);
                if (k < (Integer) itemstack2.getOrDefault(DataComponents.REPAIR_COST, 0)) {
                    k = (Integer) itemstack2.getOrDefault(DataComponents.REPAIR_COST, 0);
                }

                if (b0 != i || b0 == 0) {
                    k = calculateIncreasedRepairCost(k);
                }

                itemstack1.set(DataComponents.REPAIR_COST, k);
                EnchantmentManager.setEnchantments(itemstack1, itemenchantments_a.toImmutable());
            }

            org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), itemstack1); // CraftBukkit
            sendAllDataToRemote(); // CraftBukkit - SPIGOT-6686: Always send completed inventory to stay in sync with client
            this.broadcastChanges();
        } else {
            org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
            this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit - use a variable for set a cost for denied item
        }
    }

    public static int calculateIncreasedRepairCost(int i) {
        return (int) Math.min((long) i * 2L + 1L, 2147483647L);
    }

    public boolean setItemName(String s) {
        String s1 = validateName(s);

        if (s1 != null && !s1.equals(this.itemName)) {
            this.itemName = s1;
            if (this.getSlot(2).hasItem()) {
                ItemStack itemstack = this.getSlot(2).getItem();

                if (UtilColor.isBlank(s1)) {
                    itemstack.remove(DataComponents.CUSTOM_NAME);
                } else {
                    itemstack.set(DataComponents.CUSTOM_NAME, IChatBaseComponent.literal(s1));
                }
            }

            this.createResult();
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private static String validateName(String s) {
        String s1 = UtilColor.filterText(s);

        return s1.length() <= 50 ? s1 : null;
    }

    public int getCost() {
        return this.cost.get();
    }

    // CraftBukkit start
    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryAnvil(
                access.getLocation(), this.inputSlots, this.resultSlots, this);
        bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);
        return bukkitEntity;
    }
    // CraftBukkit end
}
