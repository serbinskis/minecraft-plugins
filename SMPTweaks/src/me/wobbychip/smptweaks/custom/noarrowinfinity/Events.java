package me.wobbychip.smptweaks.custom.noarrowinfinity;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;

public class Events implements Listener {
	//BUG LIST
	//[+] Picking up tipped arrow while aiming will shoot it but not consume

	//CONCERNT LIST
	//[-] While aiming player has instant build, this potentially can be exploited with modified clients

	//If arrow is CREATIVE_ONLY then PlayerPickupArrowEvent will not fire (fuck you bukkit)
	//So instead I implemented my own way of handling CREATIVE_ONLY arrows

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player)) { return; }
		if (!(event.getProjectile() instanceof Arrow) || (event.getConsumable() == null)) { return; }
		Player player = (Player) event.getEntity();

		//Fix infinite bow durability & tipped arrow bug
		if (player.getGameMode() != GameMode.CREATIVE) {
			ReflectionUtils.setInstantBuild(((Player) event.getEntity()), false, false, true);
		}

		//Set isCreativeOnly if player is using infinity bow and arrow is normal arrow
		if (NoArrowInfinity.isInfinityBow(event.getBow()) && (event.getConsumable().getType() == Material.ARROW)) {
			PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.isCreativeOnly, true);
		}

		//Delay just in case if other plugins change pickup status
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				Arrow arrow = (Arrow) event.getProjectile();
				if (arrow.getPickupStatus() != PickupStatus.CREATIVE_ONLY) { return; }
				arrow.setPickupStatus(PickupStatus.ALLOWED);
				PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.isCreativeOnly, true);
			}
		}, 1L);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		if (!PersistentUtils.hasPersistentDataBoolean(event.getArrow(), NoArrowInfinity.isCreativeOnly)) { return; }

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
		task[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				if (!ReflectionUtils.isUsingItem(player)) {
					ReflectionUtils.setInstantBuild(player, false, false, true);
					Bukkit.getServer().getScheduler().cancelTask(task[0]);
				}
			}
		}, 1L, 1L);
	}
}
