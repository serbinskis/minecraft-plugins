package me.serbinskis.smptweaks.custom.custompotions.custom.miningfatigue;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class MiningFatiguePotion extends CustomPotion {
	public MiningFatiguePotion() {
		super(PotionType.WEAKNESS, Material.PRISMARINE_SHARD, "mining_fatigue", Color.fromRGB(74, 66, 23));
		this.addPotionEffect(PotionEffectType.MINING_FATIGUE, 3600, 0);
		this.setDisplayName("§r§fPotion of Mining Fatigue");
		this.setTippedArrow(true, "§r§fArrow of Mining Fatigue");
		this.setAllowVillagerTrades(true);
	}
}
