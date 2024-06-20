package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayOutScoreboardScore(String owner, String objectiveName, int score, Optional<IChatBaseComponent> display, Optional<NumberFormat> numberFormat) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutScoreboardScore> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PacketPlayOutScoreboardScore::owner, ByteBufCodecs.STRING_UTF8, PacketPlayOutScoreboardScore::objectiveName, ByteBufCodecs.VAR_INT, PacketPlayOutScoreboardScore::score, ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC, PacketPlayOutScoreboardScore::display, NumberFormatTypes.OPTIONAL_STREAM_CODEC, PacketPlayOutScoreboardScore::numberFormat, PacketPlayOutScoreboardScore::new);

    @Override
    public PacketType<PacketPlayOutScoreboardScore> type() {
        return GamePacketTypes.CLIENTBOUND_SET_SCORE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetScore(this);
    }
}
