package me.serbinskis.smptweaks.custom.custompotions.custom.haste;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class HastePotion extends CustomPotion {
	public HastePotion() {
		super(PotionType.STRENGTH, Material.SUGAR, "haste", Color.fromRGB(217, 192, 67));
		this.addPotionEffect(PotionEffectType.HASTE, 3600, 0);
		this.setDisplayName("§r§fPotion of Haste");
		this.setTippedArrow(true, "§r§fArrow of Haste");
		this.setAllowVillagerTrades(true);
	}
}
