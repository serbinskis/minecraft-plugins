package me.serbinskis.smptweaks.custom.custompotions.custom.levitation;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class LevitationPotionLong extends CustomPotion {
	public LevitationPotionLong() {
		super(UnregisteredPotion.create(LevitationPotion.class), Material.REDSTONE, "long_levitation", Color.fromRGB(206, 255, 255));
		this.addPotionEffect(PotionEffectType.LEVITATION, 1800, 0);
		this.setDisplayName("§r§fPotion of Levitation");
		this.setTippedArrow(true, "§r§fArrow of Levitation");
		this.setAllowVillagerTrades(false);
	}
}
