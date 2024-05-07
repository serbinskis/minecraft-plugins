package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.util.OptionEnum;

public enum EnumMainHand implements OptionEnum, INamable {

    LEFT(0, "left", "options.mainHand.left"), RIGHT(1, "right", "options.mainHand.right");

    public static final Codec<EnumMainHand> CODEC = INamable.fromEnum(EnumMainHand::values);
    public static final IntFunction<EnumMainHand> BY_ID = ByIdMap.continuous(EnumMainHand::getId, values(), ByIdMap.a.ZERO);
    private final int id;
    private final String name;
    private final String translationKey;

    private EnumMainHand(final int i, final String s, final String s1) {
        this.id = i;
        this.name = s;
        this.translationKey = s1;
    }

    public EnumMainHand getOpposite() {
        return this == EnumMainHand.LEFT ? EnumMainHand.RIGHT : EnumMainHand.LEFT;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.translationKey;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
