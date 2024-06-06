package me.wobbychip.smptweaks.custom.custompotions.custom.levitation;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class LevitationPotionStrong extends CustomPotion {
	public LevitationPotionStrong() {
		super("levitation", Material.GLOWSTONE_DUST, "strong_levitation", Color.fromRGB(206, 255, 255));
		this.addPotionEffect(PotionEffectType.LEVITATION, 450, 1);
		this.setDisplayName("§r§fPotion of Levitation");
		this.setTippedArrow(true, "§r§fArrow of Levitation");
		this.setAllowVillagerTrades(false);
	}
}
