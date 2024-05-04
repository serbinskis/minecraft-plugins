package net.minecraft.world.level.gameevent;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface PositionSourceType<T extends PositionSource> {

    PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.a());
    PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.a());

    MapCodec<T> codec();

    StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

    static <S extends PositionSourceType<T>, T extends PositionSource> S register(String s, S s0) {
        return (PositionSourceType) IRegistry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, s, s0);
    }
}
