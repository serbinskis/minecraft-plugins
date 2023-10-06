package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.damagesource.DamageSource;

public record CriterionConditionDamage(CriterionConditionValue.DoubleRange dealtDamage, CriterionConditionValue.DoubleRange takenDamage, Optional<CriterionConditionEntity> sourceEntity, Optional<Boolean> blocked, Optional<CriterionConditionDamageSource> type) {

    public boolean matches(EntityPlayer entityplayer, DamageSource damagesource, float f, float f1, boolean flag) {
        return !this.dealtDamage.matches((double) f) ? false : (!this.takenDamage.matches((double) f1) ? false : (this.sourceEntity.isPresent() && !((CriterionConditionEntity) this.sourceEntity.get()).matches(entityplayer, damagesource.getEntity()) ? false : (this.blocked.isPresent() && (Boolean) this.blocked.get() != flag ? false : !this.type.isPresent() || ((CriterionConditionDamageSource) this.type.get()).matches(entityplayer, damagesource))));
    }

    public static Optional<CriterionConditionDamage> fromJson(@Nullable JsonElement jsonelement) {
        if (jsonelement != null && !jsonelement.isJsonNull()) {
            JsonObject jsonobject = ChatDeserializer.convertToJsonObject(jsonelement, "damage");
            CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange = CriterionConditionValue.DoubleRange.fromJson(jsonobject.get("dealt"));
            CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange1 = CriterionConditionValue.DoubleRange.fromJson(jsonobject.get("taken"));
            Optional<Boolean> optional = jsonobject.has("blocked") ? Optional.of(ChatDeserializer.getAsBoolean(jsonobject, "blocked")) : Optional.empty();
            Optional<CriterionConditionEntity> optional1 = CriterionConditionEntity.fromJson(jsonobject.get("source_entity"));
            Optional<CriterionConditionDamageSource> optional2 = CriterionConditionDamageSource.fromJson(jsonobject.get("type"));

            return criterionconditionvalue_doublerange.isAny() && criterionconditionvalue_doublerange1.isAny() && optional1.isEmpty() && optional.isEmpty() && optional2.isEmpty() ? Optional.empty() : Optional.of(new CriterionConditionDamage(criterionconditionvalue_doublerange, criterionconditionvalue_doublerange1, optional1, optional, optional2));
        } else {
            return Optional.empty();
        }
    }

    public JsonElement serializeToJson() {
        JsonObject jsonobject = new JsonObject();

        jsonobject.add("dealt", this.dealtDamage.serializeToJson());
        jsonobject.add("taken", this.takenDamage.serializeToJson());
        this.sourceEntity.ifPresent((criterionconditionentity) -> {
            jsonobject.add("source_entity", criterionconditionentity.serializeToJson());
        });
        this.type.ifPresent((criterionconditiondamagesource) -> {
            jsonobject.add("type", criterionconditiondamagesource.serializeToJson());
        });
        this.blocked.ifPresent((obool) -> {
            jsonobject.addProperty("blocked", obool);
        });
        return jsonobject;
    }

    public static class a {

        private CriterionConditionValue.DoubleRange dealtDamage;
        private CriterionConditionValue.DoubleRange takenDamage;
        private Optional<CriterionConditionEntity> sourceEntity;
        private Optional<Boolean> blocked;
        private Optional<CriterionConditionDamageSource> type;

        public a() {
            this.dealtDamage = CriterionConditionValue.DoubleRange.ANY;
            this.takenDamage = CriterionConditionValue.DoubleRange.ANY;
            this.sourceEntity = Optional.empty();
            this.blocked = Optional.empty();
            this.type = Optional.empty();
        }

        public static CriterionConditionDamage.a damageInstance() {
            return new CriterionConditionDamage.a();
        }

        public CriterionConditionDamage.a dealtDamage(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            this.dealtDamage = criterionconditionvalue_doublerange;
            return this;
        }

        public CriterionConditionDamage.a takenDamage(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            this.takenDamage = criterionconditionvalue_doublerange;
            return this;
        }

        public CriterionConditionDamage.a sourceEntity(CriterionConditionEntity criterionconditionentity) {
            this.sourceEntity = Optional.of(criterionconditionentity);
            return this;
        }

        public CriterionConditionDamage.a blocked(Boolean obool) {
            this.blocked = Optional.of(obool);
            return this;
        }

        public CriterionConditionDamage.a type(CriterionConditionDamageSource criterionconditiondamagesource) {
            this.type = Optional.of(criterionconditiondamagesource);
            return this;
        }

        public CriterionConditionDamage.a type(CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            this.type = Optional.of(criterionconditiondamagesource_a.build());
            return this;
        }

        public CriterionConditionDamage build() {
            return new CriterionConditionDamage(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
        }
    }
}
