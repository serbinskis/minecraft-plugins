package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerEffectsChanged extends CriterionTriggerAbstract<CriterionTriggerEffectsChanged.a> {

    static final MinecraftKey ID = new MinecraftKey("effects_changed");

    public CriterionTriggerEffectsChanged() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerEffectsChanged.ID;
    }

    @Override
    public CriterionTriggerEffectsChanged.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionMobEffect criterionconditionmobeffect = CriterionConditionMobEffect.fromJson(jsonobject.get("effects"));
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "source", lootdeserializationcontext);

        return new CriterionTriggerEffectsChanged.a(contextawarepredicate, criterionconditionmobeffect, contextawarepredicate1);
    }

    public void trigger(EntityPlayer entityplayer, @Nullable Entity entity) {
        LootTableInfo loottableinfo = entity != null ? CriterionConditionEntity.createContext(entityplayer, entity) : null;

        this.trigger(entityplayer, (criteriontriggereffectschanged_a) -> {
            return criteriontriggereffectschanged_a.matches(entityplayer, loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionMobEffect effects;
        private final ContextAwarePredicate source;

        public a(ContextAwarePredicate contextawarepredicate, CriterionConditionMobEffect criterionconditionmobeffect, ContextAwarePredicate contextawarepredicate1) {
            super(CriterionTriggerEffectsChanged.ID, contextawarepredicate);
            this.effects = criterionconditionmobeffect;
            this.source = contextawarepredicate1;
        }

        public static CriterionTriggerEffectsChanged.a hasEffects(CriterionConditionMobEffect criterionconditionmobeffect) {
            return new CriterionTriggerEffectsChanged.a(ContextAwarePredicate.ANY, criterionconditionmobeffect, ContextAwarePredicate.ANY);
        }

        public static CriterionTriggerEffectsChanged.a gotEffectsFrom(CriterionConditionEntity criterionconditionentity) {
            return new CriterionTriggerEffectsChanged.a(ContextAwarePredicate.ANY, CriterionConditionMobEffect.ANY, CriterionConditionEntity.wrap(criterionconditionentity));
        }

        public boolean matches(EntityPlayer entityplayer, @Nullable LootTableInfo loottableinfo) {
            return !this.effects.matches((EntityLiving) entityplayer) ? false : this.source == ContextAwarePredicate.ANY || loottableinfo != null && this.source.matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("effects", this.effects.serializeToJson());
            jsonobject.add("source", this.source.toJson(lootserializationcontext));
            return jsonobject;
        }
    }
}
