package me.serbinskis.smptweaks.custom.custompotions.custom.unluck;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class UnluckPotionLong extends CustomPotion {
	public UnluckPotionLong() {
		super(UnregisteredPotion.create(UnluckPotion.class), Material.REDSTONE, "long_unluck", Color.fromRGB(192, 164, 77));
		this.addPotionEffect(PotionEffectType.UNLUCK, 9600, 0);
		this.setDisplayName("§r§fPotion of Unluck");
		this.setTippedArrow(true, "§r§fArrow of Unluck");
		this.setAllowVillagerTrades(false);
	}
}
