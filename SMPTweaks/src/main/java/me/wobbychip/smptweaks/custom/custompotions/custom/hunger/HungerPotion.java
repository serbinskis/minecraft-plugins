package me.wobbychip.smptweaks.custom.custompotions.custom.hunger;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class HungerPotion extends CustomPotion {
	public HungerPotion() {
		super(PotionType.AWKWARD, Material.ROTTEN_FLESH, "hunger", Color.fromRGB(88, 118, 83));
		this.addPotionEffect(PotionEffectType.HUNGER, 900, 0);
		this.setDisplayName("§r§fPotion of Hunger");
		this.setTippedArrow(true, "§r§fArrow of Hunger");
		this.setAllowVillagerTrades(true);
	}
}
