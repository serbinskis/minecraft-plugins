package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record CriterionConditionEnchantments(Optional<Holder<Enchantment>> enchantment, CriterionConditionValue.IntegerRange level) {

    public static final Codec<CriterionConditionEnchantments> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BuiltInRegistries.ENCHANTMENT.holderByNameCodec().optionalFieldOf("enchantment").forGetter(CriterionConditionEnchantments::enchantment), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("levels", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionEnchantments::level)).apply(instance, CriterionConditionEnchantments::new);
    });

    public CriterionConditionEnchantments(Enchantment enchantment, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        this(Optional.of(enchantment.builtInRegistryHolder()), criterionconditionvalue_integerrange);
    }

    public boolean containedIn(ItemEnchantments itemenchantments) {
        if (this.enchantment.isPresent()) {
            Enchantment enchantment = (Enchantment) ((Holder) this.enchantment.get()).value();
            int i = itemenchantments.getLevel(enchantment);

            if (i == 0) {
                return false;
            }

            if (this.level != CriterionConditionValue.IntegerRange.ANY && !this.level.matches(i)) {
                return false;
            }
        } else if (this.level != CriterionConditionValue.IntegerRange.ANY) {
            Iterator iterator = itemenchantments.entrySet().iterator();

            Entry entry;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                entry = (Entry) iterator.next();
            } while (!this.level.matches(entry.getIntValue()));

            return true;
        }

        return true;
    }
}
