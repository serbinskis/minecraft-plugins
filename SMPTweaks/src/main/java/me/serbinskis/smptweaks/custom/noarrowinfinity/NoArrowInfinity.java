package me.serbinskis.smptweaks.custom.noarrowinfinity;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class NoArrowInfinity extends CustomTweak {
	public static List<String> infinity = Arrays.asList("mendfinity", "infinity");
	public static HashMap<UUID, Integer> delayed = new HashMap<>();
	public static boolean DEBUG = true;

	public NoArrowInfinity() {
		super(NoArrowInfinity.class, true, false);
		this.setGameRule("bow_infinity_arrows", true, false);
		this.setDescription("Allows players to use a bow with infinity without arrows.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}

	public static boolean isInfinityBow(ItemStack item) {
		if ((item == null) || (item.getType() != Material.BOW)) { return false; }
		return Utils.containsEnchantment(item, infinity);
	}

	public static boolean hasArrow(Player player) {
		if (isArrow(player.getInventory().getItemInOffHand())) { return true; }

		for (ItemStack item : player.getInventory().getStorageContents()) {
			if (isArrow(item)) { return true; }
		}

		return false;
	}

	public static boolean isArrow(ItemStack item) {
		return ((item != null) && ((item.getType() == Material.ARROW) || (item.getType() == Material.TIPPED_ARROW) || (item.getType() == Material.SPECTRAL_ARROW)));
	}

	public static void doInstantBuild(Player player, boolean createNewTask) {
		UUID playerId = player.getUniqueId();

		// In case if task already exists finish it
		Integer taskId = NoArrowInfinity.delayed.remove(playerId);
		if (taskId != null) { TaskUtils.finishTask(taskId); }

		// In case if we are only checking existing tasks
		if (!createNewTask) { return; }

		// Create new delayed task for instabuild reversion
		taskId = TaskUtils.scheduleSyncDelayedTask(() -> {
			ReflectionUtils.setInstantBuild(player, false, false, true);
			NoArrowInfinity.delayed.remove(playerId);
		}, 0L);

		ReflectionUtils.setInstantBuild(player, true, false, true);
		NoArrowInfinity.delayed.put(playerId, taskId);
	}
}
