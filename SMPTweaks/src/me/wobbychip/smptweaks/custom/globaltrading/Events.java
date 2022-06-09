package me.wobbychip.smptweaks.custom.globaltrading;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	private void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Villager)) { return; }
		Villager villager = (Villager) event.getRightClicked();
		if (villager.getProfession() == Profession.NONE) { return; }

		copyReputation(event.getPlayer(), villager, ReputationType.MAJOR_POSITIVE);
		copyReputation(event.getPlayer(), villager, ReputationType.MINOR_POSITIVE);
	}

	private void copyReputation(Player player, Villager villager, ReputationType type) {
		if (hasReputation(player, villager, type)) { return; }
		int amount = getReputation(villager, type);
		if (amount <= 0) { return; }
		Reputation reputaion = new Reputation();
		reputaion.setReputation(type, amount);
		villager.setReputation(player.getUniqueId(), reputaion);
	}

	private boolean hasReputation(Player player, Villager villager, ReputationType type) {
		Reputation reputaion = villager.getReputation(player.getUniqueId());
		if (reputaion == null) { return false; }
		return reputaion.getReputation(type) > 0;
	}

	private int getReputation(Villager villager, ReputationType type) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Reputation reputaion = villager.getReputation(player.getUniqueId());
			if ((reputaion == null) || (reputaion.getReputation(type) <= 0)) { continue; }
			return reputaion.getReputation(type);
		}

		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			Reputation reputaion = villager.getReputation(player.getUniqueId());
			if ((reputaion == null) || (reputaion.getReputation(type) <= 0)) { continue; }
			return reputaion.getReputation(type);
		}

		return -1;
	}
}
