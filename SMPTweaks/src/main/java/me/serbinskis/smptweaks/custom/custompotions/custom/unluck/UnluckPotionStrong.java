package me.serbinskis.smptweaks.custom.custompotions.custom.unluck;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class UnluckPotionStrong extends CustomPotion {
	public UnluckPotionStrong() {
		super(UnregisteredPotion.create(UnluckPotion.class), Material.GLOWSTONE_DUST, "strong_unluck", Color.fromRGB(192, 164, 77));
		this.addPotionEffect(PotionEffectType.UNLUCK, 1800, 1);
		this.setDisplayName("§r§fPotion of Unluck");
		this.setTippedArrow(true, "§r§fArrow of Unluck");
		this.setAllowVillagerTrades(false);
	}
}
