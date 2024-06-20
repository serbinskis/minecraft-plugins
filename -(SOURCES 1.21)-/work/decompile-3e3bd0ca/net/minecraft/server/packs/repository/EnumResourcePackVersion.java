package net.minecraft.server.packs.repository;

import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.InclusiveRange;

public enum EnumResourcePackVersion {

    TOO_OLD("old"), TOO_NEW("new"), COMPATIBLE("compatible");

    private final IChatBaseComponent description;
    private final IChatBaseComponent confirmation;

    private EnumResourcePackVersion(final String s) {
        this.description = IChatBaseComponent.translatable("pack.incompatible." + s).withStyle(EnumChatFormat.GRAY);
        this.confirmation = IChatBaseComponent.translatable("pack.incompatible.confirm." + s);
    }

    public boolean isCompatible() {
        return this == EnumResourcePackVersion.COMPATIBLE;
    }

    public static EnumResourcePackVersion forVersion(InclusiveRange<Integer> inclusiverange, int i) {
        return (Integer) inclusiverange.maxInclusive() < i ? EnumResourcePackVersion.TOO_OLD : (i < (Integer) inclusiverange.minInclusive() ? EnumResourcePackVersion.TOO_NEW : EnumResourcePackVersion.COMPATIBLE);
    }

    public IChatBaseComponent getDescription() {
        return this.description;
    }

    public IChatBaseComponent getConfirmation() {
        return this.confirmation;
    }
}
