package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class CriterionTriggerFilledBucket extends CriterionTriggerAbstract<CriterionTriggerFilledBucket.a> {

    public CriterionTriggerFilledBucket() {}

    @Override
    public Codec<CriterionTriggerFilledBucket.a> codec() {
        return CriterionTriggerFilledBucket.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggerfilledbucket_a) -> {
            return criteriontriggerfilledbucket_a.matches(itemstack);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerFilledBucket.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerFilledBucket.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerFilledBucket.a::item)).apply(instance, CriterionTriggerFilledBucket.a::new);
        });

        public static Criterion<CriterionTriggerFilledBucket.a> filledBucket(CriterionConditionItem.a criterionconditionitem_a) {
            return CriterionTriggers.FILLED_BUCKET.createCriterion(new CriterionTriggerFilledBucket.a(Optional.empty(), Optional.of(criterionconditionitem_a.build())));
        }

        public boolean matches(ItemStack itemstack) {
            return !this.item.isPresent() || ((CriterionConditionItem) this.item.get()).test(itemstack);
        }
    }
}
