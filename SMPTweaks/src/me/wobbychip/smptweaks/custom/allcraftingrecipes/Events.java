package me.wobbychip.smptweaks.custom.allcraftingrecipes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().discoverRecipes(AllCraftingRecipes.recipeKeys);
	}
}
