package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends CriterionTriggerAbstract<UsingItemTrigger.a> {

    public UsingItemTrigger() {}

    @Override
    public Codec<UsingItemTrigger.a> codec() {
        return UsingItemTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (usingitemtrigger_a) -> {
            return usingitemtrigger_a.matches(itemstack);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item) implements CriterionTriggerAbstract.a {

        public static final Codec<UsingItemTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsingItemTrigger.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(UsingItemTrigger.a::item)).apply(instance, UsingItemTrigger.a::new);
        });

        public static Criterion<UsingItemTrigger.a> lookingAt(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionItem.a criterionconditionitem_a) {
            return CriterionTriggers.USING_ITEM.createCriterion(new UsingItemTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.of(criterionconditionitem_a.build())));
        }

        public boolean matches(ItemStack itemstack) {
            return !this.item.isPresent() || ((CriterionConditionItem) this.item.get()).test(itemstack);
        }
    }
}
