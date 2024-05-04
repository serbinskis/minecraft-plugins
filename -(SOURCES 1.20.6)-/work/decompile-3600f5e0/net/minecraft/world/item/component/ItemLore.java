package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record ItemLore(List<IChatBaseComponent> lines, List<IChatBaseComponent> styledLines) implements TooltipProvider {

    public static final ItemLore EMPTY = new ItemLore(List.of());
    public static final int MAX_LINES = 256;
    private static final ChatModifier LORE_STYLE = ChatModifier.EMPTY.withColor(EnumChatFormat.DARK_PURPLE).withItalic(true);
    public static final Codec<ItemLore> CODEC = ComponentSerialization.FLAT_CODEC.sizeLimitedListOf(256).xmap(ItemLore::new, ItemLore::lines);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemLore> STREAM_CODEC = ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list(256)).map(ItemLore::new, ItemLore::lines);

    public ItemLore(List<IChatBaseComponent> list) {
        this(list, Lists.transform(list, (ichatbasecomponent) -> {
            return ChatComponentUtils.mergeStyles(ichatbasecomponent.copy(), ItemLore.LORE_STYLE);
        }));
    }

    public ItemLore(List<IChatBaseComponent> lines, List<IChatBaseComponent> styledLines) {
        if (lines.size() > 256) {
            throw new IllegalArgumentException("Got " + lines.size() + " lines, but maximum is 256");
        } else {
            this.lines = lines;
            this.styledLines = styledLines;
        }
    }

    public ItemLore withLineAdded(IChatBaseComponent ichatbasecomponent) {
        return new ItemLore(SystemUtils.copyAndAdd(this.lines, (Object) ichatbasecomponent));
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        this.styledLines.forEach(consumer);
    }
}
