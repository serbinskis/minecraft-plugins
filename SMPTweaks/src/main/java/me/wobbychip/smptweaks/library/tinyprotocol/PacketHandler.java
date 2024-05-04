package me.wobbychip.smptweaks.library.tinyprotocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketHandler extends ChannelDuplexHandler {
    private final Player player;

    public PacketHandler(final Player player) {
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
        PacketEvent packetEvent = new PacketEvent(player, PacketType.Flow.CLIENTBOUND, PacketType.getType(ReflectionUtils.getPacketType(packet)), packet);
        Bukkit.getPluginManager().callEvent(packetEvent);
        if (!packetEvent.isCancelled()) { super.write(ctx, packetEvent.getPacket(), promise); }
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object packet) throws Exception {
        PacketEvent packetEvent = new PacketEvent(player, PacketType.Flow.SERVERBOUND, PacketType.getType(ReflectionUtils.getPacketType(packet)), packet);
        Bukkit.getPluginManager().callEvent(packetEvent);
        if (!packetEvent.isCancelled()) { super.channelRead(ctx, packetEvent.getPacket()); }
    }
}
