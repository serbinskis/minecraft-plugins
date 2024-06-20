package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public record SlotsPredicate(Map<SlotRange, CriterionConditionItem> slots) {

    public static final Codec<SlotsPredicate> CODEC = Codec.unboundedMap(SlotRanges.CODEC, CriterionConditionItem.CODEC).xmap(SlotsPredicate::new, SlotsPredicate::slots);

    public boolean matches(Entity entity) {
        Iterator iterator = this.slots.entrySet().iterator();

        Entry entry;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            entry = (Entry) iterator.next();
        } while (matchSlots(entity, (CriterionConditionItem) entry.getValue(), ((SlotRange) entry.getKey()).slots()));

        return false;
    }

    private static boolean matchSlots(Entity entity, CriterionConditionItem criterionconditionitem, IntList intlist) {
        for (int i = 0; i < intlist.size(); ++i) {
            int j = intlist.getInt(i);
            SlotAccess slotaccess = entity.getSlot(j);

            if (criterionconditionitem.test(slotaccess.get())) {
                return true;
            }
        }

        return false;
    }
}
