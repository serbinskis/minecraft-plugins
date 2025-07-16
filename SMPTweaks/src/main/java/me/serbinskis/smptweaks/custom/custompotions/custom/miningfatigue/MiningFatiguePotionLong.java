package me.serbinskis.smptweaks.custom.custompotions.custom.miningfatigue;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class MiningFatiguePotionLong extends CustomPotion {
	public MiningFatiguePotionLong() {
		super(UnregisteredPotion.create(MiningFatiguePotion.class), Material.REDSTONE, "long_mining_fatigue", Color.fromRGB(74, 66, 23));
		this.addPotionEffect(PotionEffectType.MINING_FATIGUE, 9600, 0);
		this.setDisplayName("§r§fPotion of Mining Fatigue");
		this.setTippedArrow(true, "§r§fArrow of Mining Fatigue");
		this.setAllowVillagerTrades(false);
	}
}
