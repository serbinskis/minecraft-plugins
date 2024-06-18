package me.wobbychip.smptweaks.library.tinyprotocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class PacketHandler extends ChannelDuplexHandler {
    public static HashMap<String, Channel> channel_cache = new HashMap<>();
    public static HashMap<UUID, Player> player_cache = new HashMap<>();
    private final UUID playerId;

    public PacketHandler(final UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
        String packetType = ReflectionUtils.getPacketType(packet);
        if (packetType.isEmpty() || !TinyProtocol.enabled) { super.write(ctx, packet, promise); return; }

        PacketEvent packetEvent = new PacketEvent(playerId, PacketType.Flow.CLIENTBOUND, PacketType.getType(packetType), packet);
        Bukkit.getPluginManager().callEvent(packetEvent);
        if (!packetEvent.isCancelled()) { super.write(ctx, packetEvent.getPacket(), promise); }

        //String type = ReflectionUtils.getPacketType(packet).toUpperCase();
        //if (PacketType.getType(type) == PacketType.UNKNOWN) { Utils.sendMessage(type); }
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object packet) throws Exception {
        if (packet instanceof ServerboundHelloPacket helloPacket) {
            channel_cache.put(helloPacket.name(), ctx.channel());
        }

        String packetType = ReflectionUtils.getPacketType(packet);
        if (packetType.isEmpty() || !TinyProtocol.enabled) { super.channelRead(ctx, packet); return; }

        PacketEvent packetEvent = new PacketEvent(playerId, PacketType.Flow.SERVERBOUND, PacketType.getType(packetType), packet);
        Bukkit.getPluginManager().callEvent(packetEvent);
        if (!packetEvent.isCancelled()) { super.channelRead(ctx, packetEvent.getPacket()); }

        //String type = ReflectionUtils.getPacketType(packet).toUpperCase();
        //if (PacketType.getType(type) == PacketType.UNKNOWN) { Utils.sendMessage(type); }
    }
}
