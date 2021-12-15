package me.wobbychip.discordwhitelist.events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import me.wobbychip.discordwhitelist.Main;
import me.wobbychip.discordwhitelist.Utils;

public class LoginEvent implements Listener {
	public static List<UUID> allowed = new ArrayList<UUID>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		if (!Main.plugin.getConfig().getBoolean("Enabled")) { return; }

    	if (Utils.isPlayerWhitelisted(event.getName())) {
    		//Remove from list in case if player is already added
    	    while (allowed.contains(event.getUniqueId())) {
    	    	allowed.remove(event.getUniqueId());
    	    }

    	    //Add player again to allowed list
    		allowed.add(event.getUniqueId());
        }
    }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent event) {
		if (!Main.plugin.getConfig().getBoolean("Enabled")) { return; }
		Player player = event.getPlayer(); //Get player
		Boolean allow = allowed.remove(player.getUniqueId()); //Remove and check if player was in list
		if (Utils.checkPermissions(player, "dwl.bypass")) { return; } //Return if player has bypass permissions

		//Kick if player was not in list
    	if (!allow) {
    		String message = (Utils.getString("kickMessage") + Utils.getString("secondReason")).replaceAll("%n", "\n");
    		event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', message));
    	}
    }
}
