package net.minecraft.world.level.saveddata.maps;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;

public record MapIcon(Holder<MapDecorationType> type, byte x, byte y, byte rot, Optional<IChatBaseComponent> name) {

    public static final StreamCodec<RegistryFriendlyByteBuf, MapIcon> STREAM_CODEC = StreamCodec.composite(MapDecorationType.STREAM_CODEC, MapIcon::type, ByteBufCodecs.BYTE, MapIcon::x, ByteBufCodecs.BYTE, MapIcon::y, ByteBufCodecs.BYTE, MapIcon::rot, ComponentSerialization.OPTIONAL_STREAM_CODEC, MapIcon::name, MapIcon::new);

    public MapIcon(Holder<MapDecorationType> holder, byte b0, byte b1, byte b2, Optional<IChatBaseComponent> optional) {
        b2 = (byte) (b2 & 15);
        this.type = holder;
        this.x = b0;
        this.y = b1;
        this.rot = b2;
        this.name = optional;
    }

    public MinecraftKey getSpriteLocation() {
        return ((MapDecorationType) this.type.value()).assetId();
    }

    public boolean renderOnFrame() {
        return ((MapDecorationType) this.type.value()).showOnItemFrame();
    }
}
