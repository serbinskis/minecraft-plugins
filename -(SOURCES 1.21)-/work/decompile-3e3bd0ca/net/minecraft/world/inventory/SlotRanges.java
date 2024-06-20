package net.minecraft.world.inventory;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.EnumItemSlot;

public class SlotRanges {

    private static final List<SlotRange> SLOTS = (List) SystemUtils.make(new ArrayList(), (arraylist) -> {
        addSingleSlot(arraylist, "contents", 0);
        addSlotRange(arraylist, "container.", 0, 54);
        addSlotRange(arraylist, "hotbar.", 0, 9);
        addSlotRange(arraylist, "inventory.", 9, 27);
        addSlotRange(arraylist, "enderchest.", 200, 27);
        addSlotRange(arraylist, "villager.", 300, 8);
        addSlotRange(arraylist, "horse.", 500, 15);
        int i = EnumItemSlot.MAINHAND.getIndex(98);
        int j = EnumItemSlot.OFFHAND.getIndex(98);

        addSingleSlot(arraylist, "weapon", i);
        addSingleSlot(arraylist, "weapon.mainhand", i);
        addSingleSlot(arraylist, "weapon.offhand", j);
        addSlots(arraylist, "weapon.*", i, j);
        i = EnumItemSlot.HEAD.getIndex(100);
        j = EnumItemSlot.CHEST.getIndex(100);
        int k = EnumItemSlot.LEGS.getIndex(100);
        int l = EnumItemSlot.FEET.getIndex(100);
        int i1 = EnumItemSlot.BODY.getIndex(105);

        addSingleSlot(arraylist, "armor.head", i);
        addSingleSlot(arraylist, "armor.chest", j);
        addSingleSlot(arraylist, "armor.legs", k);
        addSingleSlot(arraylist, "armor.feet", l);
        addSingleSlot(arraylist, "armor.body", i1);
        addSlots(arraylist, "armor.*", i, j, k, l, i1);
        addSingleSlot(arraylist, "horse.saddle", 400);
        addSingleSlot(arraylist, "horse.chest", 499);
        addSingleSlot(arraylist, "player.cursor", 499);
        addSlotRange(arraylist, "player.crafting.", 500, 4);
    });
    public static final Codec<SlotRange> CODEC = INamable.fromValues(() -> {
        return (SlotRange[]) SlotRanges.SLOTS.toArray(new SlotRange[0]);
    });
    private static final Function<String, SlotRange> NAME_LOOKUP = INamable.createNameLookup((SlotRange[]) SlotRanges.SLOTS.toArray(new SlotRange[0]), (s) -> {
        return s;
    });

    public SlotRanges() {}

    private static SlotRange create(String s, int i) {
        return SlotRange.of(s, IntLists.singleton(i));
    }

    private static SlotRange create(String s, IntList intlist) {
        return SlotRange.of(s, IntLists.unmodifiable(intlist));
    }

    private static SlotRange create(String s, int... aint) {
        return SlotRange.of(s, IntList.of(aint));
    }

    private static void addSingleSlot(List<SlotRange> list, String s, int i) {
        list.add(create(s, i));
    }

    private static void addSlotRange(List<SlotRange> list, String s, int i, int j) {
        IntArrayList intarraylist = new IntArrayList(j);

        for (int k = 0; k < j; ++k) {
            int l = i + k;

            list.add(create(s + k, l));
            intarraylist.add(l);
        }

        list.add(create(s + "*", (IntList) intarraylist));
    }

    private static void addSlots(List<SlotRange> list, String s, int... aint) {
        list.add(create(s, aint));
    }

    @Nullable
    public static SlotRange nameToIds(String s) {
        return (SlotRange) SlotRanges.NAME_LOOKUP.apply(s);
    }

    public static Stream<String> allNames() {
        return SlotRanges.SLOTS.stream().map(INamable::getSerializedName);
    }

    public static Stream<String> singleSlotNames() {
        return SlotRanges.SLOTS.stream().filter((slotrange) -> {
            return slotrange.size() == 1;
        }).map(INamable::getSerializedName);
    }
}
