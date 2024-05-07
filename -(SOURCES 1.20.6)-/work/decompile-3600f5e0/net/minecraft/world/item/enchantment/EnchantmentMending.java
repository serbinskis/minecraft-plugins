package net.minecraft.world.item.enchantment;

public class EnchantmentMending extends Enchantment {

    public EnchantmentMending(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }
}
