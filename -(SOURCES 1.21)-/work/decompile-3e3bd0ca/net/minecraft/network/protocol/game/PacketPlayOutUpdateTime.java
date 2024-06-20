package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutUpdateTime implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutUpdateTime> STREAM_CODEC = Packet.codec(PacketPlayOutUpdateTime::write, PacketPlayOutUpdateTime::new);
    private final long gameTime;
    private final long dayTime;

    public PacketPlayOutUpdateTime(long i, long j, boolean flag) {
        this.gameTime = i;
        long k = j;

        if (!flag) {
            k = -j;
            if (k == 0L) {
                k = -1L;
            }
        }

        this.dayTime = k;
    }

    private PacketPlayOutUpdateTime(PacketDataSerializer packetdataserializer) {
        this.gameTime = packetdataserializer.readLong();
        this.dayTime = packetdataserializer.readLong();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.gameTime);
        packetdataserializer.writeLong(this.dayTime);
    }

    @Override
    public PacketType<PacketPlayOutUpdateTime> type() {
        return GamePacketTypes.CLIENTBOUND_SET_TIME;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetTime(this);
    }

    public long getGameTime() {
        return this.gameTime;
    }

    public long getDayTime() {
        return this.dayTime;
    }
}
