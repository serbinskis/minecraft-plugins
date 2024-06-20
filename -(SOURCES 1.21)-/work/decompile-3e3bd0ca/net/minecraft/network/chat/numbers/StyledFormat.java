package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class StyledFormat implements NumberFormat {

    public static final NumberFormatType<StyledFormat> TYPE = new NumberFormatType<StyledFormat>() {
        private static final MapCodec<StyledFormat> CODEC = ChatModifier.ChatModifierSerializer.MAP_CODEC.xmap(StyledFormat::new, (styledformat) -> {
            return styledformat.style;
        });
        private static final StreamCodec<RegistryFriendlyByteBuf, StyledFormat> STREAM_CODEC = StreamCodec.composite(ChatModifier.ChatModifierSerializer.TRUSTED_STREAM_CODEC, (styledformat) -> {
            return styledformat.style;
        }, StyledFormat::new);

        @Override
        public MapCodec<StyledFormat> mapCodec() {
            return null.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StyledFormat> streamCodec() {
            return null.STREAM_CODEC;
        }
    };
    public static final StyledFormat NO_STYLE = new StyledFormat(ChatModifier.EMPTY);
    public static final StyledFormat SIDEBAR_DEFAULT = new StyledFormat(ChatModifier.EMPTY.withColor(EnumChatFormat.RED));
    public static final StyledFormat PLAYER_LIST_DEFAULT = new StyledFormat(ChatModifier.EMPTY.withColor(EnumChatFormat.YELLOW));
    final ChatModifier style;

    public StyledFormat(ChatModifier chatmodifier) {
        this.style = chatmodifier;
    }

    @Override
    public IChatMutableComponent format(int i) {
        return IChatBaseComponent.literal(Integer.toString(i)).withStyle(this.style);
    }

    @Override
    public NumberFormatType<StyledFormat> type() {
        return StyledFormat.TYPE;
    }
}
