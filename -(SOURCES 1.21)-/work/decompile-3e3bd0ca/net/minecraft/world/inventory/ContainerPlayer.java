package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeCrafting;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ContainerPlayer extends ContainerRecipeBook<CraftingInput, RecipeCrafting> {

    public static final int CONTAINER_ID = 0;
    public static final int RESULT_SLOT = 0;
    public static final int CRAFT_SLOT_START = 1;
    public static final int CRAFT_SLOT_COUNT = 4;
    public static final int CRAFT_SLOT_END = 5;
    public static final int ARMOR_SLOT_START = 5;
    public static final int ARMOR_SLOT_COUNT = 4;
    public static final int ARMOR_SLOT_END = 9;
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 36;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 45;
    public static final int SHIELD_SLOT = 45;
    public static final MinecraftKey BLOCK_ATLAS = MinecraftKey.withDefaultNamespace("textures/atlas/blocks.png");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_HELMET = MinecraftKey.withDefaultNamespace("item/empty_armor_slot_helmet");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_CHESTPLATE = MinecraftKey.withDefaultNamespace("item/empty_armor_slot_chestplate");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_LEGGINGS = MinecraftKey.withDefaultNamespace("item/empty_armor_slot_leggings");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_BOOTS = MinecraftKey.withDefaultNamespace("item/empty_armor_slot_boots");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_SHIELD = MinecraftKey.withDefaultNamespace("item/empty_armor_slot_shield");
    private static final Map<EnumItemSlot, MinecraftKey> TEXTURE_EMPTY_SLOTS = Map.of(EnumItemSlot.FEET, ContainerPlayer.EMPTY_ARMOR_SLOT_BOOTS, EnumItemSlot.LEGS, ContainerPlayer.EMPTY_ARMOR_SLOT_LEGGINGS, EnumItemSlot.CHEST, ContainerPlayer.EMPTY_ARMOR_SLOT_CHESTPLATE, EnumItemSlot.HEAD, ContainerPlayer.EMPTY_ARMOR_SLOT_HELMET);
    private static final EnumItemSlot[] SLOT_IDS = new EnumItemSlot[]{EnumItemSlot.HEAD, EnumItemSlot.CHEST, EnumItemSlot.LEGS, EnumItemSlot.FEET};
    private final InventoryCrafting craftSlots = new TransientCraftingContainer(this, 2, 2);
    private final InventoryCraftResult resultSlots = new InventoryCraftResult();
    public final boolean active;
    private final EntityHuman owner;

    public ContainerPlayer(PlayerInventory playerinventory, boolean flag, final EntityHuman entityhuman) {
        super((Containers) null, 0);
        this.active = flag;
        this.owner = entityhuman;
        this.addSlot(new SlotResult(playerinventory.player, this.craftSlots, this.resultSlots, 0, 154, 28));

        int i;
        int j;

        for (i = 0; i < 2; ++i) {
            for (j = 0; j < 2; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }

        for (i = 0; i < 4; ++i) {
            EnumItemSlot enumitemslot = ContainerPlayer.SLOT_IDS[i];
            MinecraftKey minecraftkey = (MinecraftKey) ContainerPlayer.TEXTURE_EMPTY_SLOTS.get(enumitemslot);

            this.addSlot(new ArmorSlot(playerinventory, entityhuman, enumitemslot, 39 - i, 8, 8 + i * 18, minecraftkey));
        }

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerinventory, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerinventory, i, 8 + i * 18, 142));
        }

        this.addSlot(new Slot(this, playerinventory, 40, 77, 62) {
            @Override
            public void setByPlayer(ItemStack itemstack, ItemStack itemstack1) {
                entityhuman.onEquipItem(EnumItemSlot.OFFHAND, itemstack1, itemstack);
                super.setByPlayer(itemstack, itemstack1);
            }

            @Override
            public Pair<MinecraftKey, MinecraftKey> getNoItemIcon() {
                return Pair.of(ContainerPlayer.BLOCK_ATLAS, ContainerPlayer.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    public static boolean isHotbarSlot(int i) {
        return i >= 36 && i < 45 || i == 45;
    }

    @Override
    public void fillCraftSlotsStackedContents(AutoRecipeStackManager autorecipestackmanager) {
        this.craftSlots.fillStackedContents(autorecipestackmanager);
    }

    @Override
    public void clearCraftingContent() {
        this.resultSlots.clearContent();
        this.craftSlots.clearContent();
    }

    @Override
    public boolean recipeMatches(RecipeHolder<RecipeCrafting> recipeholder) {
        return ((RecipeCrafting) recipeholder.value()).matches(this.craftSlots.asCraftInput(), this.owner.level());
    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        ContainerWorkbench.slotChangedCraftingGrid(this, this.owner.level(), this.owner, this.craftSlots, this.resultSlots, (RecipeHolder) null);
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.resultSlots.clearContent();
        if (!entityhuman.level().isClientSide) {
            this.clearContainer(entityhuman, this.craftSlots);
        }
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            EnumItemSlot enumitemslot = entityhuman.getEquipmentSlotForItem(itemstack);

            if (i == 0) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (i >= 1 && i < 5) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 5 && i < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (enumitemslot.getType() == EnumItemSlot.Function.HUMANOID_ARMOR && !((Slot) this.slots.get(8 - enumitemslot.getIndex())).hasItem()) {
                int j = 8 - enumitemslot.getIndex();

                if (!this.moveItemStackTo(itemstack1, j, j + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (enumitemslot == EnumItemSlot.OFFHAND && !((Slot) this.slots.get(45)).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 9 && i < 36) {
                if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 36 && i < 45) {
                if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY, itemstack);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(entityhuman, itemstack1);
            if (i == 0) {
                entityhuman.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemstack, slot);
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    @Override
    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    @Override
    public int getSize() {
        return 5;
    }

    public InventoryCrafting getCraftSlots() {
        return this.craftSlots;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public boolean shouldMoveToInventory(int i) {
        return i != this.getResultSlotIndex();
    }
}
