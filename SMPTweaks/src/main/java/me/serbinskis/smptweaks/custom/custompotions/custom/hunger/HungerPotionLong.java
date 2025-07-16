package me.serbinskis.smptweaks.custom.custompotions.custom.hunger;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class HungerPotionLong extends CustomPotion {
	public HungerPotionLong() {
		super(UnregisteredPotion.create(HungerPotion.class), Material.REDSTONE, "long_hunger", Color.fromRGB(88, 118, 83));
		this.addPotionEffect(PotionEffectType.HUNGER, 1800, 0);
		this.setDisplayName("§r§fPotion of Hunger");
		this.setTippedArrow(true, "§r§fArrow of Hunger");
		this.setAllowVillagerTrades(false);
	}
}
