package net.minecraft.world.level.block.state.properties;

import net.minecraft.core.EnumDirection;
import net.minecraft.util.INamable;

public enum BlockPropertyDoubleBlockHalf implements INamable {

    UPPER(EnumDirection.DOWN), LOWER(EnumDirection.UP);

    private final EnumDirection directionToOther;

    private BlockPropertyDoubleBlockHalf(final EnumDirection enumdirection) {
        this.directionToOther = enumdirection;
    }

    public EnumDirection getDirectionToOther() {
        return this.directionToOther;
    }

    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this == BlockPropertyDoubleBlockHalf.UPPER ? "upper" : "lower";
    }

    public BlockPropertyDoubleBlockHalf getOtherHalf() {
        return this == BlockPropertyDoubleBlockHalf.UPPER ? BlockPropertyDoubleBlockHalf.LOWER : BlockPropertyDoubleBlockHalf.UPPER;
    }
}
