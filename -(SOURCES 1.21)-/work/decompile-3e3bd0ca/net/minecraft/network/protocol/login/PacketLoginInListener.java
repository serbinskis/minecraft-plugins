package net.minecraft.network.protocol.login;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;
import net.minecraft.network.protocol.game.ServerPacketListener;

public interface PacketLoginInListener extends ServerCookiePacketListener, ServerPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.LOGIN;
    }

    void handleHello(PacketLoginInStart packetlogininstart);

    void handleKey(PacketLoginInEncryptionBegin packetlogininencryptionbegin);

    void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket serverboundcustomqueryanswerpacket);

    void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket serverboundloginacknowledgedpacket);
}
