package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.server.level.EntityPlayer;

@FunctionalInterface
public interface ChatDecorator {

    ChatDecorator PLAIN = (entityplayer, ichatbasecomponent) -> {
        return ichatbasecomponent;
    };

    IChatBaseComponent decorate(@Nullable EntityPlayer entityplayer, IChatBaseComponent ichatbasecomponent);
}
