package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatKillPacket implements Packet<PacketListenerPlayOut> {

    private final int playerId;
    private final IChatBaseComponent message;

    public ClientboundPlayerCombatKillPacket(int i, IChatBaseComponent ichatbasecomponent) {
        this.playerId = i;
        this.message = ichatbasecomponent;
    }

    public ClientboundPlayerCombatKillPacket(PacketDataSerializer packetdataserializer) {
        this.playerId = packetdataserializer.readVarInt();
        this.message = packetdataserializer.readComponent();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.playerId);
        packetdataserializer.writeComponent(this.message);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handlePlayerCombatKill(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public IChatBaseComponent getMessage() {
        return this.message;
    }
}
