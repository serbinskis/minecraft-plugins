package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MinecraftEncryption;

public class PacketLoginInEncryptionBegin implements Packet<PacketLoginInListener> {

    public static final StreamCodec<PacketDataSerializer, PacketLoginInEncryptionBegin> STREAM_CODEC = Packet.codec(PacketLoginInEncryptionBegin::write, PacketLoginInEncryptionBegin::new);
    private final byte[] keybytes;
    private final byte[] encryptedChallenge;

    public PacketLoginInEncryptionBegin(SecretKey secretkey, PublicKey publickey, byte[] abyte) throws CryptographyException {
        this.keybytes = MinecraftEncryption.encryptUsingKey(publickey, secretkey.getEncoded());
        this.encryptedChallenge = MinecraftEncryption.encryptUsingKey(publickey, abyte);
    }

    private PacketLoginInEncryptionBegin(PacketDataSerializer packetdataserializer) {
        this.keybytes = packetdataserializer.readByteArray();
        this.encryptedChallenge = packetdataserializer.readByteArray();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByteArray(this.keybytes);
        packetdataserializer.writeByteArray(this.encryptedChallenge);
    }

    @Override
    public PacketType<PacketLoginInEncryptionBegin> type() {
        return LoginPacketTypes.SERVERBOUND_KEY;
    }

    public void handle(PacketLoginInListener packetlogininlistener) {
        packetlogininlistener.handleKey(this);
    }

    public SecretKey getSecretKey(PrivateKey privatekey) throws CryptographyException {
        return MinecraftEncryption.decryptByteToSecretKey(privatekey, this.keybytes);
    }

    public boolean isChallengeValid(byte[] abyte, PrivateKey privatekey) {
        try {
            return Arrays.equals(abyte, MinecraftEncryption.decryptUsingKey(privatekey, this.encryptedChallenge));
        } catch (CryptographyException cryptographyexception) {
            return false;
        }
    }
}
