package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public enum EnumItemRarity implements INamable {

    COMMON(0, "common", EnumChatFormat.WHITE), UNCOMMON(1, "uncommon", EnumChatFormat.YELLOW), RARE(2, "rare", EnumChatFormat.AQUA), EPIC(3, "epic", EnumChatFormat.LIGHT_PURPLE);

    public static final Codec<EnumItemRarity> CODEC = INamable.fromValues(EnumItemRarity::values);
    public static final IntFunction<EnumItemRarity> BY_ID = ByIdMap.continuous((enumitemrarity) -> {
        return enumitemrarity.id;
    }, values(), ByIdMap.a.ZERO);
    public static final StreamCodec<ByteBuf, EnumItemRarity> STREAM_CODEC = ByteBufCodecs.idMapper(EnumItemRarity.BY_ID, (enumitemrarity) -> {
        return enumitemrarity.id;
    });
    private final int id;
    private final String name;
    private final EnumChatFormat color;

    private EnumItemRarity(final int i, final String s, final EnumChatFormat enumchatformat) {
        this.id = i;
        this.name = s;
        this.color = enumchatformat;
    }

    public EnumChatFormat color() {
        return this.color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
