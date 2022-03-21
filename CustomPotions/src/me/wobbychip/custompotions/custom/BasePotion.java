package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;

public class BasePotion extends CustomPotion {
	public BasePotion() {
		super(PotionManager.getPotion(PotionType.AWKWARD, false, false), Material.NETHER_STAR, "base", Color.fromRGB(255, 255, 255));
		this.setDisplayName("§r§fPotion of Base");
		this.setLore(Arrays.asList("§9Used as base for other potions"));
		this.setTippedArrow(false, "§r§fArrow of Base");
	}
}
