package me.wobbychip.whitelist;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class LoginEvent implements Listener {
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    	if (Main.plugin.getConfig().getBoolean("Enabled") && !Utilities.PlayerWhitelisted(event.getName())) {
    		String replacedMessage = Utilities.getString("kickMessage").replaceAll("%n", "\n");
    		event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', replacedMessage));
        }
    }
}
