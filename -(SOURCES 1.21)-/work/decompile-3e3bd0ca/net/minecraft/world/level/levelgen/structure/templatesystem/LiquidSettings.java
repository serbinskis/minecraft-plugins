package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.util.INamable;

public enum LiquidSettings implements INamable {

    IGNORE_WATERLOGGING("ignore_waterlogging"), APPLY_WATERLOGGING("apply_waterlogging");

    public static Codec<LiquidSettings> CODEC = INamable.fromValues(LiquidSettings::values);
    private final String name;

    private LiquidSettings(final String s) {
        this.name = s;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
