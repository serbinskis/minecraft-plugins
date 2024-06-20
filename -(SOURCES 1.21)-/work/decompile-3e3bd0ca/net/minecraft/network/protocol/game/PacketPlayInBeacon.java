package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.effect.MobEffectList;

public record PacketPlayInBeacon(Optional<Holder<MobEffectList>> primary, Optional<Holder<MobEffectList>> secondary) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayInBeacon> STREAM_CODEC = StreamCodec.composite(MobEffectList.STREAM_CODEC.apply(ByteBufCodecs::optional), PacketPlayInBeacon::primary, MobEffectList.STREAM_CODEC.apply(ByteBufCodecs::optional), PacketPlayInBeacon::secondary, PacketPlayInBeacon::new);

    @Override
    public PacketType<PacketPlayInBeacon> type() {
        return GamePacketTypes.SERVERBOUND_SET_BEACON;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetBeaconPacket(this);
    }
}
