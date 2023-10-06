package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;

public class ClientboundDisconnectPacket implements Packet<ClientCommonPacketListener> {

    private final IChatBaseComponent reason;

    public ClientboundDisconnectPacket(IChatBaseComponent ichatbasecomponent) {
        this.reason = ichatbasecomponent;
    }

    public ClientboundDisconnectPacket(PacketDataSerializer packetdataserializer) {
        this.reason = packetdataserializer.readComponent();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeComponent(this.reason);
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleDisconnect(this);
    }

    public IChatBaseComponent getReason() {
        return this.reason;
    }
}
