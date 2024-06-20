package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundCategory;

public class PacketPlayOutStopSound implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutStopSound> STREAM_CODEC = Packet.codec(PacketPlayOutStopSound::write, PacketPlayOutStopSound::new);
    private static final int HAS_SOURCE = 1;
    private static final int HAS_SOUND = 2;
    @Nullable
    private final MinecraftKey name;
    @Nullable
    private final SoundCategory source;

    public PacketPlayOutStopSound(@Nullable MinecraftKey minecraftkey, @Nullable SoundCategory soundcategory) {
        this.name = minecraftkey;
        this.source = soundcategory;
    }

    private PacketPlayOutStopSound(PacketDataSerializer packetdataserializer) {
        byte b0 = packetdataserializer.readByte();

        if ((b0 & 1) > 0) {
            this.source = (SoundCategory) packetdataserializer.readEnum(SoundCategory.class);
        } else {
            this.source = null;
        }

        if ((b0 & 2) > 0) {
            this.name = packetdataserializer.readResourceLocation();
        } else {
            this.name = null;
        }

    }

    private void write(PacketDataSerializer packetdataserializer) {
        if (this.source != null) {
            if (this.name != null) {
                packetdataserializer.writeByte(3);
                packetdataserializer.writeEnum(this.source);
                packetdataserializer.writeResourceLocation(this.name);
            } else {
                packetdataserializer.writeByte(1);
                packetdataserializer.writeEnum(this.source);
            }
        } else if (this.name != null) {
            packetdataserializer.writeByte(2);
            packetdataserializer.writeResourceLocation(this.name);
        } else {
            packetdataserializer.writeByte(0);
        }

    }

    @Override
    public PacketType<PacketPlayOutStopSound> type() {
        return GamePacketTypes.CLIENTBOUND_STOP_SOUND;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleStopSoundEvent(this);
    }

    @Nullable
    public MinecraftKey getName() {
        return this.name;
    }

    @Nullable
    public SoundCategory getSource() {
        return this.source;
    }
}
