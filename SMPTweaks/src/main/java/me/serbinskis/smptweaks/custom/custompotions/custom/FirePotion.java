package me.serbinskis.smptweaks.custom.custompotions.custom;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.VanillaPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

import java.util.List;

public class FirePotion extends CustomPotion {
	public FirePotion() {
		super(VanillaPotion.create(PotionType.AWKWARD), Material.FIRE_CHARGE, "fire", Color.fromRGB(226, 88, 34));
		this.setDisplayName("§r§fPotion of Fire");
		this.setLore(List.of("§9Sets on fire"));
		this.setTippedArrow(true, "§r§fArrow of Fire");
		this.setAllowVillagerTrades(true);
	}

	@Override
	public boolean onAffectLivingEntity(LivingEntity livingEntity, Event event) {
		int ticks = 20 * switch (event) {
			case PlayerItemConsumeEvent ignored -> 10;
			case PotionSplashEvent ignored -> 7;
			case ProjectileHitEvent ignored -> 5;
			case AreaEffectCloudApplyEvent ignored -> 1;
			default -> 0;
		};

		if (livingEntity.getFireTicks() < ticks)  { livingEntity.setFireTicks(ticks); }
		return true;
	}
}
