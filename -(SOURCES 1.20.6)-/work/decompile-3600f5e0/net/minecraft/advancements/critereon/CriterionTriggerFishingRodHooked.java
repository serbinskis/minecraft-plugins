package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class CriterionTriggerFishingRodHooked extends CriterionTriggerAbstract<CriterionTriggerFishingRodHooked.a> {

    public CriterionTriggerFishingRodHooked() {}

    @Override
    public Codec<CriterionTriggerFishingRodHooked.a> codec() {
        return CriterionTriggerFishingRodHooked.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, EntityFishingHook entityfishinghook, Collection<ItemStack> collection) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, (Entity) (entityfishinghook.getHookedIn() != null ? entityfishinghook.getHookedIn() : entityfishinghook));

        this.trigger(entityplayer, (criteriontriggerfishingrodhooked_a) -> {
            return criteriontriggerfishingrodhooked_a.matches(itemstack, loottableinfo, collection);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> rod, Optional<ContextAwarePredicate> entity, Optional<CriterionConditionItem> item) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerFishingRodHooked.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerFishingRodHooked.a::player), CriterionConditionItem.CODEC.optionalFieldOf("rod").forGetter(CriterionTriggerFishingRodHooked.a::rod), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(CriterionTriggerFishingRodHooked.a::entity), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerFishingRodHooked.a::item)).apply(instance, CriterionTriggerFishingRodHooked.a::new);
        });

        public static Criterion<CriterionTriggerFishingRodHooked.a> fishedItem(Optional<CriterionConditionItem> optional, Optional<CriterionConditionEntity> optional1, Optional<CriterionConditionItem> optional2) {
            return CriterionTriggers.FISHING_ROD_HOOKED.createCriterion(new CriterionTriggerFishingRodHooked.a(Optional.empty(), optional, CriterionConditionEntity.wrap(optional1), optional2));
        }

        public boolean matches(ItemStack itemstack, LootTableInfo loottableinfo, Collection<ItemStack> collection) {
            if (this.rod.isPresent() && !((CriterionConditionItem) this.rod.get()).test(itemstack)) {
                return false;
            } else if (this.entity.isPresent() && !((ContextAwarePredicate) this.entity.get()).matches(loottableinfo)) {
                return false;
            } else {
                if (this.item.isPresent()) {
                    boolean flag = false;
                    Entity entity = (Entity) loottableinfo.getParamOrNull(LootContextParameters.THIS_ENTITY);

                    if (entity instanceof EntityItem) {
                        EntityItem entityitem = (EntityItem) entity;

                        if (((CriterionConditionItem) this.item.get()).test(entityitem.getItem())) {
                            flag = true;
                        }
                    }

                    Iterator iterator = collection.iterator();

                    while (iterator.hasNext()) {
                        ItemStack itemstack1 = (ItemStack) iterator.next();

                        if (((CriterionConditionItem) this.item.get()).test(itemstack1)) {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.entity, ".entity");
        }
    }
}
