package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;

public class PacketPlayOutEntityEffect implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutEntityEffect> STREAM_CODEC = Packet.codec(PacketPlayOutEntityEffect::write, PacketPlayOutEntityEffect::new);
    private static final int FLAG_AMBIENT = 1;
    private static final int FLAG_VISIBLE = 2;
    private static final int FLAG_SHOW_ICON = 4;
    private static final int FLAG_BLEND = 8;
    private final int entityId;
    private final Holder<MobEffectList> effect;
    private final int effectAmplifier;
    private final int effectDurationTicks;
    private final byte flags;

    public PacketPlayOutEntityEffect(int i, MobEffect mobeffect, boolean flag) {
        this.entityId = i;
        this.effect = mobeffect.getEffect();
        this.effectAmplifier = mobeffect.getAmplifier();
        this.effectDurationTicks = mobeffect.getDuration();
        byte b0 = 0;

        if (mobeffect.isAmbient()) {
            b0 = (byte) (b0 | 1);
        }

        if (mobeffect.isVisible()) {
            b0 = (byte) (b0 | 2);
        }

        if (mobeffect.showIcon()) {
            b0 = (byte) (b0 | 4);
        }

        if (flag) {
            b0 = (byte) (b0 | 8);
        }

        this.flags = b0;
    }

    private PacketPlayOutEntityEffect(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.entityId = registryfriendlybytebuf.readVarInt();
        this.effect = (Holder) MobEffectList.STREAM_CODEC.decode(registryfriendlybytebuf);
        this.effectAmplifier = registryfriendlybytebuf.readVarInt();
        this.effectDurationTicks = registryfriendlybytebuf.readVarInt();
        this.flags = registryfriendlybytebuf.readByte();
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeVarInt(this.entityId);
        MobEffectList.STREAM_CODEC.encode(registryfriendlybytebuf, this.effect);
        registryfriendlybytebuf.writeVarInt(this.effectAmplifier);
        registryfriendlybytebuf.writeVarInt(this.effectDurationTicks);
        registryfriendlybytebuf.writeByte(this.flags);
    }

    @Override
    public PacketType<PacketPlayOutEntityEffect> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_MOB_EFFECT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleUpdateMobEffect(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Holder<MobEffectList> getEffect() {
        return this.effect;
    }

    public int getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public int getEffectDurationTicks() {
        return this.effectDurationTicks;
    }

    public boolean isEffectVisible() {
        return (this.flags & 2) != 0;
    }

    public boolean isEffectAmbient() {
        return (this.flags & 1) != 0;
    }

    public boolean effectShowsIcon() {
        return (this.flags & 4) != 0;
    }

    public boolean shouldBlend() {
        return (this.flags & 8) != 0;
    }
}
