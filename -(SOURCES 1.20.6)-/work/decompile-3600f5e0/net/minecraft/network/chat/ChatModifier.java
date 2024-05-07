package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;

public class ChatModifier {

    public static final ChatModifier EMPTY = new ChatModifier((ChatHexColor) null, (Boolean) null, (Boolean) null, (Boolean) null, (Boolean) null, (Boolean) null, (ChatClickable) null, (ChatHoverable) null, (String) null, (MinecraftKey) null);
    public static final MinecraftKey DEFAULT_FONT = new MinecraftKey("minecraft", "default");
    @Nullable
    final ChatHexColor color;
    @Nullable
    final Boolean bold;
    @Nullable
    final Boolean italic;
    @Nullable
    final Boolean underlined;
    @Nullable
    final Boolean strikethrough;
    @Nullable
    final Boolean obfuscated;
    @Nullable
    final ChatClickable clickEvent;
    @Nullable
    final ChatHoverable hoverEvent;
    @Nullable
    final String insertion;
    @Nullable
    final MinecraftKey font;

    private static ChatModifier create(Optional<ChatHexColor> optional, Optional<Boolean> optional1, Optional<Boolean> optional2, Optional<Boolean> optional3, Optional<Boolean> optional4, Optional<Boolean> optional5, Optional<ChatClickable> optional6, Optional<ChatHoverable> optional7, Optional<String> optional8, Optional<MinecraftKey> optional9) {
        ChatModifier chatmodifier = new ChatModifier((ChatHexColor) optional.orElse((Object) null), (Boolean) optional1.orElse((Object) null), (Boolean) optional2.orElse((Object) null), (Boolean) optional3.orElse((Object) null), (Boolean) optional4.orElse((Object) null), (Boolean) optional5.orElse((Object) null), (ChatClickable) optional6.orElse((Object) null), (ChatHoverable) optional7.orElse((Object) null), (String) optional8.orElse((Object) null), (MinecraftKey) optional9.orElse((Object) null));

        return chatmodifier.equals(ChatModifier.EMPTY) ? ChatModifier.EMPTY : chatmodifier;
    }

    private ChatModifier(@Nullable ChatHexColor chathexcolor, @Nullable Boolean obool, @Nullable Boolean obool1, @Nullable Boolean obool2, @Nullable Boolean obool3, @Nullable Boolean obool4, @Nullable ChatClickable chatclickable, @Nullable ChatHoverable chathoverable, @Nullable String s, @Nullable MinecraftKey minecraftkey) {
        this.color = chathexcolor;
        this.bold = obool;
        this.italic = obool1;
        this.underlined = obool2;
        this.strikethrough = obool3;
        this.obfuscated = obool4;
        this.clickEvent = chatclickable;
        this.hoverEvent = chathoverable;
        this.insertion = s;
        this.font = minecraftkey;
    }

    @Nullable
    public ChatHexColor getColor() {
        return this.color;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == ChatModifier.EMPTY;
    }

    @Nullable
    public ChatClickable getClickEvent() {
        return this.clickEvent;
    }

    @Nullable
    public ChatHoverable getHoverEvent() {
        return this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion;
    }

    public MinecraftKey getFont() {
        return this.font != null ? this.font : ChatModifier.DEFAULT_FONT;
    }

    private static <T> ChatModifier checkEmptyAfterChange(ChatModifier chatmodifier, @Nullable T t0, @Nullable T t1) {
        return t0 != null && t1 == null && chatmodifier.equals(ChatModifier.EMPTY) ? ChatModifier.EMPTY : chatmodifier;
    }

    public ChatModifier withColor(@Nullable ChatHexColor chathexcolor) {
        return Objects.equals(this.color, chathexcolor) ? this : checkEmptyAfterChange(new ChatModifier(chathexcolor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.color, chathexcolor);
    }

    public ChatModifier withColor(@Nullable EnumChatFormat enumchatformat) {
        return this.withColor(enumchatformat != null ? ChatHexColor.fromLegacyFormat(enumchatformat) : null);
    }

    public ChatModifier withColor(int i) {
        return this.withColor(ChatHexColor.fromRgb(i));
    }

    public ChatModifier withBold(@Nullable Boolean obool) {
        return Objects.equals(this.bold, obool) ? this : checkEmptyAfterChange(new ChatModifier(this.color, obool, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.bold, obool);
    }

    public ChatModifier withItalic(@Nullable Boolean obool) {
        return Objects.equals(this.italic, obool) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, obool, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.italic, obool);
    }

    public ChatModifier withUnderlined(@Nullable Boolean obool) {
        return Objects.equals(this.underlined, obool) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, this.italic, obool, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.underlined, obool);
    }

