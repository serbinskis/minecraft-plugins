package me.serbinskis.smptweaks.custom.custompotions.custom.dolphinsgrace;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class DolphinsPotionStrong extends CustomPotion {
	public DolphinsPotionStrong() {
		super(UnregisteredPotion.create(DolphinsPotion.class), Material.GLOWSTONE_DUST, "strong_dolphins_grace", Color.fromRGB(136, 163, 190));
		this.addPotionEffect(PotionEffectType.DOLPHINS_GRACE, 1800, 1);
		this.setDisplayName("§r§fPotion of Dolphins Grace");
		this.setTippedArrow(true, "§r§fArrow of Dolphins Grace");
		this.setAllowVillagerTrades(false);
	}
}
