package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;

public record CriterionConditionEntityFlags(Optional<Boolean> isOnFire, Optional<Boolean> isCrouching, Optional<Boolean> isSprinting, Optional<Boolean> isSwimming, Optional<Boolean> isBaby) {

    public static final Codec<CriterionConditionEntityFlags> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(CriterionConditionEntityFlags::isOnFire), Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(CriterionConditionEntityFlags::isCrouching), Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(CriterionConditionEntityFlags::isSprinting), Codec.BOOL.optionalFieldOf("is_swimming").forGetter(CriterionConditionEntityFlags::isSwimming), Codec.BOOL.optionalFieldOf("is_baby").forGetter(CriterionConditionEntityFlags::isBaby)).apply(instance, CriterionConditionEntityFlags::new);
    });

    public boolean matches(Entity entity) {
        if (this.isOnFire.isPresent() && entity.isOnFire() != (Boolean) this.isOnFire.get()) {
            return false;
        } else if (this.isCrouching.isPresent() && entity.isCrouching() != (Boolean) this.isCrouching.get()) {
            return false;
        } else if (this.isSprinting.isPresent() && entity.isSprinting() != (Boolean) this.isSprinting.get()) {
            return false;
        } else if (this.isSwimming.isPresent() && entity.isSwimming() != (Boolean) this.isSwimming.get()) {
            return false;
        } else {
            if (this.isBaby.isPresent() && entity instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) entity;

                if (entityliving.isBaby() != (Boolean) this.isBaby.get()) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class a {

        private Optional<Boolean> isOnFire = Optional.empty();
        private Optional<Boolean> isCrouching = Optional.empty();
        private Optional<Boolean> isSprinting = Optional.empty();
        private Optional<Boolean> isSwimming = Optional.empty();
        private Optional<Boolean> isBaby = Optional.empty();

        public a() {}

        public static CriterionConditionEntityFlags.a flags() {
            return new CriterionConditionEntityFlags.a();
        }

        public CriterionConditionEntityFlags.a setOnFire(Boolean obool) {
            this.isOnFire = Optional.of(obool);
            return this;
        }

        public CriterionConditionEntityFlags.a setCrouching(Boolean obool) {
            this.isCrouching = Optional.of(obool);
            return this;
        }

        public CriterionConditionEntityFlags.a setSprinting(Boolean obool) {
            this.isSprinting = Optional.of(obool);
            return this;
        }

        public CriterionConditionEntityFlags.a setSwimming(Boolean obool) {
            this.isSwimming = Optional.of(obool);
            return this;
        }

        public CriterionConditionEntityFlags.a setIsBaby(Boolean obool) {
            this.isBaby = Optional.of(obool);
            return this;
        }

        public CriterionConditionEntityFlags build() {
            return new CriterionConditionEntityFlags(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
        }
    }
}
