package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public record LootItemRandomChanceWithEnchantedBonusCondition(float unenchantedChance, LevelBasedValue enchantedChance, Holder<Enchantment> enchantment) implements LootItemCondition {

    public static final MapCodec<LootItemRandomChanceWithEnchantedBonusCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("unenchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::unenchantedChance), LevelBasedValue.CODEC.fieldOf("enchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantedChance), Enchantment.CODEC.fieldOf("enchantment").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantment)).apply(instance, LootItemRandomChanceWithEnchantedBonusCondition::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.ATTACKING_ENTITY);
    }

    public boolean test(LootTableInfo loottableinfo) {
        Entity entity = (Entity) loottableinfo.getParamOrNull(LootContextParameters.ATTACKING_ENTITY);
        int i;

        if (entity instanceof EntityLiving entityliving) {
            i = EnchantmentManager.getEnchantmentLevel(this.enchantment, entityliving);
        } else {
            i = 0;
        }

        int j = i;
        float f = j > 0 ? this.enchantedChance.calculate(j) : this.unenchantedChance;

        return loottableinfo.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.a randomChanceAndLootingBoost(HolderLookup.a holderlookup_a, float f, float f1) {
        HolderLookup.b<Enchantment> holderlookup_b = holderlookup_a.lookupOrThrow(Registries.ENCHANTMENT);

        return () -> {
            return new LootItemRandomChanceWithEnchantedBonusCondition(f, new LevelBasedValue.e(f + f1, f1), holderlookup_b.getOrThrow(Enchantments.LOOTING));
        };
    }
}
