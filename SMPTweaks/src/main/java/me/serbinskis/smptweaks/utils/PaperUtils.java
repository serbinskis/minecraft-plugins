package me.serbinskis.smptweaks.utils;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import io.papermc.paper.entity.Leashable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;

import java.util.Objects;
import java.util.UUID;

public class PaperUtils {
	public static boolean isPaper() {
		return Objects.nonNull(ReflectionUtils.loadClass("com.destroystokyo.paper.ParticleBuilder", false));
	}

	public static boolean isLeashable(Entity entity) {
		if (!(entity instanceof Leashable leashable)) { return false; }
		if (entity instanceof Shulker) { return false; }
		if (entity instanceof EnderDragon) { return false; }
		if (entity instanceof Wither) { return false; }
		return !leashable.isLeashed();
	}

	public static int getPlayerReputation(Villager villager, UUID uuid, ReputationType type) {
		return villager.getReputation(uuid).getReputation(type);
	}

	public static void setPlayerReputation(Villager villager, UUID uuid, ReputationType type, int amount) {
		Reputation reputation = new Reputation();
		reputation.setReputation(type, amount);
		villager.setReputation(uuid, reputation);
	}

	public static void copyExtremeReputation(Player player, Villager villager, ReputationType type, boolean max) {
		int amount = getReputationExtreme(villager, type, max);
		if (!max) { setPlayerReputation(villager, player.getUniqueId(), type, amount); } // Set min value even if it is 0
		if (max && (amount > 0)) { setPlayerReputation(villager, player.getUniqueId(), type, amount); }
	}

	public static int getReputationExtreme(Villager villager, ReputationType type, boolean max) {
		int result = 0;

		// This gets all players even online ones
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			int amount = getPlayerReputation(villager, player.getUniqueId(), type);
			if (max && (amount > result)) { result = amount; }
			if (!max && (amount < result)) { result = amount; }
		}

		return result;
	}

	public static void copyPositiveReputation(Player player, Villager villager) {
		copyExtremeReputation(player, villager, ReputationType.MAJOR_POSITIVE, true);
		copyExtremeReputation(player, villager, ReputationType.MINOR_POSITIVE, true);
	}

	public static int getTick() {
		return Bukkit.getServer().getCurrentTick();
	}
}