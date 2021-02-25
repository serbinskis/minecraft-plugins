package me.wobbychip.ipbind;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class LoginEvent implements Listener {
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		String IPAddress = Main.PlayersConfig.getConfig().getString("players." + event.getUniqueId().toString());

		if ((IPAddress != null) && (!IPAddress.equals(event.getAddress().getHostName()))) {
			String replacedMessage = Utilities.getString("kickMessage").replaceAll("%n", "\n");
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', replacedMessage));
		}
    }
}