    public ChatModifier withStrikethrough(@Nullable Boolean obool) {
        return Objects.equals(this.strikethrough, obool) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, this.italic, this.underlined, obool, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.strikethrough, obool);
    }

    public ChatModifier withObfuscated(@Nullable Boolean obool) {
        return Objects.equals(this.obfuscated, obool) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, this.italic, this.underlined, this.strikethrough, obool, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.obfuscated, obool);
    }

    public ChatModifier withClickEvent(@Nullable ChatClickable chatclickable) {
        return Objects.equals(this.clickEvent, chatclickable) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, chatclickable, this.hoverEvent, this.insertion, this.font), this.clickEvent, chatclickable);
    }

    public ChatModifier withHoverEvent(@Nullable ChatHoverable chathoverable) {
        return Objects.equals(this.hoverEvent, chathoverable) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, chathoverable, this.insertion, this.font), this.hoverEvent, chathoverable);
    }

    public ChatModifier withInsertion(@Nullable String s) {
        return Objects.equals(this.insertion, s) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, s, this.font), this.insertion, s);
    }

    public ChatModifier withFont(@Nullable MinecraftKey minecraftkey) {
        return Objects.equals(this.font, minecraftkey) ? this : checkEmptyAfterChange(new ChatModifier(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, minecraftkey), this.font, minecraftkey);
    }

    public ChatModifier applyFormat(EnumChatFormat enumchatformat) {
        ChatHexColor chathexcolor = this.color;
        Boolean obool = this.bold;
        Boolean obool1 = this.italic;
        Boolean obool2 = this.strikethrough;
        Boolean obool3 = this.underlined;
        Boolean obool4 = this.obfuscated;

        switch (enumchatformat) {
            case OBFUSCATED:
                obool4 = true;
                break;
            case BOLD:
                obool = true;
                break;
            case STRIKETHROUGH:
                obool2 = true;
                break;
            case UNDERLINE:
                obool3 = true;
                break;
            case ITALIC:
                obool1 = true;
                break;
            case RESET:
                return ChatModifier.EMPTY;
            default:
                chathexcolor = ChatHexColor.fromLegacyFormat(enumchatformat);
        }

        return new ChatModifier(chathexcolor, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public ChatModifier applyLegacyFormat(EnumChatFormat enumchatformat) {
        ChatHexColor chathexcolor = this.color;
        Boolean obool = this.bold;
        Boolean obool1 = this.italic;
        Boolean obool2 = this.strikethrough;
        Boolean obool3 = this.underlined;
        Boolean obool4 = this.obfuscated;

        switch (enumchatformat) {
            case OBFUSCATED:
                obool4 = true;
                break;
            case BOLD:
                obool = true;
                break;
            case STRIKETHROUGH:
                obool2 = true;
                break;
            case UNDERLINE:
                obool3 = true;
                break;
            case ITALIC:
                obool1 = true;
                break;
            case RESET:
                return ChatModifier.EMPTY;
            default:
                obool4 = false;
                obool = false;
                obool2 = false;
                obool3 = false;
                obool1 = false;
                chathexcolor = ChatHexColor.fromLegacyFormat(enumchatformat);
        }

        return new ChatModifier(chathexcolor, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public ChatModifier applyFormats(EnumChatFormat... aenumchatformat) {
        ChatHexColor chathexcolor = this.color;
        Boolean obool = this.bold;
        Boolean obool1 = this.italic;
        Boolean obool2 = this.strikethrough;
        Boolean obool3 = this.underlined;
        Boolean obool4 = this.obfuscated;
        EnumChatFormat[] aenumchatformat1 = aenumchatformat;
        int i = aenumchatformat.length;

        for (int j = 0; j < i; ++j) {
            EnumChatFormat enumchatformat = aenumchatformat1[j];

            switch (enumchatformat) {
                case OBFUSCATED:
                    obool4 = true;
                    break;
                case BOLD:
                    obool = true;
                    break;
                case STRIKETHROUGH:
                    obool2 = true;
                    break;
                case UNDERLINE:
                    obool3 = true;
                    break;
                case ITALIC:
                    obool1 = true;
                    break;
                case RESET:
                    return ChatModifier.EMPTY;
                default:
                    chathexcolor = ChatHexColor.fromLegacyFormat(enumchatformat);
            }
        }

        return new ChatModifier(chathexcolor, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public ChatModifier applyTo(ChatModifier chatmodifier) {
        return this == ChatModifier.EMPTY ? chatmodifier : (chatmodifier == ChatModifier.EMPTY ? this : new ChatModifier(this.color != null ? this.color : chatmodifier.color, this.bold != null ? this.bold : chatmodifier.bold, this.italic != null ? this.italic : chatmodifier.italic, this.underlined != null ? this.underlined : chatmodifier.underlined, this.strikethrough != null ? this.strikethrough : chatmodifier.strikethrough, this.obfuscated != null ? this.obfuscated : chatmodifier.obfuscated, this.clickEvent != null ? this.clickEvent : chatmodifier.clickEvent, this.hoverEvent != null ? this.hoverEvent : chatmodifier.hoverEvent, this.insertion != null ? this.insertion : chatmodifier.insertion, this.font != null ? this.font : chatmodifier.font));
    }

    public String toString() {
        final StringBuilder stringbuilder = new StringBuilder("{");

        class a {

            private boolean isNotFirst;

            a(final ChatModifier chatmodifier) {}

            private void prependSeparator() {
                if (this.isNotFirst) {
                    stringbuilder.append(',');
                }

                this.isNotFirst = true;
            }

            void addFlagString(String s, @Nullable Boolean obool) {
                if (obool != null) {
                    this.prependSeparator();
                    if (!obool) {
                        stringbuilder.append('!');
                    }

                    stringbuilder.append(s);
                }

            }

            void addValueString(String s, @Nullable Object object) {
                if (object != null) {
                    this.prependSeparator();
                    stringbuilder.append(s);
                    stringbuilder.append('=');
                    stringbuilder.append(object);
                }

            }
        }

        a a0 = new a(this);

        a0.addValueString("color", this.color);
        a0.addFlagString("bold", this.bold);
        a0.addFlagString("italic", this.italic);
        a0.addFlagString("underlined", this.underlined);
        a0.addFlagString("strikethrough", this.strikethrough);
        a0.addFlagString("obfuscated", this.obfuscated);
        a0.addValueString("clickEvent", this.clickEvent);
        a0.addValueString("hoverEvent", this.hoverEvent);
        a0.addValueString("insertion", this.insertion);
        a0.addValueString("font", this.font);
        stringbuilder.append("}");
        return stringbuilder.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof ChatModifier)) {
            return false;
        } else {
            ChatModifier chatmodifier = (ChatModifier) object;

            return this.bold == chatmodifier.bold && Objects.equals(this.getColor(), chatmodifier.getColor()) && this.italic == chatmodifier.italic && this.obfuscated == chatmodifier.obfuscated && this.strikethrough == chatmodifier.strikethrough && this.underlined == chatmodifier.underlined && Objects.equals(this.clickEvent, chatmodifier.clickEvent) && Objects.equals(this.hoverEvent, chatmodifier.hoverEvent) && Objects.equals(this.insertion, chatmodifier.insertion) && Objects.equals(this.font, chatmodifier.font);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion});
    }

    public static class ChatModifierSerializer {

        public static final MapCodec<ChatModifier> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ChatHexColor.CODEC.optionalFieldOf("color").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.color);
            }), Codec.BOOL.optionalFieldOf("bold").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.bold);
            }), Codec.BOOL.optionalFieldOf("italic").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.italic);
            }), Codec.BOOL.optionalFieldOf("underlined").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.underlined);
            }), Codec.BOOL.optionalFieldOf("strikethrough").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.strikethrough);
            }), Codec.BOOL.optionalFieldOf("obfuscated").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.obfuscated);
            }), ChatClickable.CODEC.optionalFieldOf("clickEvent").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.clickEvent);
            }), ChatHoverable.CODEC.optionalFieldOf("hoverEvent").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.hoverEvent);
            }), Codec.STRING.optionalFieldOf("insertion").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.insertion);
            }), MinecraftKey.CODEC.optionalFieldOf("font").forGetter((chatmodifier) -> {
                return Optional.ofNullable(chatmodifier.font);
            })).apply(instance, ChatModifier::create);
        });
        public static final Codec<ChatModifier> CODEC = ChatModifier.ChatModifierSerializer.MAP_CODEC.codec();
        public static final StreamCodec<RegistryFriendlyByteBuf, ChatModifier> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(ChatModifier.ChatModifierSerializer.CODEC);

        public ChatModifierSerializer() {}
    }
}
