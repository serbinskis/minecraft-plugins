package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map.Entry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TypedDataComponent<T>(DataComponentType<T> type, T value) {

    public static final StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>>() {
        public TypedDataComponent<?> decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            DataComponentType<?> datacomponenttype = (DataComponentType) DataComponentType.STREAM_CODEC.decode(registryfriendlybytebuf);

            return decodeTyped(registryfriendlybytebuf, datacomponenttype);
        }

        private static <T> TypedDataComponent<T> decodeTyped(RegistryFriendlyByteBuf registryfriendlybytebuf, DataComponentType<T> datacomponenttype) {
            return new TypedDataComponent<>(datacomponenttype, datacomponenttype.streamCodec().decode(registryfriendlybytebuf));
        }

        public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, TypedDataComponent<?> typeddatacomponent) {
            encodeCap(registryfriendlybytebuf, typeddatacomponent);
        }

        private static <T> void encodeCap(RegistryFriendlyByteBuf registryfriendlybytebuf, TypedDataComponent<T> typeddatacomponent) {
            DataComponentType.STREAM_CODEC.encode(registryfriendlybytebuf, typeddatacomponent.type());
            typeddatacomponent.type().streamCodec().encode(registryfriendlybytebuf, typeddatacomponent.value());
        }
    };

    static TypedDataComponent<?> fromEntryUnchecked(Entry<DataComponentType<?>, Object> entry) {
        return createUnchecked((DataComponentType) entry.getKey(), entry.getValue());
    }

    static <T> TypedDataComponent<T> createUnchecked(DataComponentType<T> datacomponenttype, Object object) {
        return new TypedDataComponent<>(datacomponenttype, object);
    }

    public void applyTo(PatchedDataComponentMap patcheddatacomponentmap) {
        patcheddatacomponentmap.set(this.type, this.value);
    }

    public <D> DataResult<D> encodeValue(DynamicOps<D> dynamicops) {
        Codec<T> codec = this.type.codec();

        return codec == null ? DataResult.error(() -> {
            return "Component of type " + String.valueOf(this.type) + " is not encodable";
        }) : codec.encodeStart(dynamicops, this.value);
    }

    public String toString() {
        String s = String.valueOf(this.type);

        return s + "=>" + String.valueOf(this.value);
    }
}
