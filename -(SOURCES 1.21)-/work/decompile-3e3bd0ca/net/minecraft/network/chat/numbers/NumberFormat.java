package net.minecraft.network.chat.numbers;

import net.minecraft.network.chat.IChatMutableComponent;

public interface NumberFormat {

    IChatMutableComponent format(int i);

    NumberFormatType<? extends NumberFormat> type();
}
