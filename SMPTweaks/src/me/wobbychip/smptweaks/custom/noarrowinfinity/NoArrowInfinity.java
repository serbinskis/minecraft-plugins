package me.wobbychip.smptweaks.custom.noarrowinfinity;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class NoArrowInfinity extends CustomTweak {
	public static String isCreativeOnly = "isCreativeOnly";
	public static List<String> infinity = Arrays.asList("mendfinity", "infinity");

	public NoArrowInfinity() {
		super(NoArrowInfinity.class.getSimpleName(), false, false);
		this.setGameRule("doInfinityArrows", true, false);
		this.setDescription("Allows players to use a bow with infinity without arrows.");
	}

	public void onEnable() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					checkPlayer(player);
				}
			}
		}, 1L, 1L);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	//Instant build is not creative mode and it gives different perks to player
	//Such as shooting with no arrows, infinite consumables and infinite durability
	//To prevent everything from above and get only shooting with no arrows
	//Give instant build only to client and not server
	public void checkPlayer(Player player) {
		if (!this.getGameRuleBoolean(player.getWorld())) { return; }
		if (player.getGameMode() == GameMode.CREATIVE) { return; }
		if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) { return; }
		ItemStack mainahnd = player.getInventory().getItemInMainHand();
		ItemStack offhand = player.getInventory().getItemInOffHand();

		//Ignore creative players, they already have instant build
		//Prevent check if any inventory opened, except default players inventory
		//Can't check that because if no any other opened, then default will be always opened

		//Allow instant build only if infinity bow is in main hand or offhand
		//But if it is in offhand check if there is no any other bow or crossbow in mainhand

		if (isInfinityBow(mainahnd) || (isInfinityBow(offhand) && (mainahnd.getType() != Material.BOW) && (mainahnd.getType() != Material.CROSSBOW))) {
			ReflectionUtils.setInstantBuild(player, !hasArrow(player), true, false);
		} else {
			if (player.getItemInUse() != null) { return; }
			ReflectionUtils.setInstantBuild(player, false, true, true);
		}
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
}
