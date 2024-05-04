package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record ItemContainerPredicate(Optional<CollectionPredicate<ItemStack, CriterionConditionItem>> items) implements SingleComponentItemPredicate<ItemContainerContents> {

    public static final Codec<ItemContainerPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(CriterionConditionItem.CODEC).optionalFieldOf("items").forGetter(ItemContainerPredicate::items)).apply(instance, ItemContainerPredicate::new);
    });

    @Override
    public DataComponentType<ItemContainerContents> componentType() {
        return DataComponents.CONTAINER;
    }

    public boolean matches(ItemStack itemstack, ItemContainerContents itemcontainercontents) {
        return !this.items.isPresent() || ((CollectionPredicate) this.items.get()).test(itemcontainercontents.nonEmptyItems());
    }
}
