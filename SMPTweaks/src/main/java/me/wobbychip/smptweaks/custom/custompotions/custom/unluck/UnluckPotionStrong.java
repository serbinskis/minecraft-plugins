package me.wobbychip.smptweaks.custom.custompotions.custom.unluck;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class UnluckPotionStrong extends CustomPotion {
	public UnluckPotionStrong() {
		super("unluck", Material.GLOWSTONE_DUST, "strong_unluck", Color.fromRGB(192, 164, 77));
		this.addPotionEffect(PotionEffectType.UNLUCK, 1800, 1);
		this.setDisplayName("§r§fPotion of Unluck");
		this.setTippedArrow(true, "§r§fArrow of Unluck");
		this.setAllowVillagerTrades(false);
	}
}
