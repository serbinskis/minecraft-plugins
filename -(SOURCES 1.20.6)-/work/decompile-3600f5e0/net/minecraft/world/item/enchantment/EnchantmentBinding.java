package net.minecraft.world.item.enchantment;

public class EnchantmentBinding extends Enchantment {

    public EnchantmentBinding(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isCurse() {
        return true;
    }
}
