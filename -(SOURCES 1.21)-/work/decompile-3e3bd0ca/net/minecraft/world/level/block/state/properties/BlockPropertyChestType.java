package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.INamable;

public enum BlockPropertyChestType implements INamable {

    SINGLE("single"), LEFT("left"), RIGHT("right");

    private final String name;

    private BlockPropertyChestType(final String s) {
        this.name = s;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public BlockPropertyChestType getOpposite() {
        BlockPropertyChestType blockpropertychesttype;

        switch (this.ordinal()) {
            case 0:
                blockpropertychesttype = BlockPropertyChestType.SINGLE;
                break;
            case 1:
                blockpropertychesttype = BlockPropertyChestType.RIGHT;
                break;
            case 2:
                blockpropertychesttype = BlockPropertyChestType.LEFT;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return blockpropertychesttype;
    }
}
