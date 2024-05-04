package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;

public record CriterionConditionDamage(CriterionConditionValue.DoubleRange dealtDamage, CriterionConditionValue.DoubleRange takenDamage, Optional<CriterionConditionEntity> sourceEntity, Optional<Boolean> blocked, Optional<CriterionConditionDamageSource> type) {

    public static final Codec<CriterionConditionDamage> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("dealt", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionDamage::dealtDamage), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("taken", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionDamage::takenDamage), CriterionConditionEntity.CODEC.optionalFieldOf("source_entity").forGetter(CriterionConditionDamage::sourceEntity), Codec.BOOL.optionalFieldOf("blocked").forGetter(CriterionConditionDamage::blocked), CriterionConditionDamageSource.CODEC.optionalFieldOf("type").forGetter(CriterionConditionDamage::type)).apply(instance, CriterionConditionDamage::new);
    });

    public boolean matches(EntityPlayer entityplayer, DamageSource damagesource, float f, float f1, boolean flag) {
        return !this.dealtDamage.matches((double) f) ? false : (!this.takenDamage.matches((double) f1) ? false : (this.sourceEntity.isPresent() && !((CriterionConditionEntity) this.sourceEntity.get()).matches(entityplayer, damagesource.getEntity()) ? false : (this.blocked.isPresent() && (Boolean) this.blocked.get() != flag ? false : !this.type.isPresent() || ((CriterionConditionDamageSource) this.type.get()).matches(entityplayer, damagesource))));
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
