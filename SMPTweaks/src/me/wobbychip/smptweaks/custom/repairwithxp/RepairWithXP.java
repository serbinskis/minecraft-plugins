package me.wobbychip.smptweaks.custom.repairwithxp;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class RepairWithXP extends CustomTweak {
	public static int amountXP;
	public static int intervalTicks;
	public static Config config;
	public static List<String> mendings = Arrays.asList("mendfinity", "mending");

	public RepairWithXP() {
		super("RepairWithXP");

		if (this.isEnabled()) {
			loadConfig();

			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
	            public void run() {
	                for (Player player : Bukkit.getOnlinePlayers()) {
	                	checkPlayer(player);
	                }
	            }
	        }, 0L, intervalTicks);

			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(RepairWithXP.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		RepairWithXP.config = new Config(configPath, "/tweaks/RepairWithXP/config.yml");

		RepairWithXP.amountXP = RepairWithXP.config.getConfig().getInt("amountXP");
		RepairWithXP.intervalTicks = RepairWithXP.config.getConfig().getInt("intervalTicks");
	}

	@SuppressWarnings("deprecation")
	public static void checkPlayer(Player player) {
		//Check if player is sneaking
		if (!player.isSneaking()) {
			return;
		}

		//Check if player has enough exp
		if (Utils.getPlayerExp(player) < amountXP) {
			return;
		}

		//Get player off hand item
		ItemStack offHand = player.getInventory().getItemInOffHand();

		//Check if item is damaged
		if (offHand.getDurability() <= 0) {
			return;
		}

		//Check if item has mending
		if (!checkEnchantments(offHand)) {
			return;
		}

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
