package me.wobbychip.smptweaks.library.tinyprotocol;

import io.netty.channel.Channel;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

public class TinyProtocol implements Listener {
    public static boolean enabled;

    public static void start() {
        Bukkit.getPluginManager().registerEvents(new TinyProtocol(), Main.plugin);
        ReflectionUtils.createServerChannelHandler(channel -> addConnection(channel, null));
        enabled = true;
    }

    public static void stop() {
        enabled = false;
    }

    private static void addPlayer(Player player) {
        Channel channel = PacketHandler.channel_cache.remove(player.getName());
        if (channel == null) { channel = ReflectionUtils.getChannel(player); }
        addConnection(channel, player);
    }

    private static void removePlayer(Player player) {
        PacketHandler.player_cache.remove(player.getUniqueId());
        Channel channel = PacketHandler.channel_cache.remove(player.getName());
        if (channel == null) { channel = ReflectionUtils.getChannel(player); }
        if ((channel == null) || (channel.pipeline().get(Main.PREFIX) == null)) { return; }
        channel.pipeline().remove(Main.PREFIX);
    }

    private static void addConnection(Channel channel, @Nullable Player player) {
        if (channel.pipeline().get(Main.PREFIX) != null) {  channel.pipeline().remove(Main.PREFIX); }
        if (player != null) { PacketHandler.player_cache.put(player.getUniqueId(), player); }
        PacketHandler packetHandler = new PacketHandler((player != null) ? player.getUniqueId() : null);
        channel.pipeline().addBefore("packet_handler", Main.PREFIX, packetHandler);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerLoginEvent(PlayerLoginEvent event) {
        TinyProtocol.addPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerQuit(PlayerQuitEvent event) {
        TinyProtocol.removePlayer(event.getPlayer());
    }
}
