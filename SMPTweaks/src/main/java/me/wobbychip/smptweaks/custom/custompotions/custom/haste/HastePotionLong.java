package me.wobbychip.smptweaks.custom.custompotions.custom.haste;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class HastePotionLong extends CustomPotion {
	public HastePotionLong() {
		super("haste", Material.REDSTONE, "long_haste", Color.fromRGB(217, 192, 67));
		this.addPotionEffect(PotionEffectType.HASTE, 9600, 0);
		this.setDisplayName("§r§fPotion of Haste");
		this.setTippedArrow(true, "§r§fArrow of Haste");
		this.setAllowVillagerTrades(false);
	}
}
