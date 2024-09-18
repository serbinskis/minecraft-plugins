package me.serbinskis.smptweaks.custom.custompotions.custom.unluck;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class UnluckPotion extends CustomPotion {
	public UnluckPotion() {
		super("luck", Material.FERMENTED_SPIDER_EYE, "unluck", Color.fromRGB(192, 164, 77));
		this.addPotionEffect(PotionEffectType.UNLUCK, 3600, 0);
		this.setDisplayName("§r§fPotion of Unluck");
		this.setTippedArrow(true, "§r§fArrow of Unluck");
		this.setAllowVillagerTrades(true);
	}
}
