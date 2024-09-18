package me.serbinskis.smptweaks.custom.custompotions.custom.nausea;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class NauseaPotionLong extends CustomPotion {
	public NauseaPotionLong() {
		super("nausea", Material.REDSTONE, "long_nausea", Color.fromRGB(135, 124, 83));
		this.addPotionEffect(PotionEffectType.NAUSEA, 1800, 0);
		this.setDisplayName("§r§fPotion of Nausea");
		this.setTippedArrow(true, "§r§fArrow of Nausea");
		this.setAllowVillagerTrades(false);
	}
}
