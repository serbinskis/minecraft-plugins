package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerConsumeItem extends CriterionTriggerAbstract<CriterionTriggerConsumeItem.a> {

    public CriterionTriggerConsumeItem() {}

    @Override
    public Codec<CriterionTriggerConsumeItem.a> codec() {
        return CriterionTriggerConsumeItem.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggerconsumeitem_a) -> {
            return criteriontriggerconsumeitem_a.matches(itemstack);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerConsumeItem.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerConsumeItem.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerConsumeItem.a::item)).apply(instance, CriterionTriggerConsumeItem.a::new);
        });

        public static Criterion<CriterionTriggerConsumeItem.a> usedItem() {
            return CriterionTriggers.CONSUME_ITEM.createCriterion(new CriterionTriggerConsumeItem.a(Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerConsumeItem.a> usedItem(IMaterial imaterial) {
            return usedItem(CriterionConditionItem.a.item().of(imaterial.asItem()));
        }

        public static Criterion<CriterionTriggerConsumeItem.a> usedItem(CriterionConditionItem.a criterionconditionitem_a) {
            return CriterionTriggers.CONSUME_ITEM.createCriterion(new CriterionTriggerConsumeItem.a(Optional.empty(), Optional.of(criterionconditionitem_a.build())));
        }

        public boolean matches(ItemStack itemstack) {
            return this.item.isEmpty() || ((CriterionConditionItem) this.item.get()).test(itemstack);
        }
    }
}
