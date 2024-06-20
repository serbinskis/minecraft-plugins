package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.entity.Entity;

public class PacketPlayOutEntitySound implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutEntitySound> STREAM_CODEC = Packet.codec(PacketPlayOutEntitySound::write, PacketPlayOutEntitySound::new);
    private final Holder<SoundEffect> sound;
    private final SoundCategory source;
    private final int id;
    private final float volume;
    private final float pitch;
    private final long seed;

    public PacketPlayOutEntitySound(Holder<SoundEffect> holder, SoundCategory soundcategory, Entity entity, float f, float f1, long i) {
        this.sound = holder;
        this.source = soundcategory;
        this.id = entity.getId();
        this.volume = f;
        this.pitch = f1;
        this.seed = i;
    }

    private PacketPlayOutEntitySound(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.sound = (Holder) SoundEffect.STREAM_CODEC.decode(registryfriendlybytebuf);
        this.source = (SoundCategory) registryfriendlybytebuf.readEnum(SoundCategory.class);
        this.id = registryfriendlybytebuf.readVarInt();
        this.volume = registryfriendlybytebuf.readFloat();
        this.pitch = registryfriendlybytebuf.readFloat();
        this.seed = registryfriendlybytebuf.readLong();
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        SoundEffect.STREAM_CODEC.encode(registryfriendlybytebuf, this.sound);
        registryfriendlybytebuf.writeEnum(this.source);
        registryfriendlybytebuf.writeVarInt(this.id);
        registryfriendlybytebuf.writeFloat(this.volume);
        registryfriendlybytebuf.writeFloat(this.pitch);
        registryfriendlybytebuf.writeLong(this.seed);
    }

    @Override
    public PacketType<PacketPlayOutEntitySound> type() {
        return GamePacketTypes.CLIENTBOUND_SOUND_ENTITY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSoundEntityEvent(this);
    }

    public Holder<SoundEffect> getSound() {
        return this.sound;
    }

    public SoundCategory getSource() {
        return this.source;
    }

    public int getId() {
        return this.id;
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
