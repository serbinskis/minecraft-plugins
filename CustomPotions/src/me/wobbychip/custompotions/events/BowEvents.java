package me.wobbychip.custompotions.events;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import me.wobbychip.custompotions.Main;
import me.wobbychip.custompotions.potions.CustomPotion;

public class BowEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getProjectile() instanceof Arrow)) { return; }
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getConsumable());
		if (customPotion == null) { return; }
		customPotion.onEntityShootBowEvent(event);

		if (!event.isCancelled()) {
			Arrow projectile = (Arrow) event.getProjectile();
			projectile.setCustomName(customPotion.getName());
			projectile.setCustomNameVisible(false);
			projectile.setColor(customPotion.getColor());
		}
    }

	//Dispenser doesn't work because bukkit is again garbage and doesn't even apply metdata from item to arrow
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getEntity());
		if (customPotion != null) { customPotion.onProjectileHit(event); }
    }

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getArrow());
		if (customPotion == null) { return; }
		customPotion.onPlayerPickupArrow(event);

		if (!event.isCancelled()) {
			 event.getItem().setItemStack(customPotion.getTippedArrow(true, 1));
		}
    }
}
