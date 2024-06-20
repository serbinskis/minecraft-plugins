package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record TargetedConditionalEffect<T>(EnchantmentTarget enchanted, EnchantmentTarget affected, T effect, Optional<LootItemCondition> requirements) {

    public static <S> Codec<TargetedConditionalEffect<S>> codec(Codec<S> codec, LootContextParameterSet lootcontextparameterset) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(EnchantmentTarget.CODEC.fieldOf("enchanted").forGetter(TargetedConditionalEffect::enchanted), EnchantmentTarget.CODEC.fieldOf("affected").forGetter(TargetedConditionalEffect::affected), codec.fieldOf("effect").forGetter(TargetedConditionalEffect::effect), ConditionalEffect.conditionCodec(lootcontextparameterset).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)).apply(instance, TargetedConditionalEffect::new);
        });
    }

    public static <S> Codec<TargetedConditionalEffect<S>> equipmentDropsCodec(Codec<S> codec, LootContextParameterSet lootcontextparameterset) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(EnchantmentTarget.CODEC.validate((enchantmenttarget) -> {
                return enchantmenttarget != EnchantmentTarget.DAMAGING_ENTITY ? DataResult.success(enchantmenttarget) : DataResult.error(() -> {
                    return "enchanted must be attacker or victim";
                });
            }).fieldOf("enchanted").forGetter(TargetedConditionalEffect::enchanted), codec.fieldOf("effect").forGetter(TargetedConditionalEffect::effect), ConditionalEffect.conditionCodec(lootcontextparameterset).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)).apply(instance, (enchantmenttarget, object, optional) -> {
                return new TargetedConditionalEffect<>(enchantmenttarget, EnchantmentTarget.VICTIM, object, optional);
            });
        });
    }

    public boolean matches(LootTableInfo loottableinfo) {
        return this.requirements.isEmpty() ? true : ((LootItemCondition) this.requirements.get()).test(loottableinfo);
    }
}
