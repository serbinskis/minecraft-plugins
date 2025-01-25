package me.serbinskis.smptweaks.custom.custompotions.custom.glowing;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class GlowingPotionStrong extends CustomPotion {
	public GlowingPotionStrong() {
		super("glowing", Material.GLOWSTONE_DUST, "strong_glowing", Color.fromRGB(255, 255, 255));
		this.addPotionEffect(PotionEffectType.GLOWING, 450, 1);
		this.setDisplayName("§r§fPotion of Glowing");
		this.setTippedArrow(true, "§r§fArrow of Glowing");
		this.setAllowVillagerTrades(false);
	}
}
