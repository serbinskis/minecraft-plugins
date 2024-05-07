package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class CriterionTriggerItemDurabilityChanged extends CriterionTriggerAbstract<CriterionTriggerItemDurabilityChanged.a> {

    public CriterionTriggerItemDurabilityChanged() {}

    @Override
    public Codec<CriterionTriggerItemDurabilityChanged.a> codec() {
        return CriterionTriggerItemDurabilityChanged.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, int i) {
        this.trigger(entityplayer, (criteriontriggeritemdurabilitychanged_a) -> {
            return criteriontriggeritemdurabilitychanged_a.matches(itemstack, i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item, CriterionConditionValue.IntegerRange durability, CriterionConditionValue.IntegerRange delta) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerItemDurabilityChanged.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerItemDurabilityChanged.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerItemDurabilityChanged.a::item), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("durability", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerItemDurabilityChanged.a::durability), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("delta", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerItemDurabilityChanged.a::delta)).apply(instance, CriterionTriggerItemDurabilityChanged.a::new);
        });

        public static Criterion<CriterionTriggerItemDurabilityChanged.a> changedDurability(Optional<CriterionConditionItem> optional, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return changedDurability(Optional.empty(), optional, criterionconditionvalue_integerrange);
        }

        public static Criterion<CriterionTriggerItemDurabilityChanged.a> changedDurability(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.ITEM_DURABILITY_CHANGED.createCriterion(new CriterionTriggerItemDurabilityChanged.a(optional, optional1, criterionconditionvalue_integerrange, CriterionConditionValue.IntegerRange.ANY));
        }

        public boolean matches(ItemStack itemstack, int i) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).test(itemstack) ? false : (!this.durability.matches(itemstack.getMaxDamage() - i) ? false : this.delta.matches(itemstack.getDamageValue() - i));
        }
    }
}
