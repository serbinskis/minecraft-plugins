package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.INamable;

public interface SlotRange extends INamable {

    IntList slots();

    default int size() {
        return this.slots().size();
    }

    static SlotRange of(final String s, final IntList intlist) {
        return new SlotRange() {
            @Override
            public IntList slots() {
                return intlist;
            }

            @Override
            public String getSerializedName() {
                return s;
            }

            public String toString() {
                return s;
            }
        };
    }
}
