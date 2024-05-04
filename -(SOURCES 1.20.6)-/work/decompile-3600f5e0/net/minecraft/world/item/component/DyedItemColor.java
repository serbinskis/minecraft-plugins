package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.ColorUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public record DyedItemColor(int rgb, boolean showInTooltip) implements TooltipProvider {

    private static final Codec<DyedItemColor> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.INT.fieldOf("rgb").forGetter(DyedItemColor::rgb), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(DyedItemColor::showInTooltip)).apply(instance, DyedItemColor::new);
    });
    public static final Codec<DyedItemColor> CODEC = Codec.withAlternative(DyedItemColor.FULL_CODEC, Codec.INT, (integer) -> {
        return new DyedItemColor(integer, true);
    });
    public static final StreamCodec<ByteBuf, DyedItemColor> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, DyedItemColor::rgb, ByteBufCodecs.BOOL, DyedItemColor::showInTooltip, DyedItemColor::new);
    public static final int LEATHER_COLOR = -6265536;

    public static int getOrDefault(ItemStack itemstack, int i) {
        DyedItemColor dyeditemcolor = (DyedItemColor) itemstack.get(DataComponents.DYED_COLOR);

        return dyeditemcolor != null ? ColorUtil.b.opaque(dyeditemcolor.rgb()) : i;
    }

    public static ItemStack applyDyes(ItemStack itemstack, List<ItemDye> list) {
        if (!itemstack.is(TagsItem.DYEABLE)) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack1 = itemstack.copyWithCount(1);
            int i = 0;
            int j = 0;
            int k = 0;
            int l = 0;
            int i1 = 0;
            DyedItemColor dyeditemcolor = (DyedItemColor) itemstack1.get(DataComponents.DYED_COLOR);
            int j1;
            int k1;
            int l1;

            if (dyeditemcolor != null) {
                j1 = ColorUtil.b.red(dyeditemcolor.rgb());
                k1 = ColorUtil.b.green(dyeditemcolor.rgb());
                l1 = ColorUtil.b.blue(dyeditemcolor.rgb());
                l += Math.max(j1, Math.max(k1, l1));
                i += j1;
                j += k1;
                k += l1;
                ++i1;
            }

            int i2;

            for (Iterator iterator = list.iterator(); iterator.hasNext(); ++i1) {
                ItemDye itemdye = (ItemDye) iterator.next();
                float[] afloat = itemdye.getDyeColor().getTextureDiffuseColors();
                int j2 = (int) (afloat[0] * 255.0F);
                int k2 = (int) (afloat[1] * 255.0F);

                i2 = (int) (afloat[2] * 255.0F);
                l += Math.max(j2, Math.max(k2, i2));
                i += j2;
                j += k2;
                k += i2;
            }

            j1 = i / i1;
            k1 = j / i1;
            l1 = k / i1;
            float f = (float) l / (float) i1;
            float f1 = (float) Math.max(j1, Math.max(k1, l1));

            j1 = (int) ((float) j1 * f / f1);
            k1 = (int) ((float) k1 * f / f1);
            l1 = (int) ((float) l1 * f / f1);
            i2 = ColorUtil.b.color(0, j1, k1, l1);
            boolean flag = dyeditemcolor == null || dyeditemcolor.showInTooltip();

            itemstack1.set(DataComponents.DYED_COLOR, new DyedItemColor(i2, flag));
            return itemstack1;
        }
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        if (this.showInTooltip) {
            if (tooltipflag.isAdvanced()) {
                consumer.accept(IChatBaseComponent.translatable("item.color", String.format(Locale.ROOT, "#%06X", this.rgb)).withStyle(EnumChatFormat.GRAY));
            } else {
                consumer.accept(IChatBaseComponent.translatable("item.dyed").withStyle(EnumChatFormat.GRAY, EnumChatFormat.ITALIC));
            }

        }
    }

    public DyedItemColor withTooltip(boolean flag) {
        return new DyedItemColor(this.rgb, flag);
    }
}
