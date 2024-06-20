package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.stats.Statistic;

public record PacketPlayOutStatistic(Object2IntMap<Statistic<?>> stats) implements Packet<PacketListenerPlayOut> {

    private static final StreamCodec<RegistryFriendlyByteBuf, Object2IntMap<Statistic<?>>> STAT_VALUES_STREAM_CODEC = ByteBufCodecs.map(Object2IntOpenHashMap::new, Statistic.STREAM_CODEC, ByteBufCodecs.VAR_INT);
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutStatistic> STREAM_CODEC = PacketPlayOutStatistic.STAT_VALUES_STREAM_CODEC.map(PacketPlayOutStatistic::new, PacketPlayOutStatistic::stats);

    @Override
    public PacketType<PacketPlayOutStatistic> type() {
        return GamePacketTypes.CLIENTBOUND_AWARD_STATS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleAwardStats(this);
    }
}
