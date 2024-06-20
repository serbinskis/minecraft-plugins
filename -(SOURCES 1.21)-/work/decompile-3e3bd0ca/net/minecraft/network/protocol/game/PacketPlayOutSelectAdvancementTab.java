package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class PacketPlayOutSelectAdvancementTab implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutSelectAdvancementTab> STREAM_CODEC = Packet.codec(PacketPlayOutSelectAdvancementTab::write, PacketPlayOutSelectAdvancementTab::new);
    @Nullable
    private final MinecraftKey tab;

    public PacketPlayOutSelectAdvancementTab(@Nullable MinecraftKey minecraftkey) {
        this.tab = minecraftkey;
    }

    private PacketPlayOutSelectAdvancementTab(PacketDataSerializer packetdataserializer) {
        this.tab = (MinecraftKey) packetdataserializer.readNullable(PacketDataSerializer::readResourceLocation);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeNullable(this.tab, PacketDataSerializer::writeResourceLocation);
    }

    @Override
    public PacketType<PacketPlayOutSelectAdvancementTab> type() {
        return GamePacketTypes.CLIENTBOUND_SELECT_ADVANCEMENTS_TAB;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSelectAdvancementsTab(this);
    }

    @Nullable
    public MinecraftKey getTab() {
        return this.tab;
    }
}
