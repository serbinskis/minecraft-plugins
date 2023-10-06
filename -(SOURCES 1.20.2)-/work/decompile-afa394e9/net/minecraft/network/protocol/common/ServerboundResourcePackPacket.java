package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerCommonPacketListener> {

    private final ServerboundResourcePackPacket.a action;

    public ServerboundResourcePackPacket(ServerboundResourcePackPacket.a serverboundresourcepackpacket_a) {
        this.action = serverboundresourcepackpacket_a;
    }

    public ServerboundResourcePackPacket(PacketDataSerializer packetdataserializer) {
        this.action = (ServerboundResourcePackPacket.a) packetdataserializer.readEnum(ServerboundResourcePackPacket.a.class);
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.action);
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleResourcePackResponse(this);
    }

    public ServerboundResourcePackPacket.a getAction() {
        return this.action;
    }

    public static enum a {

        SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED;

        private a() {}
    }
}
