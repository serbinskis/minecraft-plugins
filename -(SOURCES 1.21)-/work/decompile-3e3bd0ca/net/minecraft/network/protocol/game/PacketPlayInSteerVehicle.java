package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInSteerVehicle implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInSteerVehicle> STREAM_CODEC = Packet.codec(PacketPlayInSteerVehicle::write, PacketPlayInSteerVehicle::new);
    private static final int FLAG_JUMPING = 1;
    private static final int FLAG_SHIFT_KEY_DOWN = 2;
    private final float xxa;
    private final float zza;
    private final boolean isJumping;
    private final boolean isShiftKeyDown;

    public PacketPlayInSteerVehicle(float f, float f1, boolean flag, boolean flag1) {
        this.xxa = f;
        this.zza = f1;
        this.isJumping = flag;
        this.isShiftKeyDown = flag1;
    }

    private PacketPlayInSteerVehicle(PacketDataSerializer packetdataserializer) {
        this.xxa = packetdataserializer.readFloat();
        this.zza = packetdataserializer.readFloat();
        byte b0 = packetdataserializer.readByte();

        this.isJumping = (b0 & 1) > 0;
        this.isShiftKeyDown = (b0 & 2) > 0;
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeFloat(this.xxa);
        packetdataserializer.writeFloat(this.zza);
        byte b0 = 0;

        if (this.isJumping) {
            b0 = (byte) (b0 | 1);
        }

        if (this.isShiftKeyDown) {
            b0 = (byte) (b0 | 2);
        }

        packetdataserializer.writeByte(b0);
    }

    @Override
    public PacketType<PacketPlayInSteerVehicle> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_INPUT;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePlayerInput(this);
    }

    public float getXxa() {
        return this.xxa;
    }

    public float getZza() {
        return this.zza;
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    public boolean isShiftKeyDown() {
        return this.isShiftKeyDown;
    }
}
