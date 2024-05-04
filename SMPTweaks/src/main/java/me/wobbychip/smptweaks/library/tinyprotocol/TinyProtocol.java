package me.wobbychip.smptweaks.library.tinyprotocol;

import io.netty.channel.Channel;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TinyProtocol implements Listener {
    private static void addPlayer(Player player) {
        Channel channel = ReflectionUtils.getChannel(player);
        if (channel.pipeline().get(Main.PREFIX) != null) { return; }
        PacketHandler packetHandler = new PacketHandler(player);
        channel.pipeline().addBefore("packet_handler", Main.PREFIX, packetHandler);
    }

    private static void removePlayer(Player player) {
        Channel channel = ReflectionUtils.getChannel(player);
        if (channel.pipeline().get(Main.PREFIX) == null) { return; }
        channel.pipeline().remove(Main.PREFIX);
    }

    public static void start() {
        Bukkit.getPluginManager().registerEvents(new TinyProtocol(), Main.plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerLogin(PlayerJoinEvent event) {
        TinyProtocol.addPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerQuit(PlayerQuitEvent event) {
        TinyProtocol.removePlayer(event.getPlayer());
    }
}
