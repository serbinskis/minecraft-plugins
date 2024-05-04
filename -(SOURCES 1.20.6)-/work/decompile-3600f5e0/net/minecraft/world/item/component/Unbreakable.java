package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record Unbreakable(boolean showInTooltip) implements TooltipProvider {

    public static final Codec<Unbreakable> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(Unbreakable::showInTooltip)).apply(instance, Unbreakable::new);
    });
    public static final StreamCodec<ByteBuf, Unbreakable> STREAM_CODEC = ByteBufCodecs.BOOL.map(Unbreakable::new, Unbreakable::showInTooltip);
    private static final IChatBaseComponent TOOLTIP = IChatBaseComponent.translatable("item.unbreakable").withStyle(EnumChatFormat.BLUE);

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        if (this.showInTooltip) {
            consumer.accept(Unbreakable.TOOLTIP);
        }

    }

    public Unbreakable withTooltip(boolean flag) {
        return new Unbreakable(flag);
    }
}
