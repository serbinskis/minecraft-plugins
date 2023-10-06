package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ClientInformation;

public record ServerboundClientInformationPacket(ClientInformation information) implements Packet<ServerCommonPacketListener> {

    public ServerboundClientInformationPacket(PacketDataSerializer packetdataserializer) {
        this(new ClientInformation(packetdataserializer));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        this.information.write(packetdataserializer);
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleClientInformation(this);
    }
}
