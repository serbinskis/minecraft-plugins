package net.minecraft.world.item.crafting;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.World;

public class RecipeRepair extends IRecipeComplex {

    public RecipeRepair(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    @Nullable
    private Pair<ItemStack, ItemStack> getItemsToCombine(CraftingInput craftinginput) {
        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack2 = craftinginput.getItem(i);

            if (!itemstack2.isEmpty()) {
                if (itemstack == null) {
                    itemstack = itemstack2;
                } else {
                    if (itemstack1 != null) {
                        return null;
                    }

                    itemstack1 = itemstack2;
                }
            }
        }

        if (itemstack != null && itemstack1 != null && canCombine(itemstack, itemstack1)) {
            return Pair.of(itemstack, itemstack1);
        } else {
            return null;
        }
    }

    private static boolean canCombine(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack1.is(itemstack.getItem()) && itemstack.getCount() == 1 && itemstack1.getCount() == 1 && itemstack.has(DataComponents.MAX_DAMAGE) && itemstack1.has(DataComponents.MAX_DAMAGE) && itemstack.has(DataComponents.DAMAGE) && itemstack1.has(DataComponents.DAMAGE);
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        return this.getItemsToCombine(craftinginput) != null;
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        Pair<ItemStack, ItemStack> pair = this.getItemsToCombine(craftinginput);

        if (pair == null) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack = (ItemStack) pair.getFirst();
            ItemStack itemstack1 = (ItemStack) pair.getSecond();
            int i = Math.max(itemstack.getMaxDamage(), itemstack1.getMaxDamage());
            int j = itemstack.getMaxDamage() - itemstack.getDamageValue();
            int k = itemstack1.getMaxDamage() - itemstack1.getDamageValue();
            int l = j + k + i * 5 / 100;
            ItemStack itemstack2 = new ItemStack(itemstack.getItem());

            itemstack2.set(DataComponents.MAX_DAMAGE, i);
            itemstack2.setDamageValue(Math.max(i - l, 0));
            ItemEnchantments itemenchantments = EnchantmentManager.getEnchantmentsForCrafting(itemstack);
            ItemEnchantments itemenchantments1 = EnchantmentManager.getEnchantmentsForCrafting(itemstack1);

            EnchantmentManager.updateEnchantments(itemstack2, (itemenchantments_a) -> {
                holderlookup_a.lookupOrThrow(Registries.ENCHANTMENT).listElements().filter((holder_c) -> {
                    return holder_c.is(EnchantmentTags.CURSE);
                }).forEach((holder_c) -> {
                    int i1 = Math.max(itemenchantments.getLevel(holder_c), itemenchantments1.getLevel(holder_c));

                    if (i1 > 0) {
                        itemenchantments_a.upgrade(holder_c, i1);
                    }

                });
            });
            return itemstack2;
        }
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}
