package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public abstract class ItemEnchantmentsPredicate implements SingleComponentItemPredicate<ItemEnchantments> {

    private final List<CriterionConditionEnchantments> enchantments;

    protected ItemEnchantmentsPredicate(List<CriterionConditionEnchantments> list) {
        this.enchantments = list;
    }

    public static <T extends ItemEnchantmentsPredicate> Codec<T> codec(Function<List<CriterionConditionEnchantments>, T> function) {
        return CriterionConditionEnchantments.CODEC.listOf().xmap(function, ItemEnchantmentsPredicate::enchantments);
    }

    protected List<CriterionConditionEnchantments> enchantments() {
        return this.enchantments;
    }

    public boolean matches(ItemStack itemstack, ItemEnchantments itemenchantments) {
        Iterator iterator = this.enchantments.iterator();

        CriterionConditionEnchantments criterionconditionenchantments;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            criterionconditionenchantments = (CriterionConditionEnchantments) iterator.next();
        } while (criterionconditionenchantments.containedIn(itemenchantments));

        return false;
    }

    public static ItemEnchantmentsPredicate.a enchantments(List<CriterionConditionEnchantments> list) {
        return new ItemEnchantmentsPredicate.a(list);
    }

    public static ItemEnchantmentsPredicate.b storedEnchantments(List<CriterionConditionEnchantments> list) {
        return new ItemEnchantmentsPredicate.b(list);
    }

    public static class a extends ItemEnchantmentsPredicate {

        public static final Codec<ItemEnchantmentsPredicate.a> CODEC = codec(ItemEnchantmentsPredicate.a::new);

        protected a(List<CriterionConditionEnchantments> list) {
            super(list);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.ENCHANTMENTS;
        }
    }

    public static class b extends ItemEnchantmentsPredicate {

        public static final Codec<ItemEnchantmentsPredicate.b> CODEC = codec(ItemEnchantmentsPredicate.b::new);

        protected b(List<CriterionConditionEnchantments> list) {
            super(list);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.STORED_ENCHANTMENTS;
        }
    }
}
