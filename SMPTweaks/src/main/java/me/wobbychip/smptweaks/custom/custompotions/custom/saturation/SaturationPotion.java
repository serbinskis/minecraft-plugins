package me.wobbychip.smptweaks.custom.custompotions.custom.saturation;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class SaturationPotion extends CustomPotion {
	public SaturationPotion() {
		super(PotionType.REGENERATION, Material.GOLDEN_CARROT, "saturation", Color.fromRGB(248, 36, 36));
		this.addPotionEffect(PotionEffectType.SATURATION, 900, 0);
		this.setDisplayName("§r§fPotion of Saturation");
		this.setTippedArrow(true, "§r§fArrow of Saturation");
		this.setAllowVillagerTrades(true);
	}
}
