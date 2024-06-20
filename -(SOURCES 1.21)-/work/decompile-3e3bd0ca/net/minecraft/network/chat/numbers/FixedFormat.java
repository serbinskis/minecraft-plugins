package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class FixedFormat implements NumberFormat {

    public static final NumberFormatType<FixedFormat> TYPE = new NumberFormatType<FixedFormat>() {
        private static final MapCodec<FixedFormat> CODEC = ComponentSerialization.CODEC.fieldOf("value").xmap(FixedFormat::new, (fixedformat) -> {
            return fixedformat.value;
        });
        private static final StreamCodec<RegistryFriendlyByteBuf, FixedFormat> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_STREAM_CODEC, (fixedformat) -> {
            return fixedformat.value;
        }, FixedFormat::new);

        @Override
        public MapCodec<FixedFormat> mapCodec() {
            return null.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FixedFormat> streamCodec() {
            return null.STREAM_CODEC;
        }
    };
    final IChatBaseComponent value;

    public FixedFormat(IChatBaseComponent ichatbasecomponent) {
        this.value = ichatbasecomponent;
    }

    @Override
    public IChatMutableComponent format(int i) {
        return this.value.copy();
    }

    @Override
    public NumberFormatType<FixedFormat> type() {
        return FixedFormat.TYPE;
    }
}
