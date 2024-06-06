package me.wobbychip.smptweaks.custom.custompotions.custom.nausea;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class NauseaPotion extends CustomPotion {
	public NauseaPotion() {
		super(PotionType.THICK, Material.FERMENTED_SPIDER_EYE, "nausea", Color.fromRGB(135, 124, 83));
		this.addPotionEffect(PotionEffectType.NAUSEA, 900, 0);
		this.setDisplayName("§r§fPotion of Nausea");
		this.setTippedArrow(true, "§r§fArrow of Nausea");
		this.setAllowVillagerTrades(true);
	}
}
