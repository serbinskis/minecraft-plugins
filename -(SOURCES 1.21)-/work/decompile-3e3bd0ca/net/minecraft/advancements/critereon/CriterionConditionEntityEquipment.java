package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;

public record CriterionConditionEntityEquipment(Optional<CriterionConditionItem> head, Optional<CriterionConditionItem> chest, Optional<CriterionConditionItem> legs, Optional<CriterionConditionItem> feet, Optional<CriterionConditionItem> body, Optional<CriterionConditionItem> mainhand, Optional<CriterionConditionItem> offhand) {

    public static final Codec<CriterionConditionEntityEquipment> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionItem.CODEC.optionalFieldOf("head").forGetter(CriterionConditionEntityEquipment::head), CriterionConditionItem.CODEC.optionalFieldOf("chest").forGetter(CriterionConditionEntityEquipment::chest), CriterionConditionItem.CODEC.optionalFieldOf("legs").forGetter(CriterionConditionEntityEquipment::legs), CriterionConditionItem.CODEC.optionalFieldOf("feet").forGetter(CriterionConditionEntityEquipment::feet), CriterionConditionItem.CODEC.optionalFieldOf("body").forGetter(CriterionConditionEntityEquipment::body), CriterionConditionItem.CODEC.optionalFieldOf("mainhand").forGetter(CriterionConditionEntityEquipment::mainhand), CriterionConditionItem.CODEC.optionalFieldOf("offhand").forGetter(CriterionConditionEntityEquipment::offhand)).apply(instance, CriterionConditionEntityEquipment::new);
    });

    public static CriterionConditionEntityEquipment captainPredicate(HolderGetter<EnumBannerPatternType> holdergetter) {
        return CriterionConditionEntityEquipment.a.equipment().head(CriterionConditionItem.a.item().of(Items.WHITE_BANNER).hasComponents(DataComponentPredicate.allOf(Raid.getLeaderBannerInstance(holdergetter).getComponents()))).build();
    }

    public boolean matches(@Nullable Entity entity) {
        if (entity instanceof EntityLiving entityliving) {
            return this.head.isPresent() && !((CriterionConditionItem) this.head.get()).test(entityliving.getItemBySlot(EnumItemSlot.HEAD)) ? false : (this.chest.isPresent() && !((CriterionConditionItem) this.chest.get()).test(entityliving.getItemBySlot(EnumItemSlot.CHEST)) ? false : (this.legs.isPresent() && !((CriterionConditionItem) this.legs.get()).test(entityliving.getItemBySlot(EnumItemSlot.LEGS)) ? false : (this.feet.isPresent() && !((CriterionConditionItem) this.feet.get()).test(entityliving.getItemBySlot(EnumItemSlot.FEET)) ? false : (this.body.isPresent() && !((CriterionConditionItem) this.body.get()).test(entityliving.getItemBySlot(EnumItemSlot.BODY)) ? false : (this.mainhand.isPresent() && !((CriterionConditionItem) this.mainhand.get()).test(entityliving.getItemBySlot(EnumItemSlot.MAINHAND)) ? false : !this.offhand.isPresent() || ((CriterionConditionItem) this.offhand.get()).test(entityliving.getItemBySlot(EnumItemSlot.OFFHAND)))))));
        } else {
            return false;
        }
    }

    public static class a {

        private Optional<CriterionConditionItem> head = Optional.empty();
        private Optional<CriterionConditionItem> chest = Optional.empty();
        private Optional<CriterionConditionItem> legs = Optional.empty();
        private Optional<CriterionConditionItem> feet = Optional.empty();
        private Optional<CriterionConditionItem> body = Optional.empty();
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

        public CriterionConditionEntityEquipment.a body(CriterionConditionItem.a criterionconditionitem_a) {
            this.body = Optional.of(criterionconditionitem_a.build());
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
            return new CriterionConditionEntityEquipment(this.head, this.chest, this.legs, this.feet, this.body, this.mainhand, this.offhand);
        }
    }
}
