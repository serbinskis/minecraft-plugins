package me.serbinskis.smptweaks.custom.globaltrading;

import me.serbinskis.smptweaks.utils.PaperUtils;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!GlobalTrading.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (!(event.getRightClicked() instanceof Villager villager)) { return; }
		if (villager.getProfession() == Profession.NONE) { return; }
		PaperUtils.copyPositiveReputation(event.getPlayer(), villager);
	}
}
