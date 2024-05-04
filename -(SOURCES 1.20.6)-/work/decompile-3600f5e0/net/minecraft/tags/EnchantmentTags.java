package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentTags {

    TagKey<Enchantment> TOOLTIP_ORDER = create("tooltip_order");

    private static TagKey<Enchantment> create(String s) {
        return TagKey.create(Registries.ENCHANTMENT, new MinecraftKey("minecraft", s));
    }
}
