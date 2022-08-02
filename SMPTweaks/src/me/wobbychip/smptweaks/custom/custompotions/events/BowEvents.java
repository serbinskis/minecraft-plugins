package me.wobbychip.smptweaks.custom.custompotions.events;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.PersistentUtils;

public class BowEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getProjectile() instanceof Arrow)) { return; }
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getConsumable());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onEntityShootBowEvent(event); }

		if (!event.isCancelled()) {
			Arrow projectile = (Arrow) event.getProjectile();
			PersistentUtils.setPersistentDataString(projectile, CustomPotions.customTag, customPotion.getName());
			projectile.setColor(customPotion.getColor());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getArrow());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onPlayerPickupArrow(event); }
		event.getItem().setItemStack(customPotion.getTippedArrow(true, 1));
	}
}
