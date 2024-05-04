package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class BlankFormat implements NumberFormat {

    public static final BlankFormat INSTANCE = new BlankFormat();
    public static final NumberFormatType<BlankFormat> TYPE = new NumberFormatType<BlankFormat>() {
        private static final MapCodec<BlankFormat> CODEC = MapCodec.unit(BlankFormat.INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, BlankFormat> STREAM_CODEC = StreamCodec.unit(BlankFormat.INSTANCE);

        @Override
        public MapCodec<BlankFormat> mapCodec() {
            return null.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlankFormat> streamCodec() {
            return null.STREAM_CODEC;
        }
    };

    public BlankFormat() {}

    @Override
    public IChatMutableComponent format(int i) {
        return IChatBaseComponent.empty();
    }

    @Override
    public NumberFormatType<BlankFormat> type() {
        return BlankFormat.TYPE;
    }
}
