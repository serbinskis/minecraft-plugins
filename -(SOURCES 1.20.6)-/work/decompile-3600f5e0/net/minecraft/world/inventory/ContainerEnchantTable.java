package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.WeightedRandomEnchant;
import net.minecraft.world.level.block.BlockEnchantmentTable;
import net.minecraft.world.level.block.Blocks;

public class ContainerEnchantTable extends Container {

    static final MinecraftKey EMPTY_SLOT_LAPIS_LAZULI = new MinecraftKey("item/empty_slot_lapis_lazuli");
    private final IInventory enchantSlots;
    private final ContainerAccess access;
    private final RandomSource random;
    private final ContainerProperty enchantmentSeed;
    public final int[] costs;
    public final int[] enchantClue;
    public final int[] levelClue;

    public ContainerEnchantTable(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerEnchantTable(int i, PlayerInventory playerinventory, ContainerAccess containeraccess) {
        super(Containers.ENCHANTMENT, i);
        this.enchantSlots = new InventorySubcontainer(2) {
            @Override
            public void setChanged() {
                super.setChanged();
                ContainerEnchantTable.this.slotsChanged(this);
            }
        };
        this.random = RandomSource.create();
        this.enchantmentSeed = ContainerProperty.standalone();
        this.costs = new int[3];
        this.enchantClue = new int[]{-1, -1, -1};
        this.levelClue = new int[]{-1, -1, -1};
        this.access = containeraccess;
        this.addSlot(new Slot(this, this.enchantSlots, 0, 15, 47) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this, this.enchantSlots, 1, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public Pair<MinecraftKey, MinecraftKey> getNoItemIcon() {
                return Pair.of(ContainerPlayer.BLOCK_ATLAS, ContainerEnchantTable.EMPTY_SLOT_LAPIS_LAZULI);
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

        this.addDataSlot(ContainerProperty.shared(this.costs, 0));
        this.addDataSlot(ContainerProperty.shared(this.costs, 1));
        this.addDataSlot(ContainerProperty.shared(this.costs, 2));
        this.addDataSlot(this.enchantmentSeed).set(playerinventory.player.getEnchantmentSeed());
        this.addDataSlot(ContainerProperty.shared(this.enchantClue, 0));
        this.addDataSlot(ContainerProperty.shared(this.enchantClue, 1));
        this.addDataSlot(ContainerProperty.shared(this.enchantClue, 2));
        this.addDataSlot(ContainerProperty.shared(this.levelClue, 0));
        this.addDataSlot(ContainerProperty.shared(this.levelClue, 1));
        this.addDataSlot(ContainerProperty.shared(this.levelClue, 2));
    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        if (iinventory == this.enchantSlots) {
            ItemStack itemstack = iinventory.getItem(0);

            if (!itemstack.isEmpty() && itemstack.isEnchantable()) {
                this.access.execute((world, blockposition) -> {
                    int i = 0;
                    Iterator iterator = BlockEnchantmentTable.BOOKSHELF_OFFSETS.iterator();

                    while (iterator.hasNext()) {
                        BlockPosition blockposition1 = (BlockPosition) iterator.next();

                        if (BlockEnchantmentTable.isValidBookShelf(world, blockposition, blockposition1)) {
                            ++i;
                        }
                    }

                    this.random.setSeed((long) this.enchantmentSeed.get());

                    int j;

                    for (j = 0; j < 3; ++j) {
                        this.costs[j] = EnchantmentManager.getEnchantmentCost(this.random, j, i, itemstack);
                        this.enchantClue[j] = -1;
                        this.levelClue[j] = -1;
                        if (this.costs[j] < j + 1) {
                            this.costs[j] = 0;
                        }
                    }

                    for (j = 0; j < 3; ++j) {
                        if (this.costs[j] > 0) {
                            List<WeightedRandomEnchant> list = this.getEnchantmentList(world.enabledFeatures(), itemstack, j, this.costs[j]);

                            if (list != null && !list.isEmpty()) {
                                WeightedRandomEnchant weightedrandomenchant = (WeightedRandomEnchant) list.get(this.random.nextInt(list.size()));

                                this.enchantClue[j] = BuiltInRegistries.ENCHANTMENT.getId(weightedrandomenchant.enchantment);
                                this.levelClue[j] = weightedrandomenchant.level;
                            }
                        }
                    }

                    this.broadcastChanges();
                });
            } else {
                for (int i = 0; i < 3; ++i) {
                    this.costs[i] = 0;
                    this.enchantClue[i] = -1;
                    this.levelClue[i] = -1;
                }
            }
        }

    }

    @Override
    public boolean clickMenuButton(EntityHuman entityhuman, int i) {
        if (i >= 0 && i < this.costs.length) {
            ItemStack itemstack = this.enchantSlots.getItem(0);
            ItemStack itemstack1 = this.enchantSlots.getItem(1);
            int j = i + 1;

            if ((itemstack1.isEmpty() || itemstack1.getCount() < j) && !entityhuman.hasInfiniteMaterials()) {
                return false;
            } else if (this.costs[i] > 0 && !itemstack.isEmpty() && (entityhuman.experienceLevel >= j && entityhuman.experienceLevel >= this.costs[i] || entityhuman.getAbilities().instabuild)) {
                this.access.execute((world, blockposition) -> {
                    ItemStack itemstack2 = itemstack;
                    List<WeightedRandomEnchant> list = this.getEnchantmentList(world.enabledFeatures(), itemstack, i, this.costs[i]);

                    if (!list.isEmpty()) {
                        entityhuman.onEnchantmentPerformed(itemstack, j);
                        if (itemstack.is(Items.BOOK)) {
                            itemstack2 = itemstack.transmuteCopy(Items.ENCHANTED_BOOK, 1);
                            this.enchantSlots.setItem(0, itemstack2);
                        }

                        Iterator iterator = list.iterator();

                        while (iterator.hasNext()) {
                            WeightedRandomEnchant weightedrandomenchant = (WeightedRandomEnchant) iterator.next();

                            itemstack2.enchant(weightedrandomenchant.enchantment, weightedrandomenchant.level);
                        }

                        if (!entityhuman.hasInfiniteMaterials()) {
                            itemstack1.shrink(j);
                            if (itemstack1.isEmpty()) {
                                this.enchantSlots.setItem(1, ItemStack.EMPTY);
                            }
                        }

                        entityhuman.awardStat(StatisticList.ENCHANT_ITEM);
                        if (entityhuman instanceof EntityPlayer) {
                            CriterionTriggers.ENCHANTED_ITEM.trigger((EntityPlayer) entityhuman, itemstack2, j);
                        }

                        this.enchantSlots.setChanged();
                        this.enchantmentSeed.set(entityhuman.getEnchantmentSeed());
                        this.slotsChanged(this.enchantSlots);
                        world.playSound((EntityHuman) null, blockposition, SoundEffects.ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                    }

                });
                return true;
            } else {
                return false;
            }
        } else {
            String s = String.valueOf(entityhuman.getName());

            SystemUtils.logAndPauseIfInIde(s + " pressed invalid button id: " + i);
            return false;
        }
    }

    private List<WeightedRandomEnchant> getEnchantmentList(FeatureFlagSet featureflagset, ItemStack itemstack, int i, int j) {
        this.random.setSeed((long) (this.enchantmentSeed.get() + i));
        List<WeightedRandomEnchant> list = EnchantmentManager.selectEnchantment(featureflagset, this.random, itemstack, j, false);

        if (itemstack.is(Items.BOOK) && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }

        return list;
    }

    public int getGoldCount() {
        ItemStack itemstack = this.enchantSlots.getItem(1);

        return itemstack.isEmpty() ? 0 : itemstack.getCount();
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.access.execute((world, blockposition) -> {
            this.clearContainer(entityhuman, this.enchantSlots);
        });
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return stillValid(this.access, entityhuman, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (i == 1) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (((Slot) this.slots.get(0)).hasItem() || !((Slot) this.slots.get(0)).mayPlace(itemstack1)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack2 = itemstack1.copyWithCount(1);

                itemstack1.shrink(1);
                ((Slot) this.slots.get(0)).setByPlayer(itemstack2);
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
