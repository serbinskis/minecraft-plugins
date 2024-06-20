package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.core.IRegistryCustom;

public class RegistryFriendlyByteBuf extends PacketDataSerializer {

    private final IRegistryCustom registryAccess;

    public RegistryFriendlyByteBuf(ByteBuf bytebuf, IRegistryCustom iregistrycustom) {
        super(bytebuf);
        this.registryAccess = iregistrycustom;
    }

    public IRegistryCustom registryAccess() {
        return this.registryAccess;
    }

    public static Function<ByteBuf, RegistryFriendlyByteBuf> decorator(IRegistryCustom iregistrycustom) {
        return (bytebuf) -> {
            return new RegistryFriendlyByteBuf(bytebuf, iregistrycustom);
        };
    }
}
