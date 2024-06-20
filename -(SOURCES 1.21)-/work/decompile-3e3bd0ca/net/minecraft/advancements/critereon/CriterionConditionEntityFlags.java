package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;

public record CriterionConditionEntityFlags(Optional<Boolean> isOnGround, Optional<Boolean> isOnFire, Optional<Boolean> isCrouching, Optional<Boolean> isSprinting, Optional<Boolean> isSwimming, Optional<Boolean> isFlying, Optional<Boolean> isBaby) {

    public static final Codec<CriterionConditionEntityFlags> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(CriterionConditionEntityFlags::isOnGround), Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(CriterionConditionEntityFlags::isOnFire), Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(CriterionConditionEntityFlags::isCrouching), Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(CriterionConditionEntityFlags::isSprinting), Codec.BOOL.optionalFieldOf("is_swimming").forGetter(CriterionConditionEntityFlags::isSwimming), Codec.BOOL.optionalFieldOf("is_flying").forGetter(CriterionConditionEntityFlags::isFlying), Codec.BOOL.optionalFieldOf("is_baby").forGetter(CriterionConditionEntityFlags::isBaby)).apply(instance, CriterionConditionEntityFlags::new);
    });

    public boolean matches(Entity entity) {
        if (this.isOnGround.isPresent() && entity.onGround() != (Boolean) this.isOnGround.get()) {
            return false;
        } else if (this.isOnFire.isPresent() && entity.isOnFire() != (Boolean) this.isOnFire.get()) {
            return false;
        } else if (this.isCrouching.isPresent() && entity.isCrouching() != (Boolean) this.isCrouching.get()) {
            return false;
        } else if (this.isSprinting.isPresent() && entity.isSprinting() != (Boolean) this.isSprinting.get()) {
            return false;
        } else if (this.isSwimming.isPresent() && entity.isSwimming() != (Boolean) this.isSwimming.get()) {
            return false;
        } else {
            if (this.isFlying.isPresent()) {
                boolean flag;
                label54:
                {
                    if (entity instanceof EntityLiving) {
                        label52:
                        {
                            EntityLiving entityliving = (EntityLiving) entity;

                            if (!entityliving.isFallFlying()) {
                                if (!(entityliving instanceof EntityHuman)) {
                                    break label52;
                                }

                                EntityHuman entityhuman = (EntityHuman) entityliving;

                                if (!entityhuman.getAbilities().flying) {
                                    break label52;
                                }
                            }

                            flag = true;
                            break label54;
                        }
                    }

                    flag = false;
                }

                boolean flag1 = flag;

                if (flag1 != (Boolean) this.isFlying.get()) {
                    return false;
                }
            }

            if (this.isBaby.isPresent() && entity instanceof EntityLiving) {
                EntityLiving entityliving1 = (EntityLiving) entity;

                if (entityliving1.isBaby() != (Boolean) this.isBaby.get()) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class a {

        private Optional<Boolean> isOnGround = Optional.empty();
        private Optional<Boolean> isOnFire = Optional.empty();
        private Optional<Boolean> isCrouching = Optional.empty();
        private Optional<Boolean> isSprinting = Optional.empty();
        private Optional<Boolean> isSwimming = Optional.empty();
        private Optional<Boolean> isFlying = Optional.empty();
        private Optional<Boolean> isBaby = Optional.empty();

        public a() {}

        public static CriterionConditionEntityFlags.a flags() {
            return new CriterionConditionEntityFlags.a();
        }

        public CriterionConditionEntityFlags.a setOnGround(Boolean obool) {
            this.isOnGround = Optional.of(obool);
            return this;
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

        public CriterionConditionEntityFlags.a setIsFlying(Boolean obool) {
            this.isFlying = Optional.of(obool);
            return this;
        }

        public CriterionConditionEntityFlags.a setIsBaby(Boolean obool) {
            this.isBaby = Optional.of(obool);
            return this;
        }

        public CriterionConditionEntityFlags build() {
            return new CriterionConditionEntityFlags(this.isOnGround, this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isFlying, this.isBaby);
        }
    }
}
