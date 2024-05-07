package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class CriterionTriggerEnchantedItem extends CriterionTriggerAbstract<CriterionTriggerEnchantedItem.a> {

    public CriterionTriggerEnchantedItem() {}

    @Override
    public Codec<CriterionTriggerEnchantedItem.a> codec() {
        return CriterionTriggerEnchantedItem.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, int i) {
        this.trigger(entityplayer, (criteriontriggerenchanteditem_a) -> {
            return criteriontriggerenchanteditem_a.matches(itemstack, i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item, CriterionConditionValue.IntegerRange levels) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerEnchantedItem.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerEnchantedItem.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerEnchantedItem.a::item), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("levels", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerEnchantedItem.a::levels)).apply(instance, CriterionTriggerEnchantedItem.a::new);
        });

        public static Criterion<CriterionTriggerEnchantedItem.a> enchantedItem() {
            return CriterionTriggers.ENCHANTED_ITEM.createCriterion(new CriterionTriggerEnchantedItem.a(Optional.empty(), Optional.empty(), CriterionConditionValue.IntegerRange.ANY));
        }

        public boolean matches(ItemStack itemstack, int i) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).test(itemstack) ? false : this.levels.matches(i);
        }
    }
}
