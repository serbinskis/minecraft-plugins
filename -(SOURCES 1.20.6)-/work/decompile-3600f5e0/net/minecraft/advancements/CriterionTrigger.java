package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.server.AdvancementDataPlayer;

public interface CriterionTrigger<T extends CriterionInstance> {

    void addPlayerListener(AdvancementDataPlayer advancementdataplayer, CriterionTrigger.a<T> criteriontrigger_a);

    void removePlayerListener(AdvancementDataPlayer advancementdataplayer, CriterionTrigger.a<T> criteriontrigger_a);

    void removePlayerListeners(AdvancementDataPlayer advancementdataplayer);

    Codec<T> codec();

    default Criterion<T> createCriterion(T t0) {
        return new Criterion<>(this, t0);
    }

    public static record a<T extends CriterionInstance>(T trigger, AdvancementHolder advancement, String criterion) {

        public void run(AdvancementDataPlayer advancementdataplayer) {
            advancementdataplayer.award(this.advancement, this.criterion);
        }
    }
}
