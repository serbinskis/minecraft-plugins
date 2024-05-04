package net.minecraft.world.item.enchantment;

public class SwiftSneakEnchantment extends Enchantment {

    public SwiftSneakEnchantment(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }
}
