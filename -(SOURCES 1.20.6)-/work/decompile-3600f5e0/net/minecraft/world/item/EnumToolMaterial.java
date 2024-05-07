package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.item.crafting.RecipeItemStack;
import net.minecraft.world.level.block.Block;

public enum EnumToolMaterial implements ToolMaterial {

    WOOD(TagsBlock.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 15, () -> {
        return RecipeItemStack.of(TagsItem.PLANKS);
    }), STONE(TagsBlock.INCORRECT_FOR_STONE_TOOL, 131, 4.0F, 1.0F, 5, () -> {
        return RecipeItemStack.of(TagsItem.STONE_TOOL_MATERIALS);
    }), IRON(TagsBlock.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 14, () -> {
        return RecipeItemStack.of(Items.IRON_INGOT);
    }), DIAMOND(TagsBlock.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0F, 3.0F, 10, () -> {
        return RecipeItemStack.of(Items.DIAMOND);
    }), GOLD(TagsBlock.INCORRECT_FOR_GOLD_TOOL, 32, 12.0F, 0.0F, 22, () -> {
        return RecipeItemStack.of(Items.GOLD_INGOT);
    }), NETHERITE(TagsBlock.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 4.0F, 15, () -> {
        return RecipeItemStack.of(Items.NETHERITE_INGOT);
    });

    private final TagKey<Block> incorrectBlocksForDrops;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<RecipeItemStack> repairIngredient;

    private EnumToolMaterial(final TagKey tagkey, final int i, final float f, final float f1, final int j, final Supplier supplier) {
        this.incorrectBlocksForDrops = tagkey;
        this.uses = i;
        this.speed = f;
        this.damage = f1;
        this.enchantmentValue = j;
        Objects.requireNonNull(supplier);
        this.repairIngredient = Suppliers.memoize(supplier::get);
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return this.incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public RecipeItemStack getRepairIngredient() {
        return (RecipeItemStack) this.repairIngredient.get();
    }
}
