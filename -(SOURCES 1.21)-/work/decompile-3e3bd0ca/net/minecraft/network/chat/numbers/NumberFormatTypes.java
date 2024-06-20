package net.minecraft.network.chat.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class NumberFormatTypes {

    public static final MapCodec<NumberFormat> MAP_CODEC = BuiltInRegistries.NUMBER_FORMAT_TYPE.byNameCodec().dispatchMap(NumberFormat::type, NumberFormatType::mapCodec);
    public static final Codec<NumberFormat> CODEC = NumberFormatTypes.MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, NumberFormat> STREAM_CODEC = ByteBufCodecs.registry(Registries.NUMBER_FORMAT_TYPE).dispatch(NumberFormat::type, NumberFormatType::streamCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<NumberFormat>> OPTIONAL_STREAM_CODEC = NumberFormatTypes.STREAM_CODEC.apply(ByteBufCodecs::optional);

    public NumberFormatTypes() {}

    public static NumberFormatType<?> bootstrap(IRegistry<NumberFormatType<?>> iregistry) {
        IRegistry.register(iregistry, "blank", BlankFormat.TYPE);
        IRegistry.register(iregistry, "styled", StyledFormat.TYPE);
        return (NumberFormatType) IRegistry.register(iregistry, "fixed", FixedFormat.TYPE);
    }
}
