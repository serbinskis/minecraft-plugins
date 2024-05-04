package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public record ItemDamagePredicate(CriterionConditionValue.IntegerRange durability, CriterionConditionValue.IntegerRange damage) implements SingleComponentItemPredicate<Integer> {

    public static final Codec<ItemDamagePredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("durability", CriterionConditionValue.IntegerRange.ANY).forGetter(ItemDamagePredicate::durability), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("damage", CriterionConditionValue.IntegerRange.ANY).forGetter(ItemDamagePredicate::damage)).apply(instance, ItemDamagePredicate::new);
    });

    @Override
    public DataComponentType<Integer> componentType() {
        return DataComponents.DAMAGE;
    }

    public boolean matches(ItemStack itemstack, Integer integer) {
        return !this.durability.matches(itemstack.getMaxDamage() - integer) ? false : this.damage.matches(integer);
    }

    public static ItemDamagePredicate durability(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        return new ItemDamagePredicate(criterionconditionvalue_integerrange, CriterionConditionValue.IntegerRange.ANY);
    }
}
