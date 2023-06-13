package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.network.chat.ChatClickable;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FormattedString;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.EnumColor;

public class SignText {

    private static final Codec<IChatBaseComponent[]> LINES_CODEC = ExtraCodecs.FLAT_COMPONENT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 4).map((list1) -> {
            return new IChatBaseComponent[]{(IChatBaseComponent) list1.get(0), (IChatBaseComponent) list1.get(1), (IChatBaseComponent) list1.get(2), (IChatBaseComponent) list1.get(3)};
        });
    }, (aichatbasecomponent) -> {
        return List.of(aichatbasecomponent[0], aichatbasecomponent[1], aichatbasecomponent[2], aichatbasecomponent[3]);
    });
    public static final Codec<SignText> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(SignText.LINES_CODEC.fieldOf("messages").forGetter((signtext) -> {
            return signtext.messages;
        }), SignText.LINES_CODEC.optionalFieldOf("filtered_messages").forGetter(SignText::getOnlyFilteredMessages), EnumColor.CODEC.fieldOf("color").orElse(EnumColor.BLACK).forGetter((signtext) -> {
            return signtext.color;
        }), Codec.BOOL.fieldOf("has_glowing_text").orElse(false).forGetter((signtext) -> {
            return signtext.hasGlowingText;
        })).apply(instance, SignText::load);
    });
    public static final int LINES = 4;
    private final IChatBaseComponent[] messages;
    private final IChatBaseComponent[] filteredMessages;
    private final EnumColor color;
    private final boolean hasGlowingText;
    @Nullable
    private FormattedString[] renderMessages;
    private boolean renderMessagedFiltered;

    public SignText() {
        this(emptyMessages(), emptyMessages(), EnumColor.BLACK, false);
    }

    public SignText(IChatBaseComponent[] aichatbasecomponent, IChatBaseComponent[] aichatbasecomponent1, EnumColor enumcolor, boolean flag) {
        this.messages = aichatbasecomponent;
        this.filteredMessages = aichatbasecomponent1;
        this.color = enumcolor;
        this.hasGlowingText = flag;
    }

    private static IChatBaseComponent[] emptyMessages() {
        return new IChatBaseComponent[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
    }

    private static SignText load(IChatBaseComponent[] aichatbasecomponent, Optional<IChatBaseComponent[]> optional, EnumColor enumcolor, boolean flag) {
        IChatBaseComponent[] aichatbasecomponent1 = (IChatBaseComponent[]) optional.orElseGet(SignText::emptyMessages);

        populateFilteredMessagesWithRawMessages(aichatbasecomponent, aichatbasecomponent1);
        return new SignText(aichatbasecomponent, aichatbasecomponent1, enumcolor, flag);
    }

    private static void populateFilteredMessagesWithRawMessages(IChatBaseComponent[] aichatbasecomponent, IChatBaseComponent[] aichatbasecomponent1) {
        for (int i = 0; i < 4; ++i) {
            if (aichatbasecomponent1[i].equals(CommonComponents.EMPTY)) {
                aichatbasecomponent1[i] = aichatbasecomponent[i];
            }
        }

    }

    public boolean hasGlowingText() {
        return this.hasGlowingText;
    }

    public SignText setHasGlowingText(boolean flag) {
        return flag == this.hasGlowingText ? this : new SignText(this.messages, this.filteredMessages, this.color, flag);
    }

    public EnumColor getColor() {
        return this.color;
    }

    public SignText setColor(EnumColor enumcolor) {
        return enumcolor == this.getColor() ? this : new SignText(this.messages, this.filteredMessages, enumcolor, this.hasGlowingText);
    }

    public IChatBaseComponent getMessage(int i, boolean flag) {
        return this.getMessages(flag)[i];
    }

    public SignText setMessage(int i, IChatBaseComponent ichatbasecomponent) {
        return this.setMessage(i, ichatbasecomponent, ichatbasecomponent);
    }

    public SignText setMessage(int i, IChatBaseComponent ichatbasecomponent, IChatBaseComponent ichatbasecomponent1) {
        IChatBaseComponent[] aichatbasecomponent = (IChatBaseComponent[]) Arrays.copyOf(this.messages, this.messages.length);
        IChatBaseComponent[] aichatbasecomponent1 = (IChatBaseComponent[]) Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);

        aichatbasecomponent[i] = ichatbasecomponent;
        aichatbasecomponent1[i] = ichatbasecomponent1;
        return new SignText(aichatbasecomponent, aichatbasecomponent1, this.color, this.hasGlowingText);
    }

    public boolean hasMessage(EntityHuman entityhuman) {
        return Arrays.stream(this.getMessages(entityhuman.isTextFilteringEnabled())).anyMatch((ichatbasecomponent) -> {
            return !ichatbasecomponent.getString().isEmpty();
        });
    }

    public IChatBaseComponent[] getMessages(boolean flag) {
        return flag ? this.filteredMessages : this.messages;
    }

    public FormattedString[] getRenderMessages(boolean flag, Function<IChatBaseComponent, FormattedString> function) {
        if (this.renderMessages == null || this.renderMessagedFiltered != flag) {
            this.renderMessagedFiltered = flag;
            this.renderMessages = new FormattedString[4];

            for (int i = 0; i < 4; ++i) {
                this.renderMessages[i] = (FormattedString) function.apply(this.getMessage(i, flag));
            }
        }

        return this.renderMessages;
    }

    private Optional<IChatBaseComponent[]> getOnlyFilteredMessages() {
        IChatBaseComponent[] aichatbasecomponent = new IChatBaseComponent[4];
        boolean flag = false;

        for (int i = 0; i < 4; ++i) {
            IChatBaseComponent ichatbasecomponent = this.filteredMessages[i];

            if (!ichatbasecomponent.equals(this.messages[i])) {
                aichatbasecomponent[i] = ichatbasecomponent;
                flag = true;
            } else {
                aichatbasecomponent[i] = CommonComponents.EMPTY;
            }
        }

        return flag ? Optional.of(aichatbasecomponent) : Optional.empty();
    }

    public boolean hasAnyClickCommands(EntityHuman entityhuman) {
        IChatBaseComponent[] aichatbasecomponent = this.getMessages(entityhuman.isTextFilteringEnabled());
        int i = aichatbasecomponent.length;

        for (int j = 0; j < i; ++j) {
            IChatBaseComponent ichatbasecomponent = aichatbasecomponent[j];
            ChatModifier chatmodifier = ichatbasecomponent.getStyle();
            ChatClickable chatclickable = chatmodifier.getClickEvent();

            if (chatclickable != null && chatclickable.getAction() == ChatClickable.EnumClickAction.RUN_COMMAND) {
                return true;
            }
        }

        return false;
    }
}
