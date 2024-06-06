package me.wobbychip.smptweaks.custom.custompotions.custom.miningfatigue;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class MiningFatiguePotionStrong extends CustomPotion {
	public MiningFatiguePotionStrong() {
		super("mining_fatigue", Material.GLOWSTONE, "strong_mining_fatigue", Color.fromRGB(74, 66, 23));
		this.addPotionEffect(PotionEffectType.MINING_FATIGUE, 1800, 1);
		this.setDisplayName("§r§fPotion of Mining Fatigue");
		this.setTippedArrow(true, "§r§fArrow of Mining Fatigue");
		this.setAllowVillagerTrades(false);
	}
}
