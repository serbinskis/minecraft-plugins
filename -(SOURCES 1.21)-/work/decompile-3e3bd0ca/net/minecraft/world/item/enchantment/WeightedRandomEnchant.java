package net.minecraft.world.item.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedEntry;

public class WeightedRandomEnchant extends WeightedEntry.a {

    public final Holder<Enchantment> enchantment;
    public final int level;

    public WeightedRandomEnchant(Holder<Enchantment> holder, int i) {
        super(((Enchantment) holder.value()).getWeight());
        this.enchantment = holder;
        this.level = i;
    }
}
