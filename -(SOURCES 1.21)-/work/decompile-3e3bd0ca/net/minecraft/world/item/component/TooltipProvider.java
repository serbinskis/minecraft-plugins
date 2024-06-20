package net.minecraft.world.item.component;

import java.util.function.Consumer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public interface TooltipProvider {

    void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag);
}
