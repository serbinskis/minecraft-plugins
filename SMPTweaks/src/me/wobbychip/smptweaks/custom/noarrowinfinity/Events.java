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
	//If arrow is CREATIVE_ONLY then PlayerPickupArrowEvent will not fire (fuck you bukkit)
	//So instead I implemented my own way of handling CREATIVE_ONLY arrows

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getProjectile() instanceof Arrow)) { return; }
		if (!(event.getEntity() instanceof Player)) { return; }
		if ((event.getConsumable() == null) || (event.getConsumable().getType() != Material.ARROW)) { return; }
		Player player = (Player) event.getEntity();

		//If player is in creative set isCreativeOnly
		if (player.getGameMode() == GameMode.CREATIVE) {
			PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.isCreativeOnly, true);
		}

		//Set isCreativeOnly if player is holding infinity bow
		if (!NoArrowInfinity.isInfinityBow(event.getBow())) { return; }
		PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.isCreativeOnly, true);

		//Fix infinite bow durability
		if (player.getGameMode() != GameMode.CREATIVE) {
			ReflectionUtils.setInstantBuild(((Player) event.getEntity()), false, true, true);
		}

		//Delay just in case if other plugins change pickup status
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        public void run() {
	        	((Arrow) event.getProjectile()).setPickupStatus(PickupStatus.ALLOWED);
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
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) { return; }

		if (event.getHand() == null) { return; }
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItem(event.getHand());

		//Give instant build and remove it in onEntityShootBowEvent
		if (!NoArrowInfinity.isInfinityBow(item) || NoArrowInfinity.hasArrow(player)) { return; }
		ReflectionUtils.setInstantBuild(player, true, true, true);

		//In case if player didn't shoot make a timer and do checks
		int[] task = { 0 };
		task[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
	        public void run() {
	        	if (!player.isOnline() || !ReflectionUtils.isUsingItem(player)) {
	        		ReflectionUtils.setInstantBuild(player, false, true, true);
	        		Bukkit.getServer().getScheduler().cancelTask(task[0]);
	        	}
	        }
	    }, 1L, 1L);
	}
}
