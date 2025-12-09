package me.serbinskis.smptweaks.custom.allcraftingrecipes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		if (!AllCraftingRecipes.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		event.getPlayer().discoverRecipes(AllCraftingRecipes.recipeKeys);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
		if (!AllCraftingRecipes.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		event.getPlayer().discoverRecipes(AllCraftingRecipes.recipeKeys);
	}
}
