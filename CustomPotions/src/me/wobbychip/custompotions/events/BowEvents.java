package me.wobbychip.custompotions.events;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import me.wobbychip.custompotions.Main;
import me.wobbychip.custompotions.potions.CustomPotion;

public class BowEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getProjectile() instanceof Arrow)) { return; }
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getConsumable());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onEntityShootBowEvent(event); }

		if (!event.isCancelled()) {
			Arrow projectile = (Arrow) event.getProjectile();
			projectile.setCustomName(customPotion.getName());
			projectile.setCustomNameVisible(false);
			projectile.setColor(customPotion.getColor());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getArrow());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onPlayerPickupArrow(event); }
		event.getItem().setItemStack(customPotion.getTippedArrow(true, 1));
	}
}
