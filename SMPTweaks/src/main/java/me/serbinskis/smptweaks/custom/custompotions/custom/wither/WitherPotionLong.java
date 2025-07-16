package me.serbinskis.smptweaks.custom.custompotions.custom.wither;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class WitherPotionLong extends CustomPotion {
	public WitherPotionLong() {
		super(UnregisteredPotion.create(WitherPotion.class), Material.REDSTONE, "long_wither", Color.fromRGB(115, 97, 86));
		this.addPotionEffect(PotionEffectType.WITHER, 1800, 0);
		this.setDisplayName("§r§fPotion of Wither");
		this.setTippedArrow(true, "§r§fArrow of Wither");
		this.setAllowVillagerTrades(false);
	}
}
