package net.minecraft.world.item.enchantment;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record EnchantedItemInUse(ItemStack itemStack, @Nullable EnumItemSlot inSlot, @Nullable EntityLiving owner, Consumer<Item> onBreak) {

    public EnchantedItemInUse(ItemStack itemstack, EnumItemSlot enumitemslot, EntityLiving entityliving) {
        this(itemstack, enumitemslot, entityliving, (item) -> {
            entityliving.onEquippedItemBroken(item, enumitemslot);
        });
    }
}
