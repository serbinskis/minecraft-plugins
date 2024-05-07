package net.minecraft.world.item;

import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.RecipeItemStack;
import net.minecraft.world.level.block.Block;

public interface ToolMaterial {

    int getUses();

    float getSpeed();

    float getAttackDamageBonus();

    TagKey<Block> getIncorrectBlocksForDrops();

    int getEnchantmentValue();

    RecipeItemStack getRepairIngredient();

    default Tool createToolProperties(TagKey<Block> tagkey) {
        return new Tool(List.of(Tool.a.deniesDrops(this.getIncorrectBlocksForDrops()), Tool.a.minesAndDrops(tagkey, this.getSpeed())), 1.0F, 1);
    }
}
