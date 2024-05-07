package net.minecraft.world.inventory;

import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

import org.bukkit.craftbukkit.inventory.CraftInventoryView; // CraftBukkit

public class ContainerSmithing extends ContainerAnvilAbstract {

    public static final int TEMPLATE_SLOT = 0;
    public static final int BASE_SLOT = 1;
    public static final int ADDITIONAL_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
    public static final int BASE_SLOT_X_PLACEMENT = 26;
    public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
    private static final int RESULT_SLOT_X_PLACEMENT = 98;
    public static final int SLOT_Y_PLACEMENT = 48;
    private final World level;
    @Nullable
    private RecipeHolder<SmithingRecipe> selectedRecipe;
    private final List<RecipeHolder<SmithingRecipe>> recipes;
    // CraftBukkit start
    private CraftInventoryView bukkitEntity;
    // CraftBukkit end

    public ContainerSmithing(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerSmithing(int i, PlayerInventory playerinventory, ContainerAccess containeraccess) {
        super(Containers.SMITHING, i, playerinventory, containeraccess);
        this.level = playerinventory.player.level();
        this.recipes = this.level.getRecipeManager().getAllRecipesFor(Recipes.SMITHING);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 8, 48, (itemstack) -> {
            return this.recipes.stream().anyMatch((recipeholder) -> {
                return ((SmithingRecipe) recipeholder.value()).isTemplateIngredient(itemstack);
            });
        }).withSlot(1, 26, 48, (itemstack) -> {
            return this.recipes.stream().anyMatch((recipeholder) -> {
                return ((SmithingRecipe) recipeholder.value()).isBaseIngredient(itemstack);
            });
        }).withSlot(2, 44, 48, (itemstack) -> {
            return this.recipes.stream().anyMatch((recipeholder) -> {
                return ((SmithingRecipe) recipeholder.value()).isAdditionIngredient(itemstack);
            });
        }).withResultSlot(3, 98, 48).build();
    }

    @Override
    protected boolean isValidBlock(IBlockData iblockdata) {
        return iblockdata.is(Blocks.SMITHING_TABLE);
    }

    @Override
    protected boolean mayPickup(EntityHuman entityhuman, boolean flag) {
        return this.selectedRecipe != null && ((SmithingRecipe) this.selectedRecipe.value()).matches(this.inputSlots, this.level);
    }

    @Override
    protected void onTake(EntityHuman entityhuman, ItemStack itemstack) {
        itemstack.onCraftedBy(entityhuman.level(), entityhuman, itemstack.getCount());
        this.resultSlots.awardUsedRecipes(entityhuman, this.getRelevantItems());
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.access.execute((world, blockposition) -> {
            world.levelEvent(1044, blockposition, 0);
        });
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private void shrinkStackInSlot(int i) {
        ItemStack itemstack = this.inputSlots.getItem(i);

        if (!itemstack.isEmpty()) {
            itemstack.shrink(1);
            this.inputSlots.setItem(i, itemstack);
        }

    }

    @Override
    public void createResult() {
        List<RecipeHolder<SmithingRecipe>> list = this.level.getRecipeManager().getRecipesFor(Recipes.SMITHING, this.inputSlots, this.level);

        if (list.isEmpty()) {
            org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareSmithingEvent(getBukkitView(), ItemStack.EMPTY); // CraftBukkit
        } else {
            RecipeHolder<SmithingRecipe> recipeholder = (RecipeHolder) list.get(0);
            ItemStack itemstack = ((SmithingRecipe) recipeholder.value()).assemble(this.inputSlots, this.level.registryAccess());

            if (itemstack.isItemEnabled(this.level.enabledFeatures())) {
                this.selectedRecipe = recipeholder;
                this.resultSlots.setRecipeUsed(recipeholder);
                // CraftBukkit start
                org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareSmithingEvent(getBukkitView(), itemstack);
                // CraftBukkit end
            }
        }

    }

    @Override
    public int getSlotToQuickMoveTo(ItemStack itemstack) {
        return this.findSlotToQuickMoveTo(itemstack).orElse(0);
    }

    private static OptionalInt findSlotMatchingIngredient(SmithingRecipe smithingrecipe, ItemStack itemstack) {
        return smithingrecipe.isTemplateIngredient(itemstack) ? OptionalInt.of(0) : (smithingrecipe.isBaseIngredient(itemstack) ? OptionalInt.of(1) : (smithingrecipe.isAdditionIngredient(itemstack) ? OptionalInt.of(2) : OptionalInt.empty()));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemstack, slot);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack itemstack) {
        return this.findSlotToQuickMoveTo(itemstack).isPresent();
    }

    private OptionalInt findSlotToQuickMoveTo(ItemStack itemstack) {
        return this.recipes.stream().flatMapToInt((recipeholder) -> {
            return findSlotMatchingIngredient((SmithingRecipe) recipeholder.value(), itemstack).stream();
        }).filter((i) -> {
            return !this.getSlot(i).hasItem();
        }).findFirst();
    }

    // CraftBukkit start
    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventorySmithing(
                access.getLocation(), this.inputSlots, this.resultSlots);
        bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);
        return bukkitEntity;
    }
    // CraftBukkit end
}
