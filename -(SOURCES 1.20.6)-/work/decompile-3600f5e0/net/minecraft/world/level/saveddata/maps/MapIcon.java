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

    public MapIcon(Holder<MapDecorationType> type, byte x, byte y, byte rot, Optional<IChatBaseComponent> name) {
        rot = (byte) (rot & 15);
        this.type = type;
        this.x = x;
        this.y = y;
        this.rot = rot;
        this.name = name;
    }

    public MinecraftKey getSpriteLocation() {
        return ((MapDecorationType) this.type.value()).assetId();
    }

    public boolean renderOnFrame() {
        return ((MapDecorationType) this.type.value()).showOnItemFrame();
    }
}
