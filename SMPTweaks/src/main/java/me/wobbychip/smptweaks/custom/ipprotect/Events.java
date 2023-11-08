package me.wobbychip.smptweaks.custom.ipprotect;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		if (!IpProtect.tweak.getConfig(0).getConfig().contains("players." + event.getName().toLowerCase())) { return; }
		if (event.getAddress().getHostAddress().equalsIgnoreCase("127.0.0.1")) { return; }
		String ipAddress = IpProtect.tweak.getConfig(0).getConfig().getString("players." + event.getName().toLowerCase());
		if (event.getAddress().getHostAddress().equalsIgnoreCase(ipAddress)) { return; }
		event.disallow(Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', "&cPasol nahuj kropli."));
	}
}
