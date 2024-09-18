package me.serbinskis.smptweaks.custom.custompotions.custom.blindness;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class BlindnessPotionLong extends CustomPotion {
	public BlindnessPotionLong() {
		super("blindness", Material.REDSTONE, "long_blindness", Color.fromRGB(31, 31, 35));
		this.addPotionEffect(PotionEffectType.BLINDNESS, 1800, 0);
		this.setDisplayName("§r§fPotion of Blindness");
		this.setTippedArrow(true, "§r§fArrow of Blindness");
		this.setAllowVillagerTrades(false);
	}
}
