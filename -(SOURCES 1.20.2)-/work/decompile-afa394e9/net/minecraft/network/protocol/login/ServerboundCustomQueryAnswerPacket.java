package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;

public record ServerboundCustomQueryAnswerPacket(int transactionId, @Nullable CustomQueryAnswerPayload payload) implements Packet<PacketLoginInListener> {

    private static final int MAX_PAYLOAD_SIZE = 1048576;

    public static ServerboundCustomQueryAnswerPacket read(PacketDataSerializer packetdataserializer) {
        int i = packetdataserializer.readVarInt();

        return new ServerboundCustomQueryAnswerPacket(i, readPayload(i, packetdataserializer));
    }

    private static CustomQueryAnswerPayload readPayload(int i, PacketDataSerializer packetdataserializer) {
        return readUnknownPayload(packetdataserializer);
    }

    private static CustomQueryAnswerPayload readUnknownPayload(PacketDataSerializer packetdataserializer) {
        int i = packetdataserializer.readableBytes();

        if (i >= 0 && i <= 1048576) {
            packetdataserializer.skipBytes(i);
            return DiscardedQueryAnswerPayload.INSTANCE;
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.transactionId);
        packetdataserializer.writeNullable(this.payload, (packetdataserializer1, customqueryanswerpayload) -> {
            customqueryanswerpayload.write(packetdataserializer1);
        });
    }

    public void handle(PacketLoginInListener packetlogininlistener) {
        packetlogininlistener.handleCustomQueryPacket(this);
    }
}
