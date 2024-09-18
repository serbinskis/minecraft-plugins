package me.serbinskis.smptweaks.custom.custompotions.custom.glowing;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class GlowingPotion extends CustomPotion {
	public GlowingPotion() {
		super(PotionType.THICK, Material.GLOW_INK_SAC, "glowing", Color.fromRGB(255, 255, 255));
		this.addPotionEffect(PotionEffectType.GLOWING, 900, 0);
		this.setDisplayName("§r§fPotion of Glowing");
		this.setTippedArrow(true, "§r§fArrow of Glowing");
		this.setAllowVillagerTrades(true);
	}
}
