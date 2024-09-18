package me.serbinskis.smptweaks.custom.custompotions.custom.dolphinsgrace;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class DolphinsPotionLong extends CustomPotion {
	public DolphinsPotionLong() {
		super("dolphins_grace", Material.REDSTONE, "long_dolphins_grace", Color.fromRGB(136, 163, 190));
		this.addPotionEffect(PotionEffectType.DOLPHINS_GRACE, 9600, 0);
		this.setDisplayName("§r§fPotion of Dolphins Grace");
		this.setTippedArrow(true, "§r§fArrow of Dolphins Grace");
		this.setAllowVillagerTrades(false);
	}
}
