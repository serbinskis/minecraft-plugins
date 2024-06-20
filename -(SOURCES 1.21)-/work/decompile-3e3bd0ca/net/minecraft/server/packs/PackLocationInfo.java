package net.minecraft.server.packs;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Optional;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackSource;

public record PackLocationInfo(String id, IChatBaseComponent title, PackSource source, Optional<KnownPack> knownPackInfo) {

    public IChatBaseComponent createChatLink(boolean flag, IChatBaseComponent ichatbasecomponent) {
        return ChatComponentUtils.wrapInSquareBrackets(this.source.decorate(IChatBaseComponent.literal(this.id))).withStyle((chatmodifier) -> {
            return chatmodifier.withColor(flag ? EnumChatFormat.GREEN : EnumChatFormat.RED).withInsertion(StringArgumentType.escapeIfRequired(this.id)).withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, IChatBaseComponent.empty().append(this.title).append("\n").append(ichatbasecomponent)));
        });
    }
}
