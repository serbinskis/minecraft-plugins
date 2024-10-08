package me.serbinskis.smptweaks.custom.globaltrading;

import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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

		copyReputation(event.getPlayer(), villager, "MAJOR_POSITIVE");
		copyReputation(event.getPlayer(), villager, "MINOR_POSITIVE");
	}

	public void copyReputation(Player player, Villager villager, String type) {
		int amount = getReputation(villager, type);
		if (amount > 0) { ReflectionUtils.setPlayerReputation(villager, player.getUniqueId(), type, amount); }
	}

	public int getReputation(Villager villager, String type) {
		int result = 0;

		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			int amount = ReflectionUtils.getPlayerReputation(villager, player.getUniqueId(), type);
			if (amount > result) { result = amount; }
		}

		return result;
	}
}
