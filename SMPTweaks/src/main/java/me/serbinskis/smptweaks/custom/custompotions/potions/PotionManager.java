package me.serbinskis.smptweaks.custom.custompotions.potions;

import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import org.bukkit.Bukkit;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.Map.Entry;

public class PotionManager {
	protected Map<CustomPotion, String> waiting = new HashMap<>();
	protected Map<String, CustomPotion> potions = new HashMap<>();
	protected Map<String, Object> registry = new HashMap<>();

	public List<CustomPotion> getPotions(boolean includeDisabled) {
		List<CustomPotion> result = new ArrayList<>();

		for (CustomPotion potion : potions.values()) {
			if (includeDisabled || potion.isEnabled()) { result.add(potion); }
		}

		return result;
	}

	public String getPotionsString() {
		return String.join(", ", potions.keySet());
	}

	public Set<String> getPotionSet() {
		return potions.keySet();
	}

	public void registerPotion(CustomPotion potion) {
		//If base of potion is custom and not yet registered, save it for later
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
		ReflectionUtils.registerBrewingRecipe(potion.getBase(), potion.getMaterial(), result);
	}

	public static void convertPotion(String from, String to, BrewingStand where) {
		for (int i = 0; i < 3; i++) {
			ItemStack item = where.getInventory().getItem(i);
			CustomPotion customPotion = CustomPotions.manager.getCustomPotion(item);
			if ((customPotion == null) || (!customPotion.getName().equalsIgnoreCase(from))) { continue; }
			customPotion = CustomPotions.manager.getCustomPotion(to);
			if (customPotion != null) { where.getInventory().setItem(i, customPotion.setProperties(item, true)); }
		}
	}

	public Object getPotionRegistry(CustomPotion potion) {
		return registry.getOrDefault(potion.getName(), null);
	}

	public Object getPotionRegistry(String name) {
		return registry.getOrDefault(name, null);
	}

	public boolean isCustomPotion(String name) {
		return getCustomPotion(name) != null;
	}

	public CustomPotion getCustomPotion(String name) {
		return potions.getOrDefault(name, null);
	}

	public CustomPotion getCustomPotion(Entity entity) {
		if (!PersistentUtils.hasPersistentDataString(entity, CustomPotions.TAG_CUSTOM_POTION)) { return null; }
		String name = PersistentUtils.getPersistentDataString(entity, CustomPotions.TAG_CUSTOM_POTION);
		return potions.getOrDefault(name, null);
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		String name = ReflectionUtils.getPotionTag(item);

		if (potions.containsKey(name)) {
			return potions.get(name);
		}

		if (PersistentUtils.hasPersistentDataString(item, CustomPotions.TAG_CUSTOM_POTION)) {
			name = PersistentUtils.getPersistentDataString(item, CustomPotions.TAG_CUSTOM_POTION);
		}

		return potions.getOrDefault(name, null);
	}

	public static Object getPotion(PotionType potionType) {
		return ReflectionUtils.getNMSPotion(potionType);
	}
}
