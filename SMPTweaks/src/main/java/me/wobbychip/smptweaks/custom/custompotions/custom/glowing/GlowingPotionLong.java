package me.wobbychip.smptweaks.custom.custompotions.custom.glowing;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class GlowingPotionLong extends CustomPotion {
	public GlowingPotionLong() {
		super("glowing", Material.REDSTONE, "long_glowing", Color.fromRGB(255, 255, 255));
		this.addPotionEffect(PotionEffectType.GLOWING, 1800, 0);
		this.setDisplayName("§r§fPotion of Glowing");
		this.setTippedArrow(true, "§r§fArrow of Glowing");
		this.setAllowVillagerTrades(false);
	}
}
