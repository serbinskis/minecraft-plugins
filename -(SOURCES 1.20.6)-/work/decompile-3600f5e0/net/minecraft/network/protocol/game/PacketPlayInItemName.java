package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInItemName implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInItemName> STREAM_CODEC = Packet.codec(PacketPlayInItemName::write, PacketPlayInItemName::new);
    private final String name;

    public PacketPlayInItemName(String s) {
        this.name = s;
    }

    private PacketPlayInItemName(PacketDataSerializer packetdataserializer) {
        this.name = packetdataserializer.readUtf();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.name);
    }

    @Override
    public PacketType<PacketPlayInItemName> type() {
        return GamePacketTypes.SERVERBOUND_RENAME_ITEM;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleRenameItem(this);
    }

    public String getName() {
        return this.name;
    }
}
