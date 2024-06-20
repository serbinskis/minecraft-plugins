package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketLoginOutSuccess(GameProfile gameProfile, boolean strictErrorHandling) implements Packet<PacketLoginOutListener> {

    public static final StreamCodec<ByteBuf, PacketLoginOutSuccess> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.GAME_PROFILE, PacketLoginOutSuccess::gameProfile, ByteBufCodecs.BOOL, PacketLoginOutSuccess::strictErrorHandling, PacketLoginOutSuccess::new);

    @Override
    public PacketType<PacketLoginOutSuccess> type() {
        return LoginPacketTypes.CLIENTBOUND_GAME_PROFILE;
    }

    public void handle(PacketLoginOutListener packetloginoutlistener) {
        packetloginoutlistener.handleGameProfile(this);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
