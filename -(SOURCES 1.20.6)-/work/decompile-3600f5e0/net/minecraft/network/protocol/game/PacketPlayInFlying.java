package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public abstract class PacketPlayInFlying implements Packet<PacketListenerPlayIn> {

    public final double x;
    public final double y;
    public final double z;
    public final float yRot;
    public final float xRot;
    protected final boolean onGround;
    public final boolean hasPos;
    public final boolean hasRot;

    protected PacketPlayInFlying(double d0, double d1, double d2, float f, float f1, boolean flag, boolean flag1, boolean flag2) {
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.yRot = f;
        this.xRot = f1;
        this.onGround = flag;
        this.hasPos = flag1;
        this.hasRot = flag2;
    }

    @Override
    public abstract PacketType<? extends PacketPlayInFlying> type();

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleMovePlayer(this);
    }

    public double getX(double d0) {
        return this.hasPos ? this.x : d0;
    }

    public double getY(double d0) {
        return this.hasPos ? this.y : d0;
    }

    public double getZ(double d0) {
        return this.hasPos ? this.z : d0;
    }

    public float getYRot(float f) {
        return this.hasRot ? this.yRot : f;
    }

    public float getXRot(float f) {
        return this.hasRot ? this.xRot : f;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean hasPosition() {
        return this.hasPos;
    }

    public boolean hasRotation() {
        return this.hasRot;
    }

    public static class d extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.d> STREAM_CODEC = Packet.codec(PacketPlayInFlying.d::write, PacketPlayInFlying.d::read);

        public d(boolean flag) {
            super(0.0D, 0.0D, 0.0D, 0.0F, 0.0F, flag, false, false);
        }

        private static PacketPlayInFlying.d read(PacketDataSerializer packetdataserializer) {
            boolean flag = packetdataserializer.readUnsignedByte() != 0;

            return new PacketPlayInFlying.d(flag);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<PacketPlayInFlying.d> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_STATUS_ONLY;
        }
    }

    public static class PacketPlayInLook extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.PacketPlayInLook> STREAM_CODEC = Packet.codec(PacketPlayInFlying.PacketPlayInLook::write, PacketPlayInFlying.PacketPlayInLook::read);

        public PacketPlayInLook(float f, float f1, boolean flag) {
            super(0.0D, 0.0D, 0.0D, f, f1, flag, false, true);
        }

        private static PacketPlayInFlying.PacketPlayInLook read(PacketDataSerializer packetdataserializer) {
            float f = packetdataserializer.readFloat();
            float f1 = packetdataserializer.readFloat();
            boolean flag = packetdataserializer.readUnsignedByte() != 0;

            return new PacketPlayInFlying.PacketPlayInLook(f, f1, flag);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeFloat(this.yRot);
            packetdataserializer.writeFloat(this.xRot);
            packetdataserializer.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<PacketPlayInFlying.PacketPlayInLook> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_ROT;
        }
    }

    public static class PacketPlayInPosition extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.PacketPlayInPosition> STREAM_CODEC = Packet.codec(PacketPlayInFlying.PacketPlayInPosition::write, PacketPlayInFlying.PacketPlayInPosition::read);

        public PacketPlayInPosition(double d0, double d1, double d2, boolean flag) {
            super(d0, d1, d2, 0.0F, 0.0F, flag, true, false);
        }

        private static PacketPlayInFlying.PacketPlayInPosition read(PacketDataSerializer packetdataserializer) {
            double d0 = packetdataserializer.readDouble();
            double d1 = packetdataserializer.readDouble();
            double d2 = packetdataserializer.readDouble();
            boolean flag = packetdataserializer.readUnsignedByte() != 0;

            return new PacketPlayInFlying.PacketPlayInPosition(d0, d1, d2, flag);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeDouble(this.x);
            packetdataserializer.writeDouble(this.y);
            packetdataserializer.writeDouble(this.z);
            packetdataserializer.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<PacketPlayInFlying.PacketPlayInPosition> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS;
        }
    }

    public static class PacketPlayInPositionLook extends PacketPlayInFlying {

        public static final StreamCodec<PacketDataSerializer, PacketPlayInFlying.PacketPlayInPositionLook> STREAM_CODEC = Packet.codec(PacketPlayInFlying.PacketPlayInPositionLook::write, PacketPlayInFlying.PacketPlayInPositionLook::read);

        public PacketPlayInPositionLook(double d0, double d1, double d2, float f, float f1, boolean flag) {
            super(d0, d1, d2, f, f1, flag, true, true);
        }

        private static PacketPlayInFlying.PacketPlayInPositionLook read(PacketDataSerializer packetdataserializer) {
            double d0 = packetdataserializer.readDouble();
            double d1 = packetdataserializer.readDouble();
            double d2 = packetdataserializer.readDouble();
            float f = packetdataserializer.readFloat();
            float f1 = packetdataserializer.readFloat();
            boolean flag = packetdataserializer.readUnsignedByte() != 0;

            return new PacketPlayInFlying.PacketPlayInPositionLook(d0, d1, d2, f, f1, flag);
        }

        private void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeDouble(this.x);
            packetdataserializer.writeDouble(this.y);
            packetdataserializer.writeDouble(this.z);
            packetdataserializer.writeFloat(this.yRot);
            packetdataserializer.writeFloat(this.xRot);
            packetdataserializer.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<PacketPlayInFlying.PacketPlayInPositionLook> type() {
            return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS_ROT;
        }
    }
}
