package me.serbinskis.smptweaks.custom.custompotions.custom.nausea;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class NauseaPotionStrong extends CustomPotion {
	public NauseaPotionStrong() {
		super("nausea", Material.GLOWSTONE_DUST, "strong_nausea", Color.fromRGB(135, 124, 83));
		this.addPotionEffect(PotionEffectType.NAUSEA, 450, 1);
		this.setDisplayName("§r§fPotion of Nausea");
		this.setTippedArrow(true, "§r§fArrow of Nausea");
		this.setAllowVillagerTrades(false);
	}
}
