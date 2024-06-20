package net.minecraft.world.entity;

import net.minecraft.util.INamable;
import net.minecraft.world.item.ItemStack;

public enum EnumItemSlot implements INamable {

    MAINHAND(EnumItemSlot.Function.HAND, 0, 0, "mainhand"), OFFHAND(EnumItemSlot.Function.HAND, 1, 5, "offhand"), FEET(EnumItemSlot.Function.HUMANOID_ARMOR, 0, 1, 1, "feet"), LEGS(EnumItemSlot.Function.HUMANOID_ARMOR, 1, 1, 2, "legs"), CHEST(EnumItemSlot.Function.HUMANOID_ARMOR, 2, 1, 3, "chest"), HEAD(EnumItemSlot.Function.HUMANOID_ARMOR, 3, 1, 4, "head"), BODY(EnumItemSlot.Function.ANIMAL_ARMOR, 0, 1, 6, "body");

    public static final int NO_COUNT_LIMIT = 0;
    public static final INamable.a<EnumItemSlot> CODEC = INamable.fromEnum(EnumItemSlot::values);
    private final EnumItemSlot.Function type;
    private final int index;
    private final int countLimit;
    private final int filterFlag;
    private final String name;

    private EnumItemSlot(final EnumItemSlot.Function enumitemslot_function, final int i, final int j, final int k, final String s) {
        this.type = enumitemslot_function;
        this.index = i;
        this.countLimit = j;
        this.filterFlag = k;
        this.name = s;
    }

    private EnumItemSlot(final EnumItemSlot.Function enumitemslot_function, final int i, final int j, final String s) {
        this(enumitemslot_function, i, 0, j, s);
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

    public ItemStack limit(ItemStack itemstack) {
        return this.countLimit > 0 ? itemstack.split(this.countLimit) : itemstack;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == EnumItemSlot.Function.HUMANOID_ARMOR || this.type == EnumItemSlot.Function.ANIMAL_ARMOR;
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

    public static enum Function {

        HAND, HUMANOID_ARMOR, ANIMAL_ARMOR;

        private Function() {}
    }
}
