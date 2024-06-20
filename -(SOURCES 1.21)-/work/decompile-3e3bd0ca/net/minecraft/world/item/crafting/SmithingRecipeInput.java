package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;

public record SmithingRecipeInput(ItemStack template, ItemStack base, ItemStack addition) implements RecipeInput {

    @Override
    public ItemStack getItem(int i) {
        ItemStack itemstack;

        switch (i) {
            case 0:
                itemstack = this.template;
                break;
            case 1:
                itemstack = this.base;
                break;
            case 2:
                itemstack = this.addition;
                break;
            default:
                throw new IllegalArgumentException("Recipe does not contain slot " + i);
        }

        return itemstack;
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return this.template.isEmpty() && this.base.isEmpty() && this.addition.isEmpty();
    }
}
