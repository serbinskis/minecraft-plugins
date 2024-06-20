package net.minecraft.network.protocol.common;

import java.util.UUID;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundResourcePackPacket(UUID id, ServerboundResourcePackPacket.a action) implements Packet<ServerCommonPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ServerboundResourcePackPacket> STREAM_CODEC = Packet.codec(ServerboundResourcePackPacket::write, ServerboundResourcePackPacket::new);

    private ServerboundResourcePackPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUUID(), (ServerboundResourcePackPacket.a) packetdataserializer.readEnum(ServerboundResourcePackPacket.a.class));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUUID(this.id);
        packetdataserializer.writeEnum(this.action);
    }

    @Override
    public PacketType<ServerboundResourcePackPacket> type() {
        return CommonPacketTypes.SERVERBOUND_RESOURCE_PACK;
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleResourcePackResponse(this);
    }

    public static enum a {

        SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED, DOWNLOADED, INVALID_URL, FAILED_RELOAD, DISCARDED;

        private a() {}

        public boolean isTerminal() {
            return this != ServerboundResourcePackPacket.a.ACCEPTED && this != ServerboundResourcePackPacket.a.DOWNLOADED;
        }
    }
}
