package net.minecraft.world.item.crafting;

import net.minecraft.resources.MinecraftKey;

public record RecipeHolder<T extends IRecipe<?>> (MinecraftKey id, T value) {

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof RecipeHolder) {
                RecipeHolder<?> recipeholder = (RecipeHolder) object;

                if (this.id.equals(recipeholder.id)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return this.id.toString();
    }
}
