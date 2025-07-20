package me.serbinskis.smptweaks.custom.custompotions.potions;

import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import org.bukkit.Bukkit;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PotionManager {
	private static final Map<String, CustomPotion> potions = new HashMap<>();

	public static List<CustomPotion> getPotions(boolean includeDisabled) {
		List<CustomPotion> result = new ArrayList<>();

		for (CustomPotion potion : potions.values()) {
			if (includeDisabled || potion.isEnabled()) { result.add(potion); }
		}

		return result;
	}

	public static String getPotionsString() {
		return String.join(", ", potions.keySet());
	}

	public static Set<String> getPotionSet() {
		return potions.keySet();
	}

	public static void unregisterAll() {
		for (CustomPotion potion : potions.values()) {
			HandlerList.unregisterAll(potion);
			potion.getPotionMixes().forEach(mix -> Bukkit.getPotionBrewer().removePotionMix(mix.getKey()));
			CustomPotions.tweak.printMessage("Unregistered '" + potion.getName() + "' with the base '" + potion.getBase().getName() + "'", true);
		}

		potions.clear();
	}

	public static CustomPotion registerPotion(CustomPotion potion) {
		if (potions.containsKey(potion.getName())) { return potions.get(potion.getName()); }
		if (!potion.isEnabled()) { CustomPotions.tweak.printMessage("Skipping registering disabled potion: " + potion.getName(), true); }
		if (!potion.isEnabled()) { return null; }

		//In case if base potion is not yet registered, we first register base potion
		//This recursion will fail if one of the base potions cannot be registered, for unknow reason
		if (potion.getBase() instanceof UnregisteredPotion unregisteredPotion) {
			CustomPotion success = registerPotion(unregisteredPotion.getCustomPotion());
			if (success == null) { CustomPotions.tweak.printMessage("Failed to register potion: " + unregisteredPotion.getName(), true); }
			if (success == null) { return null; } else { potion.setBase(success); }
		}

		Bukkit.getPluginManager().registerEvents(potion, Main.getPlugin());
		potion.getPotionMixes().forEach(mix -> Bukkit.getPotionBrewer().addPotionMix(mix));
		CustomPotions.tweak.printMessage("Registered '" + potion.getName() + "' with the base '" + potion.getBase().getName() + "'", true);
		return potions.compute(potion.getName(), (k, v) -> potion);
	}

	public static void convertPotion(String from, String to, BrewingStand where) {
		for (int i = 0; i < 3; i++) {
			ItemStack item = where.getInventory().getItem(i);
			CustomPotion customPotion = PotionManager.getCustomPotion(item);
			if ((customPotion == null) || (!customPotion.getName().equalsIgnoreCase(from))) { continue; }
			customPotion = PotionManager.getCustomPotion(to);
			if (customPotion != null) { where.getInventory().setItem(i, customPotion.setProperties(item)); }
		}
	}

	public static boolean isCustomPotion(String name) {
		return getCustomPotion(name) != null;
	}

	public static CustomPotion getCustomPotion(String name) {
		return potions.getOrDefault(name, null);
	}

	public static CustomPotion getCustomPotion(Entity entity) {
		if (!PersistentUtils.hasPersistentDataString(entity, CustomPotions.TAG_CUSTOM_POTION)) { return null; }
		String name = PersistentUtils.getPersistentDataString(entity, CustomPotions.TAG_CUSTOM_POTION);
		return potions.getOrDefault(name, null);
	}

	public static CustomPotion getCustomPotion(ItemStack item) {
		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		if (!PersistentUtils.hasPersistentDataString(item, CustomPotions.TAG_CUSTOM_POTION)) { return null; }
		String name = PersistentUtils.getPersistentDataString(item, CustomPotions.TAG_CUSTOM_POTION);
		return potions.getOrDefault(name, null);
	}
}
