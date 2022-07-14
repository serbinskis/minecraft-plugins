package me.wobbychip.custompotions.potions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.utils.NMSTool;
import me.wobbychip.custompotions.utils.Utils;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class PotionManager {
	protected Map<CustomPotion, String> waiting = new HashMap<CustomPotion, String>();
	protected Map<String, CustomPotion> potions = new HashMap<String, CustomPotion>();
	protected Map<String, PotionRegistry> registry = new HashMap<String, PotionRegistry>();

	public String getPotions() {
		return String.join(", ", this.potions.keySet());
	}

	public Set<String> getPotionSet() {
		return this.potions.keySet();
	}

	public void registerPotion(CustomPotion potion) {
		//If base of potion is custom and not yet registered, save it for later
		if ((potion.getBase() == null) && !registry.containsKey(potion.getBaseName())) {
			Utils.sendMessage("Potion '" + potion.getName() + "' is waiting for base '" + potion.getBaseName() + "'");
			waiting.put(potion, potion.getBaseName());
			return;
		} else if (potion.getBase() == null) {
			potion.setBase(registry.get(potion.getBaseName()));
		}

		potions.put(potion.getName(), potion);
		PotionRegistry result = registerInstantPotion(potion.getName(), potion.getIntColor());
		registry.put(potion.getName(), result);

		//Register potions that are based on current potion
		Iterator<Entry<CustomPotion, String>> iterator = waiting.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<CustomPotion, String> entry = iterator.next();
			if (entry.getValue().equals(potion.getName())) {
				entry.getKey().setBase(result);
				Utils.sendMessage("Registering '" + entry.getKey().getName() + "' with the base '" + potion.getName() + "'");
				registerPotion(entry.getKey());
				iterator.remove();
			}
		}

		//If potion disabled register it, but don't add brew recipe
		if (!potion.isEnabled()) { return; }
		registerBrewRecipe(potion.getBase(), potion.getMaterial(), result);
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
		String name = NMSTool.getPotionTag(entity);
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		String name = NMSTool.getPotionTag(item);

		if (potions.containsKey(name)) {
			return potions.get(name);
		}

		if (item.getItemMeta() == null) { return null; }
		name = item.getItemMeta().getLocalizedName();
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		return NMSTool.getPotion(potionType, extended, upgraded);
	}

	public static PotionRegistry registerInstantPotion(String name, int color) {
		return NMSTool.registerInstantPotion(name, color);
	}

	public static void setRegistryFrozen(Object registry, boolean frozen) {
		NMSTool.setRegistryFrozen(registry, frozen);
	}

	public static boolean registerBrewRecipe(PotionRegistry base, Material ingredient, PotionRegistry result) {
		return NMSTool.registerBrewRecipe(base, ingredient, result);
	}
}
