package me.wobbychip.smptweaks.custom.noarrowinfinity;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;

public class NoArrowInfinity extends CustomTweak {
	public static String isCreativeOnly = "isCreativeOnly";
	public static List<String> infinity = Arrays.asList("mendfinity", "infinity");

	public NoArrowInfinity() {
		super("NoArrowInfinity");

		if (this.isEnabled()) {
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
	            public void run() {
	                for (Player player : Bukkit.getOnlinePlayers()) {
	                	checkPlayer(player);
	                }
	            }
	        }, 0L, 1L);

			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}

	//Instant build is not creative mode and it gives different perks to player
	//Such as shooting with no arrows, infinite consumables and infinite durability
	//To prevent everything from above and get only shooting with no arrows
	//Give instant build only to client and not server
	public void checkPlayer(Player player) {
		if (player.getGameMode() == GameMode.CREATIVE) { return; }
		ItemStack mainahnd = player.getInventory().getItemInMainHand();
		ItemStack offhand = player.getInventory().getItemInOffHand();

		//Allow instant build only if infinity bow is in main hand or offhand
		//But if it is in offhand check if there is no any other bow or crossbow in mainhand

		if (isInfinityBow(mainahnd) || (isInfinityBow(offhand) && (mainahnd.getType() != Material.BOW) && (mainahnd.getType() != Material.CROSSBOW))) {
			ReflectionUtils.setInstantBuild(player, !hasArrow(player), true, false);
		} else {
			ReflectionUtils.setInstantBuild(player, false, true, true);
		}
	}

	public static boolean isInfinityBow(ItemStack item) {
		if ((item == null) || (item.getType() != Material.BOW)) { return false; }
		if (!checkEnchantments(item)) { return false; }
		return true;
	}

	public static boolean checkEnchantments(ItemStack item) {
		for (Entry<Enchantment, Integer> entrySet : item.getEnchantments().entrySet()) {
			if (entrySet.getValue() > 0) {
				String[] splitted = entrySet.getKey().getKey().toString().split(":");
				String name = splitted[splitted.length-1].toLowerCase();
				if (infinity.contains(name)) { return true; }
			}
		}

		return false;
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
}
