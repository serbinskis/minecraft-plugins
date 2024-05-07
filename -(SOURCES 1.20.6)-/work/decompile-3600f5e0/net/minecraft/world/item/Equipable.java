package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;

public interface Equipable {

    EnumItemSlot getEquipmentSlot();

    default Holder<SoundEffect> getEquipSound() {
        return SoundEffects.ARMOR_EQUIP_GENERIC;
    }

    default InteractionResultWrapper<ItemStack> swapWithEquipmentSlot(Item item, World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        EnumItemSlot enumitemslot = EntityInsentient.getEquipmentSlotForItem(itemstack);

        if (!entityhuman.canUseSlot(enumitemslot)) {
            return InteractionResultWrapper.pass(itemstack);
        } else {
            ItemStack itemstack1 = entityhuman.getItemBySlot(enumitemslot);

            if ((!EnchantmentManager.hasBindingCurse(itemstack1) || entityhuman.isCreative()) && !ItemStack.matches(itemstack, itemstack1)) {
                if (!world.isClientSide()) {
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
                }

                ItemStack itemstack2 = itemstack1.isEmpty() ? itemstack : itemstack1.copyAndClear();
                ItemStack itemstack3 = entityhuman.isCreative() ? itemstack.copy() : itemstack.copyAndClear();

                entityhuman.setItemSlot(enumitemslot, itemstack3);
                return InteractionResultWrapper.sidedSuccess(itemstack2, world.isClientSide());
            } else {
                return InteractionResultWrapper.fail(itemstack);
            }
        }
    }

    @Nullable
    static Equipable get(ItemStack itemstack) {
        Item item = itemstack.getItem();

        if (item instanceof Equipable equipable) {
            return equipable;
        } else {
            Item item1 = itemstack.getItem();

            if (item1 instanceof ItemBlock itemblock) {
                Block block = itemblock.getBlock();

                if (block instanceof Equipable equipable1) {
                    return equipable1;
                }
            }

            return null;
        }
    }
}
