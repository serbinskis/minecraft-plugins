package me.serbinskis.smptweaks.custom.custompotions.custom.wither;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class WitherPotionStrong extends CustomPotion {
	public WitherPotionStrong() {
		super("wither", Material.GLOWSTONE_DUST, "strong_wither", Color.fromRGB(115, 97, 86));
		this.addPotionEffect(PotionEffectType.WITHER, 450, 1);
		this.setDisplayName("§r§fPotion of Wither");
		this.setTippedArrow(true, "§r§fArrow of Wither");
		this.setAllowVillagerTrades(false);
	}
}
