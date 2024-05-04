package net.minecraft.world.item.enchantment;

public class EnchantmentVanishing extends Enchantment {

    public EnchantmentVanishing(Enchantment.b enchantment_b) {
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
