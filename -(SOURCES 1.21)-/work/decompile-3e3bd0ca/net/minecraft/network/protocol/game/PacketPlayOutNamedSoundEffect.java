package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;

public class PacketPlayOutNamedSoundEffect implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutNamedSoundEffect> STREAM_CODEC = Packet.codec(PacketPlayOutNamedSoundEffect::write, PacketPlayOutNamedSoundEffect::new);
    public static final float LOCATION_ACCURACY = 8.0F;
    private final Holder<SoundEffect> sound;
    private final SoundCategory source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;
    private final long seed;

    public PacketPlayOutNamedSoundEffect(Holder<SoundEffect> holder, SoundCategory soundcategory, double d0, double d1, double d2, float f, float f1, long i) {
        this.sound = holder;
        this.source = soundcategory;
        this.x = (int) (d0 * 8.0D);
        this.y = (int) (d1 * 8.0D);
        this.z = (int) (d2 * 8.0D);
        this.volume = f;
        this.pitch = f1;
        this.seed = i;
    }

    private PacketPlayOutNamedSoundEffect(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.sound = (Holder) SoundEffect.STREAM_CODEC.decode(registryfriendlybytebuf);
        this.source = (SoundCategory) registryfriendlybytebuf.readEnum(SoundCategory.class);
        this.x = registryfriendlybytebuf.readInt();
        this.y = registryfriendlybytebuf.readInt();
        this.z = registryfriendlybytebuf.readInt();
        this.volume = registryfriendlybytebuf.readFloat();
        this.pitch = registryfriendlybytebuf.readFloat();
        this.seed = registryfriendlybytebuf.readLong();
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        SoundEffect.STREAM_CODEC.encode(registryfriendlybytebuf, this.sound);
        registryfriendlybytebuf.writeEnum(this.source);
        registryfriendlybytebuf.writeInt(this.x);
        registryfriendlybytebuf.writeInt(this.y);
        registryfriendlybytebuf.writeInt(this.z);
        registryfriendlybytebuf.writeFloat(this.volume);
        registryfriendlybytebuf.writeFloat(this.pitch);
        registryfriendlybytebuf.writeLong(this.seed);
    }

    @Override
    public PacketType<PacketPlayOutNamedSoundEffect> type() {
        return GamePacketTypes.CLIENTBOUND_SOUND;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSoundEvent(this);
    }

    public Holder<SoundEffect> getSound() {
        return this.sound;
    }

    public SoundCategory getSource() {
        return this.source;
    }

    public double getX() {
        return (double) ((float) this.x / 8.0F);
    }

    public double getY() {
        return (double) ((float) this.y / 8.0F);
    }

    public double getZ() {
        return (double) ((float) this.z / 8.0F);
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public long getSeed() {
        return this.seed;
    }
}
