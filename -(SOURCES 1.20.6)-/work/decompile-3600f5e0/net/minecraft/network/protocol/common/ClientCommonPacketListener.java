package net.minecraft.network.protocol.common;

import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.protocol.cookie.ClientCookiePacketListener;

public interface ClientCommonPacketListener extends ClientCookiePacketListener, ClientboundPacketListener {

    void handleKeepAlive(ClientboundKeepAlivePacket clientboundkeepalivepacket);

    void handlePing(ClientboundPingPacket clientboundpingpacket);

    void handleCustomPayload(ClientboundCustomPayloadPacket clientboundcustompayloadpacket);

    void handleDisconnect(ClientboundDisconnectPacket clientbounddisconnectpacket);

    void handleResourcePackPush(ClientboundResourcePackPushPacket clientboundresourcepackpushpacket);

    void handleResourcePackPop(ClientboundResourcePackPopPacket clientboundresourcepackpoppacket);

    void handleUpdateTags(ClientboundUpdateTagsPacket clientboundupdatetagspacket);

    void handleStoreCookie(ClientboundStoreCookiePacket clientboundstorecookiepacket);

    void handleTransfer(ClientboundTransferPacket clientboundtransferpacket);
}
