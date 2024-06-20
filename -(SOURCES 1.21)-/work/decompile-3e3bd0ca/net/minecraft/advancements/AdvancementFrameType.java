package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.INamable;

public enum AdvancementFrameType implements INamable {

    TASK("task", EnumChatFormat.GREEN), CHALLENGE("challenge", EnumChatFormat.DARK_PURPLE), GOAL("goal", EnumChatFormat.GREEN);

    public static final Codec<AdvancementFrameType> CODEC = INamable.fromEnum(AdvancementFrameType::values);
    private final String name;
    private final EnumChatFormat chatColor;
    private final IChatBaseComponent displayName;

    private AdvancementFrameType(final String s, final EnumChatFormat enumchatformat) {
        this.name = s;
        this.chatColor = enumchatformat;
        this.displayName = IChatBaseComponent.translatable("advancements.toast." + s);
    }

    public EnumChatFormat getChatColor() {
        return this.chatColor;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public IChatMutableComponent createAnnouncement(AdvancementHolder advancementholder, EntityPlayer entityplayer) {
        return IChatBaseComponent.translatable("chat.type.advancement." + this.name, entityplayer.getDisplayName(), Advancement.name(advancementholder));
    }
}
