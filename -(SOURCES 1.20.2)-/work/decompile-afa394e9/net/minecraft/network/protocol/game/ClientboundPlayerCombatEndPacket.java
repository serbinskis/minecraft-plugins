package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<PacketListenerPlayOut> {

    private final int duration;

    public ClientboundPlayerCombatEndPacket(CombatTracker combattracker) {
        this(combattracker.getCombatDuration());
    }

    public ClientboundPlayerCombatEndPacket(int i) {
        this.duration = i;
    }

    public ClientboundPlayerCombatEndPacket(PacketDataSerializer packetdataserializer) {
        this.duration = packetdataserializer.readVarInt();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.duration);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handlePlayerCombatEnd(this);
    }
}
