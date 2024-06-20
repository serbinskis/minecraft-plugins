package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends CriterionTriggerAbstract<PlayerTrigger.a> {

    public PlayerTrigger() {}

    @Override
    public Codec<PlayerTrigger.a> codec() {
        return PlayerTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer) {
        this.trigger(entityplayer, (playertrigger_a) -> {
            return true;
        });
    }

    public static record a(Optional<ContextAwarePredicate> player) implements CriterionTriggerAbstract.a {

        public static final Codec<PlayerTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlayerTrigger.a::player)).apply(instance, PlayerTrigger.a::new);
        });

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
