package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MinecraftEncryption;

public class PacketLoginOutEncryptionBegin implements Packet<PacketLoginOutListener> {

    public static final StreamCodec<PacketDataSerializer, PacketLoginOutEncryptionBegin> STREAM_CODEC = Packet.codec(PacketLoginOutEncryptionBegin::write, PacketLoginOutEncryptionBegin::new);
    private final String serverId;
    private final byte[] publicKey;
    private final byte[] challenge;
    private final boolean shouldAuthenticate;

    public PacketLoginOutEncryptionBegin(String s, byte[] abyte, byte[] abyte1, boolean flag) {
        this.serverId = s;
        this.publicKey = abyte;
        this.challenge = abyte1;
        this.shouldAuthenticate = flag;
    }

    private PacketLoginOutEncryptionBegin(PacketDataSerializer packetdataserializer) {
        this.serverId = packetdataserializer.readUtf(20);
        this.publicKey = packetdataserializer.readByteArray();
        this.challenge = packetdataserializer.readByteArray();
        this.shouldAuthenticate = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.serverId);
        packetdataserializer.writeByteArray(this.publicKey);
        packetdataserializer.writeByteArray(this.challenge);
        packetdataserializer.writeBoolean(this.shouldAuthenticate);
    }

    @Override
    public PacketType<PacketLoginOutEncryptionBegin> type() {
        return LoginPacketTypes.CLIENTBOUND_HELLO;
    }

    public void handle(PacketLoginOutListener packetloginoutlistener) {
        packetloginoutlistener.handleHello(this);
    }

    public String getServerId() {
        return this.serverId;
    }

    public PublicKey getPublicKey() throws CryptographyException {
        return MinecraftEncryption.byteToPublicKey(this.publicKey);
    }

    public byte[] getChallenge() {
        return this.challenge;
    }

    public boolean shouldAuthenticate() {
        return this.shouldAuthenticate;
    }
}
