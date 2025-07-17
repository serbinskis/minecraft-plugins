package me.serbinskis.smptweaks.custom.custompotions.potions;

import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import org.bukkit.Bukkit;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Entity;
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

		Bukkit.getPluginManager().registerEvents(potion, Main.plugin);
		potion.getPotionMixes().forEach((mix) -> Bukkit.getPotionBrewer().addPotionMix(mix));
		CustomPotions.tweak.printMessage("Registered '" + potion.getName() + "' with the base '" + potion.getBase().getName() + "'", true);
		return potions.compute(potion.getName(), (k, v) -> potion);



		/*//If base of potion is custom and not yet registered, save it for later
		if ((potion.getBase() == null) && !registry.containsKey(potion.getBaseName())) {
			CustomPotions.tweak.printMessage("Potion '" + potion.getName() + "' is waiting for base '" + potion.getBaseName() + "'", true);
			waiting.put(potion, potion.getBaseName());
			return;
		} else if (potion.getBase() == null) {
			potion.setBase(registry.get(potion.getBaseName()));
		}

		CustomPotions.tweak.printMessage("Registering '" + potion.getName() + "' with the base '" + potion.getBaseName() + "'", true);
		Object result = ReflectionUtils.registerInstantPotion(potion.getName());
		potions.put(potion.getName(), potion);
		registry.put(potion.getName(), result);

		while (true) {
			CustomPotion toRemove = null;

			for (Entry<CustomPotion, String> entry : waiting.entrySet()) {
				if (entry.getValue().equals(potion.getName())) {
					entry.getKey().setBase(result);
					toRemove = entry.getKey();
					registerPotion(toRemove);
					break;
				}
			}

			if (toRemove != null) { waiting.remove(toRemove); }
			if (toRemove == null) { break; }
		}

		//If potion disabled register it, but don't add brew recipe and events
		if (!potion.isEnabled()) { return; }
		Bukkit.getPluginManager().registerEvents(potion, Main.plugin);

		//Don't add brew recipe if material is null, used for custom events
		if (potion.getMaterial() == null) { return; }
		ReflectionUtils.registerBrewingRecipe(potion.getBase(), potion.getMaterial(), result);*/
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
