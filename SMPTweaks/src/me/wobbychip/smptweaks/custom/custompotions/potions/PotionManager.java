package me.wobbychip.smptweaks.custom.custompotions.potions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.NMSUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class PotionManager {
	protected Map<CustomPotion, String> waiting = new HashMap<CustomPotion, String>();
	protected Map<String, CustomPotion> potions = new HashMap<String, CustomPotion>();
	protected Map<String, PotionRegistry> registry = new HashMap<String, PotionRegistry>();

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
			Utils.sendMessage("&9[CustomPotions] Potion '" + potion.getName() + "' is waiting for base '" + potion.getBaseName() + "'");
			waiting.put(potion, potion.getBaseName());
			return;
		} else if (potion.getBase() == null) {
			potion.setBase(registry.get(potion.getBaseName()));
		}

		Bukkit.getPluginManager().registerEvents(potion, Main.plugin);
		PotionRegistry result = NMSUtils.registerInstantPotion(potion.getName(), potion.getIntColor());
		potions.put(potion.getName(), potion);
		registry.put(potion.getName(), result);

		//Register potions that are based on current potion
		Iterator<Entry<CustomPotion, String>> iterator = waiting.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<CustomPotion, String> entry = iterator.next();
			if (entry.getValue().equals(potion.getName())) {
				entry.getKey().setBase(result);
				Utils.sendMessage("&9[CustomPotions] Registering '" + entry.getKey().getName() + "' with the base '" + potion.getName() + "'");
				registerPotion(entry.getKey());
				iterator.remove();
			}
		}

		//If potion disabled register it, but don't add brew recipe
		if (!potion.isEnabled()) { return; }
		NMSUtils.registerBrewRecipe(potion.getBase(), potion.getMaterial(), result);
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
		if (entity.getType() == EntityType.DRAGON_FIREBALL) { return null; } //This shit has no method getHandle()
		if (potions.containsKey(entity.getCustomName())) { return potions.get(entity.getCustomName()); }
		String name = NMSUtils.getPotionTag(entity);
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		String name = NMSUtils.getPotionTag(item);

		if (potions.containsKey(name)) {
			return potions.get(name);
		}

		if (item.getItemMeta() == null) { return null; }
		name = item.getItemMeta().getLocalizedName();
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		return NMSUtils.getPotion(potionType, extended, upgraded);
	}
}
