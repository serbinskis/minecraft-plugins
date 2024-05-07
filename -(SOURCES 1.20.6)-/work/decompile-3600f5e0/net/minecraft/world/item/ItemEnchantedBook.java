package net.minecraft.world.item;

import net.minecraft.world.item.enchantment.WeightedRandomEnchant;

public class ItemEnchantedBook extends Item {

    public ItemEnchantedBook(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public boolean isEnchantable(ItemStack itemstack) {
        return false;
    }

    public static ItemStack createForEnchantment(WeightedRandomEnchant weightedrandomenchant) {
        ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);

        itemstack.enchant(weightedrandomenchant.enchantment, weightedrandomenchant.level);
        return itemstack;
    }
}
