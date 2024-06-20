package net.minecraft.util;

import com.mojang.serialization.Codec;

public enum Unit {

    INSTANCE;

    public static final Codec<Unit> CODEC = Codec.unit(Unit.INSTANCE);

    private Unit() {}
}
