package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriterionInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.AdvancementDataPlayer;

public class CriterionTriggerImpossible implements CriterionTrigger<CriterionTriggerImpossible.a> {

    public CriterionTriggerImpossible() {}

    @Override
    public void addPlayerListener(AdvancementDataPlayer advancementdataplayer, CriterionTrigger.a<CriterionTriggerImpossible.a> criteriontrigger_a) {}

    @Override
    public void removePlayerListener(AdvancementDataPlayer advancementdataplayer, CriterionTrigger.a<CriterionTriggerImpossible.a> criteriontrigger_a) {}

    @Override
    public void removePlayerListeners(AdvancementDataPlayer advancementdataplayer) {}

    @Override
    public Codec<CriterionTriggerImpossible.a> codec() {
        return CriterionTriggerImpossible.a.CODEC;
    }

    public static record a() implements CriterionInstance {

        public static final Codec<CriterionTriggerImpossible.a> CODEC = Codec.unit(new CriterionTriggerImpossible.a());

        @Override
        public void validate(CriterionValidator criterionvalidator) {}
    }
}
