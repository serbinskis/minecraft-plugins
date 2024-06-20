package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

public interface EquipmentUser {

    void setItemSlot(EnumItemSlot enumitemslot, ItemStack itemstack);

    ItemStack getItemBySlot(EnumItemSlot enumitemslot);

    void setDropChance(EnumItemSlot enumitemslot, float f);

    default void equip(EquipmentTable equipmenttable, LootParams lootparams) {
        this.equip(equipmenttable.lootTable(), lootparams, equipmenttable.slotDropChances());
    }

    default void equip(ResourceKey<LootTable> resourcekey, LootParams lootparams, Map<EnumItemSlot, Float> map) {
        this.equip(resourcekey, lootparams, 0L, map);
    }

    default void equip(ResourceKey<LootTable> resourcekey, LootParams lootparams, long i, Map<EnumItemSlot, Float> map) {
        if (!resourcekey.equals(LootTables.EMPTY)) {
            LootTable loottable = lootparams.getLevel().getServer().reloadableRegistries().getLootTable(resourcekey);

            if (loottable != LootTable.EMPTY) {
                List<ItemStack> list = loottable.getRandomItems(lootparams, i);
                List<EnumItemSlot> list1 = new ArrayList();
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    ItemStack itemstack = (ItemStack) iterator.next();
                    EnumItemSlot enumitemslot = this.resolveSlot(itemstack, list1);

                    if (enumitemslot != null) {
                        ItemStack itemstack1 = enumitemslot.limit(itemstack);

                        this.setItemSlot(enumitemslot, itemstack1);
                        Float ofloat = (Float) map.get(enumitemslot);

                        if (ofloat != null) {
                            this.setDropChance(enumitemslot, ofloat);
                        }

                        list1.add(enumitemslot);
                    }
                }

            }
        }
    }

    @Nullable
    default EnumItemSlot resolveSlot(ItemStack itemstack, List<EnumItemSlot> list) {
        if (itemstack.isEmpty()) {
            return null;
        } else {
            Equipable equipable = Equipable.get(itemstack);

            if (equipable != null) {
                EnumItemSlot enumitemslot = equipable.getEquipmentSlot();

                if (!list.contains(enumitemslot)) {
                    return enumitemslot;
                }
            } else if (!list.contains(EnumItemSlot.MAINHAND)) {
                return EnumItemSlot.MAINHAND;
            }

            return null;
        }
    }
}
