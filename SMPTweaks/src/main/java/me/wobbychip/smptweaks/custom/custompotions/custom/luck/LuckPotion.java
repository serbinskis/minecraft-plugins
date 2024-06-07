package me.wobbychip.smptweaks.custom.custompotions.custom.luck;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class LuckPotion extends CustomPotion {
	public LuckPotion() {
		super(PotionType.THICK, Material.EXPERIENCE_BOTTLE, "luck", Color.fromRGB(89, 193, 6));
		this.addPotionEffect(PotionEffectType.LUCK, 3600, 0);
		this.setDisplayName("§r§fPotion of Luck");
		this.setTippedArrow(true, "§r§fArrow of Luck");
		this.setAllowVillagerTrades(true);
	}
}
