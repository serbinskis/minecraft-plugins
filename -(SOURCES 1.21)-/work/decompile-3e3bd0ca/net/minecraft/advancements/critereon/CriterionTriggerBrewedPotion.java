package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.Holder;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class CriterionTriggerBrewedPotion extends CriterionTriggerAbstract<CriterionTriggerBrewedPotion.a> {

    public CriterionTriggerBrewedPotion() {}

    @Override
    public Codec<CriterionTriggerBrewedPotion.a> codec() {
        return CriterionTriggerBrewedPotion.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Holder<PotionRegistry> holder) {
        this.trigger(entityplayer, (criteriontriggerbrewedpotion_a) -> {
            return criteriontriggerbrewedpotion_a.matches(holder);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<Holder<PotionRegistry>> potion) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerBrewedPotion.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerBrewedPotion.a::player), PotionRegistry.CODEC.optionalFieldOf("potion").forGetter(CriterionTriggerBrewedPotion.a::potion)).apply(instance, CriterionTriggerBrewedPotion.a::new);
        });

        public static Criterion<CriterionTriggerBrewedPotion.a> brewedPotion() {
            return CriterionTriggers.BREWED_POTION.createCriterion(new CriterionTriggerBrewedPotion.a(Optional.empty(), Optional.empty()));
        }

        public boolean matches(Holder<PotionRegistry> holder) {
            return !this.potion.isPresent() || ((Holder) this.potion.get()).equals(holder);
        }
    }
}
