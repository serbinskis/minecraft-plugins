package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends CriterionTriggerAbstract<PlayerTrigger.a> {

    public PlayerTrigger() {}

    @Override
    public PlayerTrigger.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        return new PlayerTrigger.a(optional);
    }

    public void trigger(EntityPlayer entityplayer) {
        this.trigger(entityplayer, (playertrigger_a) -> {
            return true;
        });
    }

    public static class a extends CriterionInstanceAbstract {

        public a(Optional<ContextAwarePredicate> optional) {
            super(optional);
        }

        public static Criterion<PlayerTrigger.a> located(CriterionConditionLocation.a criterionconditionlocation_a) {
            return CriterionTriggers.LOCATION.createCriterion(new PlayerTrigger.a(Optional.of(CriterionConditionEntity.wrap(CriterionConditionEntity.a.entity().located(criterionconditionlocation_a)))));
        }

        public static Criterion<PlayerTrigger.a> located(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.LOCATION.createCriterion(new PlayerTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a.build()))));
        }

        public static Criterion<PlayerTrigger.a> located(Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.LOCATION.createCriterion(new PlayerTrigger.a(CriterionConditionEntity.wrap(optional)));
        }

        public static Criterion<PlayerTrigger.a> sleptInBed() {
            return CriterionTriggers.SLEPT_IN_BED.createCriterion(new PlayerTrigger.a(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.a> raidWon() {
            return CriterionTriggers.RAID_WIN.createCriterion(new PlayerTrigger.a(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.a> avoidVibration() {
            return CriterionTriggers.AVOID_VIBRATION.createCriterion(new PlayerTrigger.a(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.a> tick() {
            return CriterionTriggers.TICK.createCriterion(new PlayerTrigger.a(Optional.empty()));
        }

        public static Criterion<PlayerTrigger.a> walkOnBlockWithEquipment(Block block, Item item) {
            return located(CriterionConditionEntity.a.entity().equipment(CriterionConditionEntityEquipment.a.equipment().feet(CriterionConditionItem.a.item().of(item))).steppingOn(CriterionConditionLocation.a.location().setBlock(CriterionConditionBlock.a.block().of(block))));
        }
    }
}
