package me.serbinskis.smptweaks.custom.custompotions.events;

import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.PotionManager;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ProjectileEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof Arrow arrow) {
			CustomPotion customPotion = PotionManager.getCustomPotion(arrow.getItemStack());
			if (customPotion == null) { return; }
			PersistentUtils.setPersistentDataString(event.getEntity(), CustomPotions.TAG_CUSTOM_POTION, customPotion.getName());
			if (event.isCancelled() || !customPotion.isEnabled()) { return; }
			customPotion.onProjectileLaunch(event);
		}

		if (event.getEntity() instanceof ThrownPotion potion) {
			if (potion.getItem().getType() != Material.SPLASH_POTION) { return; }
			CustomPotion customPotion = PotionManager.getCustomPotion(potion.getItem());
			if (customPotion == null) { return; }

			ItemStack potionItem = potion.getItem();
			PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
			potionMeta.setBasePotionType(PotionType.AWKWARD); //Otherwise the cloud will be instant, 0 ticks
			potionItem.setItemMeta(potionMeta);
			potion.setItem(potionItem);
			customPotion.onProjectileLaunch(event);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getProjectile() instanceof Arrow)) { return; }
		CustomPotion customPotion = PotionManager.getCustomPotion(event.getConsumable());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onEntityShootBowEvent(event); }

		if (!event.isCancelled()) {
			Arrow projectile = (Arrow) event.getProjectile();
			PersistentUtils.setPersistentDataString(projectile, CustomPotions.TAG_CUSTOM_POTION, customPotion.getName());
			projectile.setColor(customPotion.getColor());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		CustomPotion customPotion = PotionManager.getCustomPotion(event.getArrow());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onPlayerPickupArrow(event); }
		event.getItem().setItemStack(customPotion.getTippedArrow(true, 1));
	}
}
