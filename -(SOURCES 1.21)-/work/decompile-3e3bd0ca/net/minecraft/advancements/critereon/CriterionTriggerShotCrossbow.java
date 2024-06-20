package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerShotCrossbow extends CriterionTriggerAbstract<CriterionTriggerShotCrossbow.a> {

    public CriterionTriggerShotCrossbow() {}

    @Override
    public Codec<CriterionTriggerShotCrossbow.a> codec() {
        return CriterionTriggerShotCrossbow.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggershotcrossbow_a) -> {
            return criteriontriggershotcrossbow_a.matches(itemstack);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerShotCrossbow.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerShotCrossbow.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerShotCrossbow.a::item)).apply(instance, CriterionTriggerShotCrossbow.a::new);
        });

        public static Criterion<CriterionTriggerShotCrossbow.a> shotCrossbow(Optional<CriterionConditionItem> optional) {
            return CriterionTriggers.SHOT_CROSSBOW.createCriterion(new CriterionTriggerShotCrossbow.a(Optional.empty(), optional));
        }

        public static Criterion<CriterionTriggerShotCrossbow.a> shotCrossbow(IMaterial imaterial) {
            return CriterionTriggers.SHOT_CROSSBOW.createCriterion(new CriterionTriggerShotCrossbow.a(Optional.empty(), Optional.of(CriterionConditionItem.a.item().of(imaterial).build())));
        }

        public boolean matches(ItemStack itemstack) {
            return this.item.isEmpty() || ((CriterionConditionItem) this.item.get()).test(itemstack);
        }
    }
}
