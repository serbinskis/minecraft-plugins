package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public record PlayerScoreEntry(String owner, int value, @Nullable IChatBaseComponent display, @Nullable NumberFormat numberFormatOverride) {

    public boolean isHidden() {
        return this.owner.startsWith("#");
    }

    public IChatBaseComponent ownerName() {
        return (IChatBaseComponent) (this.display != null ? this.display : IChatBaseComponent.literal(this.owner()));
    }

    public IChatMutableComponent formatValue(NumberFormat numberformat) {
        return ((NumberFormat) Objects.requireNonNullElse(this.numberFormatOverride, numberformat)).format(this.value);
    }
}
