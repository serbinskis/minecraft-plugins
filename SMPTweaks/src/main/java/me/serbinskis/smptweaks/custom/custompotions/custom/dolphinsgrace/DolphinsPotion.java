package me.serbinskis.smptweaks.custom.custompotions.custom.dolphinsgrace;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class DolphinsPotion extends CustomPotion {
	public DolphinsPotion() {
		super(PotionType.AWKWARD, Material.NAUTILUS_SHELL, "dolphins_grace", Color.fromRGB(136, 163, 190));
		this.addPotionEffect(PotionEffectType.DOLPHINS_GRACE, 3600, 0);
		this.setDisplayName("§r§fPotion of Dolphins Grace");
		this.setTippedArrow(true, "§r§fArrow of Dolphins Grace");
		this.setAllowVillagerTrades(true);
	}
}
