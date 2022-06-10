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

import me.wobbychip.smptweaks.PaperUtils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	private void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Villager)) { return; }
		Villager villager = (Villager) event.getRightClicked();
		if (villager.getProfession() == Profession.NONE) { return; }

		copyReputation(event.getPlayer(), villager, "MAJOR_POSITIVE");
		copyReputation(event.getPlayer(), villager, "MINOR_POSITIVE");
	}

	private void copyReputation(Player player, Villager villager, String type) {
		if (hasReputation(player, villager, type)) { return; }
		int amount = getReputation(villager, type);
		if (amount <= 0) { return; }
		PaperUtils.setReputation(villager, player.getUniqueId(), type, amount);
	}

	private boolean hasReputation(Player player, Villager villager, String type) {
		return PaperUtils.getReputation(villager, player.getUniqueId(), type) > 0;
	}

	private int getReputation(Villager villager, String type) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			int amount = PaperUtils.getReputation(villager, player.getUniqueId(), type);
			if (amount > 0) { return amount; }
		}

		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			int amount = PaperUtils.getReputation(villager, player.getUniqueId(), type);
			if (amount > 0) { return amount; }
		}

		return -1;
	}
}
