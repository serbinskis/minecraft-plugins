package me.serbinskis.smptweaks.custom.custompotions.custom.haste;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class HastePotionStrong extends CustomPotion {
	public HastePotionStrong() {
		super(UnregisteredPotion.create(HastePotion.class), Material.GLOWSTONE, "strong_haste", Color.fromRGB(217, 192, 67));
		this.addPotionEffect(PotionEffectType.HASTE, 1800, 1);
		this.setDisplayName("§r§fPotion of Haste");
		this.setTippedArrow(true, "§r§fArrow of Haste");
		this.setAllowVillagerTrades(false);
	}
}
