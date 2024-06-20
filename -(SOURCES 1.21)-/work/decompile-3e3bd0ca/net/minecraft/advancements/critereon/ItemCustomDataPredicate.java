package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

public record ItemCustomDataPredicate(CriterionConditionNBT value) implements ItemSubPredicate {

    public static final Codec<ItemCustomDataPredicate> CODEC = CriterionConditionNBT.CODEC.xmap(ItemCustomDataPredicate::new, ItemCustomDataPredicate::value);

    @Override
    public boolean matches(ItemStack itemstack) {
        return this.value.matches(itemstack);
    }

    public static ItemCustomDataPredicate customData(CriterionConditionNBT criterionconditionnbt) {
        return new ItemCustomDataPredicate(criterionconditionnbt);
    }
}
