package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record FireworkExplosion(FireworkExplosion.a shape, IntList colors, IntList fadeColors, boolean hasTrail, boolean hasTwinkle) implements TooltipProvider {

    public static final FireworkExplosion DEFAULT = new FireworkExplosion(FireworkExplosion.a.SMALL_BALL, IntList.of(), IntList.of(), false, false);
    public static final Codec<IntList> COLOR_LIST_CODEC = Codec.INT.listOf().xmap(IntArrayList::new, ArrayList::new);
    public static final Codec<FireworkExplosion> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(FireworkExplosion.a.CODEC.fieldOf("shape").forGetter(FireworkExplosion::shape), FireworkExplosion.COLOR_LIST_CODEC.optionalFieldOf("colors", IntList.of()).forGetter(FireworkExplosion::colors), FireworkExplosion.COLOR_LIST_CODEC.optionalFieldOf("fade_colors", IntList.of()).forGetter(FireworkExplosion::fadeColors), Codec.BOOL.optionalFieldOf("has_trail", false).forGetter(FireworkExplosion::hasTrail), Codec.BOOL.optionalFieldOf("has_twinkle", false).forGetter(FireworkExplosion::hasTwinkle)).apply(instance, FireworkExplosion::new);
    });
    private static final StreamCodec<ByteBuf, IntList> COLOR_LIST_STREAM_CODEC = ByteBufCodecs.INT.apply(ByteBufCodecs.list()).map(IntArrayList::new, ArrayList::new);
    public static final StreamCodec<ByteBuf, FireworkExplosion> STREAM_CODEC = StreamCodec.composite(FireworkExplosion.a.STREAM_CODEC, FireworkExplosion::shape, FireworkExplosion.COLOR_LIST_STREAM_CODEC, FireworkExplosion::colors, FireworkExplosion.COLOR_LIST_STREAM_CODEC, FireworkExplosion::fadeColors, ByteBufCodecs.BOOL, FireworkExplosion::hasTrail, ByteBufCodecs.BOOL, FireworkExplosion::hasTwinkle, FireworkExplosion::new);
    private static final IChatBaseComponent CUSTOM_COLOR_NAME = IChatBaseComponent.translatable("item.minecraft.firework_star.custom_color");

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        this.addShapeNameTooltip(consumer);
        this.addAdditionalTooltip(consumer);
    }

    public void addShapeNameTooltip(Consumer<IChatBaseComponent> consumer) {
        consumer.accept(this.shape.getName().withStyle(EnumChatFormat.GRAY));
    }

    public void addAdditionalTooltip(Consumer<IChatBaseComponent> consumer) {
        if (!this.colors.isEmpty()) {
            consumer.accept(appendColors(IChatBaseComponent.empty().withStyle(EnumChatFormat.GRAY), this.colors));
        }

        if (!this.fadeColors.isEmpty()) {
            consumer.accept(appendColors(IChatBaseComponent.translatable("item.minecraft.firework_star.fade_to").append(CommonComponents.SPACE).withStyle(EnumChatFormat.GRAY), this.fadeColors));
        }

        if (this.hasTrail) {
            consumer.accept(IChatBaseComponent.translatable("item.minecraft.firework_star.trail").withStyle(EnumChatFormat.GRAY));
        }

        if (this.hasTwinkle) {
            consumer.accept(IChatBaseComponent.translatable("item.minecraft.firework_star.flicker").withStyle(EnumChatFormat.GRAY));
        }

    }

    private static IChatBaseComponent appendColors(IChatMutableComponent ichatmutablecomponent, IntList intlist) {
        for (int i = 0; i < intlist.size(); ++i) {
            if (i > 0) {
                ichatmutablecomponent.append(", ");
            }

            ichatmutablecomponent.append(getColorName(intlist.getInt(i)));
        }

        return ichatmutablecomponent;
    }

    private static IChatBaseComponent getColorName(int i) {
        EnumColor enumcolor = EnumColor.byFireworkColor(i);

        return (IChatBaseComponent) (enumcolor == null ? FireworkExplosion.CUSTOM_COLOR_NAME : IChatBaseComponent.translatable("item.minecraft.firework_star." + enumcolor.getName()));
    }

    public FireworkExplosion withFadeColors(IntList intlist) {
        return new FireworkExplosion(this.shape, this.colors, new IntArrayList(intlist), this.hasTrail, this.hasTwinkle);
    }

    public static enum a implements INamable {

        SMALL_BALL(0, "small_ball"), LARGE_BALL(1, "large_ball"), STAR(2, "star"), CREEPER(3, "creeper"), BURST(4, "burst");

        private static final IntFunction<FireworkExplosion.a> BY_ID = ByIdMap.continuous(FireworkExplosion.a::getId, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, FireworkExplosion.a> STREAM_CODEC = ByteBufCodecs.idMapper(FireworkExplosion.a.BY_ID, FireworkExplosion.a::getId);
        public static final Codec<FireworkExplosion.a> CODEC = INamable.fromValues(FireworkExplosion.a::values);
        private final int id;
        private final String name;

        private a(final int i, final String s) {
            this.id = i;
            this.name = s;
        }

        public IChatMutableComponent getName() {
            return IChatBaseComponent.translatable("item.minecraft.firework_star.shape." + this.name);
        }

        public int getId() {
            return this.id;
        }

        public static FireworkExplosion.a byId(int i) {
            return (FireworkExplosion.a) FireworkExplosion.a.BY_ID.apply(i);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
