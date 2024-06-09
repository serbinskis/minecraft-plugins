package me.wobbychip.smptweaks.custom.repairwithxp;

import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public class RepairWithXP extends CustomTweak {
	public static int task;
	public static int amountXP;
	public static int intervalTicks;
	public static List<String> mendings = Arrays.asList("mendfinity", "mending");

	public RepairWithXP() {
		super(RepairWithXP.class, false, false);
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doRepairWithXP", true, false);
		this.setReloadable(true);
		this.setDescription("Allow repairing mending tools with experience. " +
							"Put item with mending in second hand and crouch.");
	}

	public void onEnable() {
		this.onReload();

		task = TaskUtils.scheduleSyncRepeatingTask(() -> {
			Bukkit.getOnlinePlayers().forEach(this::checkPlayer);
        }, 1L, intervalTicks);
	}

	public void onReload() {
		RepairWithXP.amountXP = this.getConfig(0).getConfig().getInt("amountXP");
		RepairWithXP.intervalTicks = this.getConfig(0).getConfig().getInt("intervalTicks");
		RepairWithXP.task = TaskUtils.rescheduleSyncRepeatingTask(RepairWithXP.task, 1L, RepairWithXP.intervalTicks);
	}

	@SuppressWarnings("deprecation")
	public void checkPlayer(Player player) {
		//Check if player is sneaking
		if (!player.isSneaking()) { return; }

		//Check if gamerule enabled
		if (!this.getGameRuleBoolean(player.getWorld())) { return; }

		//Check if player has enough exp
		if (Utils.getPlayerExp(player) < amountXP) { return; }

		//Get player offhand item
		ItemStack offHand = player.getInventory().getItemInOffHand();

		//Check if item is damaged
		if (offHand.getDurability() <= 0) { return; }

		//Check if item has mending
		if (!checkEnchantments(offHand)) { return; }

		//Remove specific amount of XP from player
		player.giveExp(amountXP * -1);

		//Spawn XP orb with specific amount of XP
		ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
		orb.setExperience(amountXP);
	}

	public static boolean checkEnchantments(ItemStack item) {
		for (Entry<Enchantment, Integer> entrySet : item.getEnchantments().entrySet()) {
			if (entrySet.getValue() > 0) {
				String[] splitted = entrySet.getKey().getKey().toString().split(":");
				String name = splitted[splitted.length-1].toLowerCase();
				if (mendings.contains(name)) { return true; }
			}
		}

		return false;
	}
}
