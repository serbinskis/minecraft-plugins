package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;

public record Criterion<T extends CriterionInstance>(CriterionTrigger<T> trigger, T triggerInstance) {

    private static final MapCodec<Criterion<?>> MAP_CODEC = ExtraCodecs.dispatchOptionalValue("trigger", "conditions", CriterionTriggers.CODEC, Criterion::trigger, Criterion::criterionCodec);
    public static final Codec<Criterion<?>> CODEC = Criterion.MAP_CODEC.codec();

    private static <T extends CriterionInstance> Codec<Criterion<T>> criterionCodec(CriterionTrigger<T> criteriontrigger) {
        return criteriontrigger.codec().xmap((criterioninstance) -> {
            return new Criterion<>(criteriontrigger, criterioninstance);
        }, Criterion::triggerInstance);
    }
}
