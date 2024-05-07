package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutExperience implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutExperience> STREAM_CODEC = Packet.codec(PacketPlayOutExperience::write, PacketPlayOutExperience::new);
    private final float experienceProgress;
    private final int totalExperience;
    private final int experienceLevel;

    public PacketPlayOutExperience(float f, int i, int j) {
        this.experienceProgress = f;
        this.totalExperience = i;
        this.experienceLevel = j;
    }

    private PacketPlayOutExperience(PacketDataSerializer packetdataserializer) {
        this.experienceProgress = packetdataserializer.readFloat();
        this.experienceLevel = packetdataserializer.readVarInt();
        this.totalExperience = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeFloat(this.experienceProgress);
        packetdataserializer.writeVarInt(this.experienceLevel);
        packetdataserializer.writeVarInt(this.totalExperience);
    }

    @Override
    public PacketType<PacketPlayOutExperience> type() {
        return GamePacketTypes.CLIENTBOUND_SET_EXPERIENCE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetExperience(this);
    }

    public float getExperienceProgress() {
        return this.experienceProgress;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public int getExperienceLevel() {
        return this.experienceLevel;
    }
}
