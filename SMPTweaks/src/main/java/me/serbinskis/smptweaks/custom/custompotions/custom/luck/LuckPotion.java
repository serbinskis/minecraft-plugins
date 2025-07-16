package me.serbinskis.smptweaks.custom.custompotions.custom.luck;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.VanillaPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class LuckPotion extends CustomPotion {
	public LuckPotion() {
		super(VanillaPotion.create(PotionType.THICK), Material.EXPERIENCE_BOTTLE, "lucky", Color.fromRGB(89, 193, 6));
		this.addPotionEffect(PotionEffectType.LUCK, 3600, 0);
		this.setDisplayName("§r§fPotion of Luck");
		this.setTippedArrow(true, "§r§fArrow of Luck");
		this.setAllowVillagerTrades(true);
	}
}
