package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.npc.EntityVillagerAbstract;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerVillagerTrade extends CriterionTriggerAbstract<CriterionTriggerVillagerTrade.a> {

    public CriterionTriggerVillagerTrade() {}

    @Override
    public Codec<CriterionTriggerVillagerTrade.a> codec() {
        return CriterionTriggerVillagerTrade.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, EntityVillagerAbstract entityvillagerabstract, ItemStack itemstack) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityvillagerabstract);

        this.trigger(entityplayer, (criteriontriggervillagertrade_a) -> {
            return criteriontriggervillagertrade_a.matches(loottableinfo, itemstack);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> villager, Optional<CriterionConditionItem> item) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerVillagerTrade.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerVillagerTrade.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(CriterionTriggerVillagerTrade.a::villager), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerVillagerTrade.a::item)).apply(instance, CriterionTriggerVillagerTrade.a::new);
        });

        public static Criterion<CriterionTriggerVillagerTrade.a> tradedWithVillager() {
            return CriterionTriggers.TRADE.createCriterion(new CriterionTriggerVillagerTrade.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerVillagerTrade.a> tradedWithVillager(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.TRADE.createCriterion(new CriterionTriggerVillagerTrade.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootTableInfo loottableinfo, ItemStack itemstack) {
            return this.villager.isPresent() && !((ContextAwarePredicate) this.villager.get()).matches(loottableinfo) ? false : !this.item.isPresent() || ((CriterionConditionItem) this.item.get()).test(itemstack);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.villager, ".villager");
        }
    }
}
