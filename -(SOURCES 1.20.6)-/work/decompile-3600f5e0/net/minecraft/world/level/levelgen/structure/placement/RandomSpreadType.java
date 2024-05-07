package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.INamable;
import net.minecraft.util.RandomSource;

public enum RandomSpreadType implements INamable {

    LINEAR("linear"), TRIANGULAR("triangular");

    public static final Codec<RandomSpreadType> CODEC = INamable.fromEnum(RandomSpreadType::values);
    private final String id;

    private RandomSpreadType(final String s) {
        this.id = s;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public int evaluate(RandomSource randomsource, int i) {
        int j;

        switch (this.ordinal()) {
            case 0:
                j = randomsource.nextInt(i);
                break;
            case 1:
                j = (randomsource.nextInt(i) + randomsource.nextInt(i)) / 2;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return j;
    }
}
