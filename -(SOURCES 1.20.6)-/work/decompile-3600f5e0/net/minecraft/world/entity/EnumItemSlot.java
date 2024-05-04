package net.minecraft.world.entity;

import net.minecraft.util.INamable;

public enum EnumItemSlot implements INamable {

    MAINHAND(EnumItemSlot.Function.HAND, 0, 0, "mainhand"), OFFHAND(EnumItemSlot.Function.HAND, 1, 5, "offhand"), FEET(EnumItemSlot.Function.ARMOR, 0, 1, "feet"), LEGS(EnumItemSlot.Function.ARMOR, 1, 2, "legs"), CHEST(EnumItemSlot.Function.ARMOR, 2, 3, "chest"), HEAD(EnumItemSlot.Function.ARMOR, 3, 4, "head"), BODY(EnumItemSlot.Function.BODY, 0, 6, "body");

    public static final INamable.a<EnumItemSlot> CODEC = INamable.fromEnum(EnumItemSlot::values);
    private final EnumItemSlot.Function type;
    private final int index;
    private final int filterFlag;
    private final String name;

    private EnumItemSlot(final EnumItemSlot.Function enumitemslot_function, final int i, final int j, final String s) {
        this.type = enumitemslot_function;
        this.index = i;
        this.filterFlag = j;
        this.name = s;
    }

    public EnumItemSlot.Function getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getIndex(int i) {
        return i + this.index;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == EnumItemSlot.Function.ARMOR;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static EnumItemSlot byName(String s) {
        EnumItemSlot enumitemslot = (EnumItemSlot) EnumItemSlot.CODEC.byName(s);

        if (enumitemslot != null) {
            return enumitemslot;
        } else {
            throw new IllegalArgumentException("Invalid slot '" + s + "'");
        }
    }

    public static EnumItemSlot byTypeAndIndex(EnumItemSlot.Function enumitemslot_function, int i) {
        EnumItemSlot[] aenumitemslot = values();
        int j = aenumitemslot.length;

        for (int k = 0; k < j; ++k) {
            EnumItemSlot enumitemslot = aenumitemslot[k];

            if (enumitemslot.getType() == enumitemslot_function && enumitemslot.getIndex() == i) {
                return enumitemslot;
            }
        }

        String s = String.valueOf(enumitemslot_function);

        throw new IllegalArgumentException("Invalid slot '" + s + "': " + i);
    }

    public static enum Function {

        HAND, ARMOR, BODY;

        private Function() {}
    }
}
