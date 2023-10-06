package net.minecraft.world.item.crafting;

import net.minecraft.resources.MinecraftKey;

// CraftBukkit start
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;

public record RecipeHolder<T extends IRecipe<?>>(MinecraftKey id, T value) {

    public final Recipe toBukkitRecipe() {
        return this.value.toBukkitRecipe(CraftNamespacedKey.fromMinecraft(this.id));
    }
    // CraftBukkit end

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
