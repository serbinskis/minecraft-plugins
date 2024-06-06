package me.wobbychip.smptweaks.custom.custompotions.custom.dolphinsgrace;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class DolphinsPotionStrong extends CustomPotion {
	public DolphinsPotionStrong() {
		super("dolphins_grace", Material.GLOWSTONE_DUST, "strong_dolphins_grace", Color.fromRGB(136, 163, 190));
		this.addPotionEffect(PotionEffectType.DOLPHINS_GRACE, 1800, 1);
		this.setDisplayName("§r§fPotion of Dolphins Grace");
		this.setTippedArrow(true, "§r§fArrow of Dolphins Grace");
		this.setAllowVillagerTrades(false);
	}
}
