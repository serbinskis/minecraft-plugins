package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ScoreAccess {

    int get();

    void set(int i);

    default int add(int i) {
        int j = this.get() + i;

        this.set(j);
        return j;
    }

    default int increment() {
        return this.add(1);
    }

    default void reset() {
        this.set(0);
    }

    boolean locked();

    void unlock();

    void lock();

    @Nullable
    IChatBaseComponent display();

    void display(@Nullable IChatBaseComponent ichatbasecomponent);

    void numberFormatOverride(@Nullable NumberFormat numberformat);
}
