package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;

import java.util.List;

public class BasePotion extends CustomPotion {
	public BasePotion() {
		super(PotionType.AWKWARD, Material.NETHER_STAR, "base", Color.fromRGB(255, 255, 255));
		this.setDisplayName("§r§fPotion of Base");
		this.setLore(List.of("§9Used as base for other potions"));
		this.setTippedArrow(false, "§r§fArrow of Base");
		this.setAllowVillagerTrades(false);
	}
}
