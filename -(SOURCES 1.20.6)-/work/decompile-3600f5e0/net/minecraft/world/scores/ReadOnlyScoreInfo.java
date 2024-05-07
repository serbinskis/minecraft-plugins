package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ReadOnlyScoreInfo {

    int value();

    boolean isLocked();

    @Nullable
    NumberFormat numberFormat();

    default IChatMutableComponent formatValue(NumberFormat numberformat) {
        return ((NumberFormat) Objects.requireNonNullElse(this.numberFormat(), numberformat)).format(this.value());
    }

    static IChatMutableComponent safeFormatValue(@Nullable ReadOnlyScoreInfo readonlyscoreinfo, NumberFormat numberformat) {
        return readonlyscoreinfo != null ? readonlyscoreinfo.formatValue(numberformat) : numberformat.format(0);
    }
}
