package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerPlayerInteractedWithEntity extends CriterionTriggerAbstract<CriterionTriggerPlayerInteractedWithEntity.a> {

    public CriterionTriggerPlayerInteractedWithEntity() {}

    @Override
    public Codec<CriterionTriggerPlayerInteractedWithEntity.a> codec() {
        return CriterionTriggerPlayerInteractedWithEntity.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerplayerinteractedwithentity_a) -> {
            return criteriontriggerplayerinteractedwithentity_a.matches(itemstack, loottableinfo);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item, Optional<ContextAwarePredicate> entity) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerPlayerInteractedWithEntity.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerPlayerInteractedWithEntity.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerPlayerInteractedWithEntity.a::item), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(CriterionTriggerPlayerInteractedWithEntity.a::entity)).apply(instance, CriterionTriggerPlayerInteractedWithEntity.a::new);
        });

        public static Criterion<CriterionTriggerPlayerInteractedWithEntity.a> itemUsedOnEntity(Optional<ContextAwarePredicate> optional, CriterionConditionItem.a criterionconditionitem_a, Optional<ContextAwarePredicate> optional1) {
            return CriterionTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(new CriterionTriggerPlayerInteractedWithEntity.a(optional, Optional.of(criterionconditionitem_a.build()), optional1));
        }

        public static Criterion<CriterionTriggerPlayerInteractedWithEntity.a> itemUsedOnEntity(CriterionConditionItem.a criterionconditionitem_a, Optional<ContextAwarePredicate> optional) {
            return itemUsedOnEntity(Optional.empty(), criterionconditionitem_a, optional);
        }

        public boolean matches(ItemStack itemstack, LootTableInfo loottableinfo) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).test(itemstack) ? false : this.entity.isEmpty() || ((ContextAwarePredicate) this.entity.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.entity, ".entity");
        }
    }
}
