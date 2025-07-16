package me.serbinskis.smptweaks.custom.custompotions.custom.blindness;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.VanillaPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class BlindnessPotion extends CustomPotion {
	public BlindnessPotion() {
		super(VanillaPotion.create(PotionType.THICK), Material.INK_SAC, "blindness", Color.fromRGB(31, 31, 35));
		this.addPotionEffect(PotionEffectType.BLINDNESS, 900, 0);
		this.setDisplayName("§r§fPotion of Blindness");
		this.setTippedArrow(true, "§r§fArrow of Blindness");
		this.setAllowVillagerTrades(true);
	}
}
