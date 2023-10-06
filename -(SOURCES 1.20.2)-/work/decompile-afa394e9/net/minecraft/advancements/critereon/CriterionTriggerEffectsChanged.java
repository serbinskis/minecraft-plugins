package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerEffectsChanged extends CriterionTriggerAbstract<CriterionTriggerEffectsChanged.a> {

    public CriterionTriggerEffectsChanged() {}

    @Override
    public CriterionTriggerEffectsChanged.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionMobEffect> optional1 = CriterionConditionMobEffect.fromJson(jsonobject.get("effects"));
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "source", lootdeserializationcontext);

        return new CriterionTriggerEffectsChanged.a(optional, optional1, optional2);
    }

    public void trigger(EntityPlayer entityplayer, @Nullable Entity entity) {
        LootTableInfo loottableinfo = entity != null ? CriterionConditionEntity.createContext(entityplayer, entity) : null;

        this.trigger(entityplayer, (criteriontriggereffectschanged_a) -> {
            return criteriontriggereffectschanged_a.matches(entityplayer, loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionMobEffect> effects;
        private final Optional<ContextAwarePredicate> source;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionMobEffect> optional1, Optional<ContextAwarePredicate> optional2) {
            super(optional);
            this.effects = optional1;
            this.source = optional2;
        }

        public static Criterion<CriterionTriggerEffectsChanged.a> hasEffects(CriterionConditionMobEffect.a criterionconditionmobeffect_a) {
            return CriterionTriggers.EFFECTS_CHANGED.createCriterion(new CriterionTriggerEffectsChanged.a(Optional.empty(), criterionconditionmobeffect_a.build(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerEffectsChanged.a> gotEffectsFrom(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.EFFECTS_CHANGED.createCriterion(new CriterionTriggerEffectsChanged.a(Optional.empty(), Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a.build()))));
        }

        public boolean matches(EntityPlayer entityplayer, @Nullable LootTableInfo loottableinfo) {
            return this.effects.isPresent() && !((CriterionConditionMobEffect) this.effects.get()).matches((EntityLiving) entityplayer) ? false : !this.source.isPresent() || loottableinfo != null && ((ContextAwarePredicate) this.source.get()).matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.effects.ifPresent((criterionconditionmobeffect) -> {
                jsonobject.add("effects", criterionconditionmobeffect.serializeToJson());
            });
            this.source.ifPresent((contextawarepredicate) -> {
                jsonobject.add("source", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
