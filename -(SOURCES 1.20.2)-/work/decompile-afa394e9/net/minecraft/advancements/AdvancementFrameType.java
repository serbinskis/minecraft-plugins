package net.minecraft.advancements;

import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;

public enum AdvancementFrameType {

    TASK("task", EnumChatFormat.GREEN), CHALLENGE("challenge", EnumChatFormat.DARK_PURPLE), GOAL("goal", EnumChatFormat.GREEN);

    private final String name;
    private final EnumChatFormat chatColor;
    private final IChatBaseComponent displayName;

    private AdvancementFrameType(String s, EnumChatFormat enumchatformat) {
        this.name = s;
        this.chatColor = enumchatformat;
        this.displayName = IChatBaseComponent.translatable("advancements.toast." + s);
    }

    public String getName() {
        return this.name;
    }

    public static AdvancementFrameType byName(String s) {
        AdvancementFrameType[] aadvancementframetype = values();
        int i = aadvancementframetype.length;

        for (int j = 0; j < i; ++j) {
            AdvancementFrameType advancementframetype = aadvancementframetype[j];

            if (advancementframetype.name.equals(s)) {
                return advancementframetype;
            }
        }

        throw new IllegalArgumentException("Unknown frame type '" + s + "'");
    }

    public EnumChatFormat getChatColor() {
        return this.chatColor;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }
}
