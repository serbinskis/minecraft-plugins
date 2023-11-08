package me.wobbychip.smptweaks.custom.custompotions.potions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import com.google.common.reflect.ClassPath;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class PotionManager {
	protected Map<CustomPotion, String> waiting = new HashMap<CustomPotion, String>();
	protected Map<String, CustomPotion> potions = new HashMap<String, CustomPotion>();
	protected Map<String, PotionRegistry> registry = new HashMap<String, PotionRegistry>();

	public List<CustomPotion> getPotions(ClassLoader loader, String pacakgeName) {
		List<CustomPotion> potions = new ArrayList<>();

		try {
			ClassPath classPath = ClassPath.from(Main.classLoader);

			for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses(pacakgeName)) {
				Class<?> clazz = Class.forName(classInfo.getName(), true, loader);
				potions.add((CustomPotion) clazz.getConstructor().newInstance());
			}
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return potions;
	}

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

		PotionRegistry result = ReflectionUtils.registerInstantPotion(potion.getName());
		potions.put(potion.getName(), potion);
		registry.put(potion.getName(), result);

		//Register potions that are based on current potion
		Iterator<Entry<CustomPotion, String>> iterator = waiting.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<CustomPotion, String> entry = iterator.next();
			if (entry.getValue().equals(potion.getName())) {
				entry.getKey().setBase(result);
				CustomPotions.tweak.printMessage("Registering '" + entry.getKey().getName() + "' with the base '" + potion.getName() + "'", true);
				registerPotion(entry.getKey());
				iterator.remove();
			}
		}

		//If potion disabled register it, but don't add brew recipe and events
		if (!potion.isEnabled()) { return; }
		Bukkit.getPluginManager().registerEvents(potion, Main.plugin);

		//Don't add brew recipe if material is null, used for custom events
		if (potion.getMaterial() == null) { return; }
		ReflectionUtils.registerBrewRecipe(potion.getBase(), potion.getMaterial(), result);
	}

	public static void convertPotion(String from, String to, BrewingStand where) {
		for (int i = 0; i < 3; i++) {
			ItemStack item = where.getInventory().getItem(i);
			CustomPotion customPotion = CustomPotions.manager.getCustomPotion(item);
			if ((customPotion == null) || (!customPotion.getName().equalsIgnoreCase(from))) { continue; }
			customPotion = CustomPotions.manager.getCustomPotion(to);
			if (customPotion != null) { where.getInventory().setItem(i, customPotion.setProperties(item)); }
		}
	}

	public PotionRegistry getPotionRegistry(CustomPotion potion) {
		return registry.containsKey(potion.getName()) ? registry.get(potion.getName()) : null;
	}

	public PotionRegistry getPotionRegistry(String name) {
		return registry.containsKey(name) ? registry.get(name) : null;
	}

	public CustomPotion getCustomPotion(String name) {
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(Entity entity) {
		if (!PersistentUtils.hasPersistentDataString(entity, CustomPotions.customTag)) { return null; }
		String name = PersistentUtils.getPersistentDataString(entity, CustomPotions.customTag);
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		String name = ReflectionUtils.getPotionTag(item);

		if (potions.containsKey(name)) {
			return potions.get(name);
		}

		if (PersistentUtils.hasPersistentDataString(item, CustomPotions.customTag)) {
			name = PersistentUtils.getPersistentDataString(item, CustomPotions.customTag);
		}

		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		return ReflectionUtils.getPotion(potionType, extended, upgraded);
	}
}
