package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;

public record CriterionConditionEntityEquipment(Optional<CriterionConditionItem> head, Optional<CriterionConditionItem> chest, Optional<CriterionConditionItem> legs, Optional<CriterionConditionItem> feet, Optional<CriterionConditionItem> mainhand, Optional<CriterionConditionItem> offhand) {

    public static final Codec<CriterionConditionEntityEquipment> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "head").forGetter(CriterionConditionEntityEquipment::head), ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "chest").forGetter(CriterionConditionEntityEquipment::chest), ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "legs").forGetter(CriterionConditionEntityEquipment::legs), ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "feet").forGetter(CriterionConditionEntityEquipment::feet), ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "mainhand").forGetter(CriterionConditionEntityEquipment::mainhand), ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "offhand").forGetter(CriterionConditionEntityEquipment::offhand)).apply(instance, CriterionConditionEntityEquipment::new);
    });
    public static final CriterionConditionEntityEquipment CAPTAIN = CriterionConditionEntityEquipment.a.equipment().head(CriterionConditionItem.a.item().of(Items.WHITE_BANNER).hasNbt(Raid.getLeaderBannerInstance().getTag())).build();

    public boolean matches(@Nullable Entity entity) {
        if (entity instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) entity;

            return this.head.isPresent() && !((CriterionConditionItem) this.head.get()).matches(entityliving.getItemBySlot(EnumItemSlot.HEAD)) ? false : (this.chest.isPresent() && !((CriterionConditionItem) this.chest.get()).matches(entityliving.getItemBySlot(EnumItemSlot.CHEST)) ? false : (this.legs.isPresent() && !((CriterionConditionItem) this.legs.get()).matches(entityliving.getItemBySlot(EnumItemSlot.LEGS)) ? false : (this.feet.isPresent() && !((CriterionConditionItem) this.feet.get()).matches(entityliving.getItemBySlot(EnumItemSlot.FEET)) ? false : (this.mainhand.isPresent() && !((CriterionConditionItem) this.mainhand.get()).matches(entityliving.getItemBySlot(EnumItemSlot.MAINHAND)) ? false : !this.offhand.isPresent() || ((CriterionConditionItem) this.offhand.get()).matches(entityliving.getItemBySlot(EnumItemSlot.OFFHAND))))));
        } else {
            return false;
        }
    }

    public static class a {

        private Optional<CriterionConditionItem> head = Optional.empty();
        private Optional<CriterionConditionItem> chest = Optional.empty();
        private Optional<CriterionConditionItem> legs = Optional.empty();
        private Optional<CriterionConditionItem> feet = Optional.empty();
        private Optional<CriterionConditionItem> mainhand = Optional.empty();
        private Optional<CriterionConditionItem> offhand = Optional.empty();

        public a() {}

        public static CriterionConditionEntityEquipment.a equipment() {
            return new CriterionConditionEntityEquipment.a();
        }

        public CriterionConditionEntityEquipment.a head(CriterionConditionItem.a criterionconditionitem_a) {
            this.head = Optional.of(criterionconditionitem_a.build());
            return this;
        }

        public CriterionConditionEntityEquipment.a chest(CriterionConditionItem.a criterionconditionitem_a) {
            this.chest = Optional.of(criterionconditionitem_a.build());
            return this;
        }

        public CriterionConditionEntityEquipment.a legs(CriterionConditionItem.a criterionconditionitem_a) {
            this.legs = Optional.of(criterionconditionitem_a.build());
            return this;
        }

        public CriterionConditionEntityEquipment.a feet(CriterionConditionItem.a criterionconditionitem_a) {
            this.feet = Optional.of(criterionconditionitem_a.build());
            return this;
        }

        public CriterionConditionEntityEquipment.a mainhand(CriterionConditionItem.a criterionconditionitem_a) {
            this.mainhand = Optional.of(criterionconditionitem_a.build());
            return this;
        }

        public CriterionConditionEntityEquipment.a offhand(CriterionConditionItem.a criterionconditionitem_a) {
            this.offhand = Optional.of(criterionconditionitem_a.build());
            return this;
        }

        public CriterionConditionEntityEquipment build() {
            return new CriterionConditionEntityEquipment(this.head, this.chest, this.legs, this.feet, this.mainhand, this.offhand);
        }
    }
}
