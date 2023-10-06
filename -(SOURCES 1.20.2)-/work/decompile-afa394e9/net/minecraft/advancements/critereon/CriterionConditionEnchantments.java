package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

public record CriterionConditionEnchantments(Optional<Holder<Enchantment>> enchantment, CriterionConditionValue.IntegerRange level) {

    public static final Codec<CriterionConditionEnchantments> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), "enchantment").forGetter(CriterionConditionEnchantments::enchantment), ExtraCodecs.strictOptionalField(CriterionConditionValue.IntegerRange.CODEC, "levels", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionEnchantments::level)).apply(instance, CriterionConditionEnchantments::new);
    });

    public CriterionConditionEnchantments(Enchantment enchantment, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        this(Optional.of(enchantment.builtInRegistryHolder()), criterionconditionvalue_integerrange);
    }

    public boolean containedIn(Map<Enchantment, Integer> map) {
        if (this.enchantment.isPresent()) {
            Enchantment enchantment = (Enchantment) ((Holder) this.enchantment.get()).value();

            if (!map.containsKey(enchantment)) {
                return false;
            }

            int i = (Integer) map.get(enchantment);

            if (this.level != CriterionConditionValue.IntegerRange.ANY && !this.level.matches(i)) {
                return false;
            }
        } else if (this.level != CriterionConditionValue.IntegerRange.ANY) {
            Iterator iterator = map.values().iterator();

            Integer integer;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                integer = (Integer) iterator.next();
            } while (!this.level.matches(integer));

            return true;
        }

        return true;
    }
}
