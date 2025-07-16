package me.serbinskis.smptweaks.custom.custompotions.custom.saturation;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class SaturationPotionLong extends CustomPotion {
	public SaturationPotionLong() {
		super(UnregisteredPotion.create(SaturationPotion.class), Material.REDSTONE, "long_saturation", Color.fromRGB(248, 36, 36));
		this.addPotionEffect(PotionEffectType.SATURATION, 1800, 0);
		this.setDisplayName("§r§fPotion of Saturation");
		this.setTippedArrow(true, "§r§fArrow of Saturation");
		this.setAllowVillagerTrades(false);
	}
}
