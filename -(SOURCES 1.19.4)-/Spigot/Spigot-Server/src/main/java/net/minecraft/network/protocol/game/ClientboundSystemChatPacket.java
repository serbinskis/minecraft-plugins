// mc-dev import
package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;

// Spigot start
public record ClientboundSystemChatPacket(String content, boolean overlay) implements Packet<PacketListenerPlayOut> {

    public ClientboundSystemChatPacket(IChatBaseComponent content, boolean overlay) {
        this(IChatBaseComponent.ChatSerializer.toJson(content), overlay);
    }

    public ClientboundSystemChatPacket(net.md_5.bungee.api.chat.BaseComponent[] content, boolean overlay) {
        this(net.md_5.bungee.chat.ComponentSerializer.toString(content), overlay);
    }
    // Spigot end

    public ClientboundSystemChatPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readComponent(), packetdataserializer.readBoolean());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.content, 262144); // Spigot
        packetdataserializer.writeBoolean(this.overlay);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSystemChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}
