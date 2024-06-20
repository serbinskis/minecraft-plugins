package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;

public interface EnchantmentProviderTypes {

    static MapCodec<? extends EnchantmentProvider> bootstrap(IRegistry<MapCodec<? extends EnchantmentProvider>> iregistry) {
        IRegistry.register(iregistry, "by_cost", EnchantmentsByCost.CODEC);
        IRegistry.register(iregistry, "by_cost_with_difficulty", EnchantmentsByCostWithDifficulty.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "single", SingleEnchantment.CODEC);
    }
}
