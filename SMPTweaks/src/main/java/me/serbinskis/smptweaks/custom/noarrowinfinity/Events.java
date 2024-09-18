package me.serbinskis.smptweaks.custom.noarrowinfinity;

import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {
	//BUG LIST
	//[+] Picking up tipped arrow while aiming will shoot it but not consume

	//CONCERT LIST
	//[-] While aiming player has instant build, this potentially can be exploited with modified clients

	//If arrow is CREATIVE_ONLY then PlayerPickupArrowEvent will not fire (fuck you bukkit)
	//So instead I implemented my own way of handling CREATIVE_ONLY arrows

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Arrow)) { return; }

		TaskUtils.scheduleSyncDelayedTask(() -> {
			Arrow arrow = (Arrow) event.getEntity();
			if (arrow.getPickupStatus() != PickupStatus.CREATIVE_ONLY) { return; }
			arrow.setPickupStatus(PickupStatus.ALLOWED);
			PersistentUtils.setPersistentDataBoolean(event.getEntity(), NoArrowInfinity.TAG_IS_CREATIVE_ONLY, true);
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player player)) { return; }
		if (!(event.getProjectile() instanceof Arrow) || (event.getConsumable() == null)) { return; }

		//Fix infinite bow durability & tipped arrow bug
		if (player.getGameMode() != GameMode.CREATIVE) {
			ReflectionUtils.setInstantBuild(((Player) event.getEntity()), false, false, true);
		}

		//Set isCreativeOnly if player is using infinity bow and arrow is normal arrow
		if (NoArrowInfinity.isInfinityBow(event.getBow()) && (event.getConsumable().getType() == Material.ARROW)) {
			PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.TAG_IS_CREATIVE_ONLY, true);
		}

		//Delay just in case if other plugins change pickup status
		TaskUtils.scheduleSyncDelayedTask(() -> {
			Arrow arrow = (Arrow) event.getProjectile();
			if (arrow.getPickupStatus() != PickupStatus.CREATIVE_ONLY) { return; }
			arrow.setPickupStatus(PickupStatus.ALLOWED);
			PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.TAG_IS_CREATIVE_ONLY, true);
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		if (!PersistentUtils.hasPersistentDataBoolean(event.getArrow(), NoArrowInfinity.TAG_IS_CREATIVE_ONLY)) { return; }

		//If player in creative allow them pickup arrows
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
			event.getArrow().setPickupStatus(PickupStatus.CREATIVE_ONLY);
		} else {
			//Otherwise cancel event
			event.setCancelled(true);
		}
	}

	//When player starts using bow on client side we get interaction event
	//So we can also set instant build on server side for moment when player shoots
	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if ((player.getGameMode() == GameMode.CREATIVE) || (event.getHand() == null)) { return; }

		//Give instant build and remove it in onEntityShootBowEvent
		ItemStack item = player.getInventory().getItem(event.getHand());
		if (!NoArrowInfinity.isInfinityBow(item) || NoArrowInfinity.hasArrow(player)) { return; }
		ReflectionUtils.setInstantBuild(player, true, true, true);

		//In case if player don't shoot make a timer and do checks
		int[] task = { 0 };
		task[0] = TaskUtils.scheduleSyncRepeatingTask(() -> {
			if (player.getItemInUse() == null) {
				ReflectionUtils.setInstantBuild(player, false, false, true);
				TaskUtils.cancelTask(task[0]);
			}
		}, 1L, 1L);
	}
}
