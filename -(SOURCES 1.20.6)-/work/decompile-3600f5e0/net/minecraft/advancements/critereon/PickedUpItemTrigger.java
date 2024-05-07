package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class PickedUpItemTrigger extends CriterionTriggerAbstract<PickedUpItemTrigger.a> {

    public PickedUpItemTrigger() {}

    @Override
    public Codec<PickedUpItemTrigger.a> codec() {
        return PickedUpItemTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, @Nullable Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (pickedupitemtrigger_a) -> {
            return pickedupitemtrigger_a.matches(entityplayer, itemstack, loottableinfo);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item, Optional<ContextAwarePredicate> entity) implements CriterionTriggerAbstract.a {

        public static final Codec<PickedUpItemTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PickedUpItemTrigger.a::player), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(PickedUpItemTrigger.a::item), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(PickedUpItemTrigger.a::entity)).apply(instance, PickedUpItemTrigger.a::new);
        });

        public static Criterion<PickedUpItemTrigger.a> thrownItemPickedUpByEntity(ContextAwarePredicate contextawarepredicate, Optional<CriterionConditionItem> optional, Optional<ContextAwarePredicate> optional1) {
            return CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(new PickedUpItemTrigger.a(Optional.of(contextawarepredicate), optional, optional1));
        }

        public static Criterion<PickedUpItemTrigger.a> thrownItemPickedUpByPlayer(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, Optional<ContextAwarePredicate> optional2) {
            return CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.createCriterion(new PickedUpItemTrigger.a(optional, optional1, optional2));
        }

        public boolean matches(EntityPlayer entityplayer, ItemStack itemstack, LootTableInfo loottableinfo) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).test(itemstack) ? false : !this.entity.isPresent() || ((ContextAwarePredicate) this.entity.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.entity, ".entity");
        }
    }
}
