package net.minecraft.network.protocol.game;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayOutTabComplete(int id, int start, int length, List<PacketPlayOutTabComplete.a> suggestions) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutTabComplete> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayOutTabComplete::id, ByteBufCodecs.VAR_INT, PacketPlayOutTabComplete::start, ByteBufCodecs.VAR_INT, PacketPlayOutTabComplete::length, PacketPlayOutTabComplete.a.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketPlayOutTabComplete::suggestions, PacketPlayOutTabComplete::new);

    public PacketPlayOutTabComplete(int i, Suggestions suggestions) {
        this(i, suggestions.getRange().getStart(), suggestions.getRange().getLength(), suggestions.getList().stream().map((suggestion) -> {
            return new PacketPlayOutTabComplete.a(suggestion.getText(), Optional.ofNullable(suggestion.getTooltip()).map(ChatComponentUtils::fromMessage));
        }).toList());
    }

    @Override
    public PacketType<PacketPlayOutTabComplete> type() {
        return GamePacketTypes.CLIENTBOUND_COMMAND_SUGGESTIONS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleCommandSuggestions(this);
    }

    public Suggestions toSuggestions() {
        StringRange stringrange = StringRange.between(this.start, this.start + this.length);

        return new Suggestions(stringrange, this.suggestions.stream().map((packetplayouttabcomplete_a) -> {
            return new Suggestion(stringrange, packetplayouttabcomplete_a.text(), (Message) packetplayouttabcomplete_a.tooltip().orElse((Object) null));
        }).toList());
    }

    public static record a(String text, Optional<IChatBaseComponent> tooltip) {

        public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutTabComplete.a> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PacketPlayOutTabComplete.a::text, ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC, PacketPlayOutTabComplete.a::tooltip, PacketPlayOutTabComplete.a::new);
    }
}
