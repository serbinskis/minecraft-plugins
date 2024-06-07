package me.wobbychip.smptweaks.custom.custompotions.custom.luck;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class LuckPotionLong extends CustomPotion {
	public LuckPotionLong() {
		super("luck", Material.REDSTONE, "long_luck", Color.fromRGB(89, 193, 6));
		this.addPotionEffect(PotionEffectType.LUCK, 9600, 0);
		this.setDisplayName("§r§fPotion of Luck");
		this.setTippedArrow(true, "§r§fArrow of Luck");
		this.setAllowVillagerTrades(false);
	}
}
