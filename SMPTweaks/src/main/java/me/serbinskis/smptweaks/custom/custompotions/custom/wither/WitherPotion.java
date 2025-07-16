package me.serbinskis.smptweaks.custom.custompotions.custom.wither;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.VanillaPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class WitherPotion extends CustomPotion {
	public WitherPotion() {
		super(VanillaPotion.create(PotionType.AWKWARD), Material.WITHER_ROSE, "wither", Color.fromRGB(115, 97, 86));
		this.addPotionEffect(PotionEffectType.WITHER, 900, 0);
		this.setDisplayName("§r§fPotion of Wither");
		this.setTippedArrow(true, "§r§fArrow of Wither");
		this.setAllowVillagerTrades(true);
	}
}
