package me.wobbychip.discordwhitelist;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginEvent implements Listener {
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		if (!Main.plugin.getConfig().getBoolean("Enabled")) { return; }

    	if (Utilities.PlayerWhitelisted(event.getName())) {
    		//Remove from list in case if player is already added
    	    while (Main.allowed.contains(event.getUniqueId().toString())) {
    	    	Main.allowed.remove(event.getUniqueId().toString());
    	    }

    	    //Add player again to allowed list
    		Main.allowed.add(event.getUniqueId().toString());
        }
    }

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent event) {
		if (!Main.plugin.getConfig().getBoolean("Enabled")) { return; }
		Player player = event.getPlayer(); //Get player
		Boolean allow = Main.allowed.remove(player.getUniqueId().toString()); //Remove and check if player was in list
		if (Utilities.CheckPermissions(player, "dwl.bypass")) { return; } //Return if player has bypass permissions

		//Kick if player was not in list
    	if (!allow) {
    		String replacedMessage = Utilities.getString("kickMessage").replaceAll("%n", "\n");
    		event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', replacedMessage));
        }
    }
}
