package me.serbinskis.smptweaks.custom.noarrowinfinity;

import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Events implements Listener {
	// When player starts using bow on client side we get interaction event
	// So we can also set instant build on server side for moment when player shoots
	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if ((player.getGameMode() == GameMode.CREATIVE) || (event.getHand() == null)) { return; }

		// Ignore left click, we don't care about block breaking
		if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) { return; }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage(event.getAction() + " | " + event.getHand()); }

		@NotNull ItemStack mainHandItem = player.getInventory().getItem(EquipmentSlot.HAND);
		boolean hasInfinityMainHand = NoArrowInfinity.isInfinityBow(mainHandItem) && !NoArrowInfinity.hasArrow(player);
		ItemStack eventHandItem = player.getInventory().getItem(event.getHand());
		boolean hasInfinityEventHand = NoArrowInfinity.isInfinityBow(eventHandItem) && !NoArrowInfinity.hasArrow(player);

		// In case if we have bow with infinity in main hand, but trying to use offhand, prevent event
		if (hasInfinityMainHand && EquipmentSlot.OFF_HAND.equals(event.getHand())) {
			if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEvent -> catch third packet"); }
			event.getPlayer().startUsingItem(EquipmentSlot.HAND);
			event.setUseItemInHand(Event.Result.DENY);
			event.setUseItemInHand(Event.Result.DENY);
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setCancelled(true);
			return;
		}

		// If we are not trying to use infinity bow in this event, just return
		if (!hasInfinityEventHand) { return; }

		// If we are trying to use bow then start using it
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEvent -> startUsingItem()"); }

		// Start using item, aka the bow
		event.getPlayer().startUsingItem(event.getHand());

		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("isCancelled: " + event.isCancelled()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useItemInHand: " + event.useItemInHand()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useInteractedBlock: " + event.useInteractedBlock()); }

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		// First check if we are trying to use off hand
		if (!EquipmentSlot.OFF_HAND.equals(event.getHand())) { return; }

		// Then check if we are currently using infinity bow
		if (!NoArrowInfinity.isInfinityBow(event.getPlayer().getItemInUse())) { return; }

		// And if we are using infinity bow then using off hand is not allowed
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEntityEvent -> setCancelled(true)"); }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerStopUsingItemEvent(PlayerStopUsingItemEvent event) {
		Player player = event.getPlayer();

		// We don't care if player is already in creative
		if (player.getGameMode() == GameMode.CREATIVE) { return; }

		// If player stoped using infinity bow continue, means we about to try to shoot an arrow
		if (!NoArrowInfinity.isInfinityBow(event.getItem())) { return; }

		// Also check if player has arrow, if he does then we don't actually care to do workaround
		if (NoArrowInfinity.hasArrow(player)) { return; }

		// We need to set instabuild true for server to allow to use bow without any arrows inside the inventory
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerStopUsingItemEvent -> setInstantBuild"); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("======================================="); }

		// Set instabuild and revert this change in the next tick
		NoArrowInfinity.setInstantBuild(event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player player)) { return; }
		if (!(event.getProjectile() instanceof Arrow) || (event.getConsumable() == null)) { return; }

		// Fix infinite bow durability & tipped arrow bug
		if (player.getGameMode() != GameMode.CREATIVE) { NoArrowInfinity.setInstantBuild(player, false); }

		// Set isCreativeOnly if player is using infinity bow and arrow is normal arrow
		if (NoArrowInfinity.isInfinityBow(event.getBow()) && (event.getConsumable().getType() == Material.ARROW)) {
			PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.TAG_IS_CREATIVE_ONLY, true);
		}

		// Delay just in case if other plugins change pickup status
		TaskUtils.scheduleSyncDelayedTask(() -> {
			Arrow arrow = (Arrow) event.getProjectile();
			if (arrow.getPickupStatus() != PickupStatus.CREATIVE_ONLY) { return; }
			arrow.setPickupStatus(PickupStatus.ALLOWED);
			PersistentUtils.setPersistentDataBoolean(event.getProjectile(), NoArrowInfinity.TAG_IS_CREATIVE_ONLY, true);
		}, 1L);
	}

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

	// If arrow is CREATIVE_ONLY then PlayerPickupArrowEvent will not fire (fuck you bukkit)
	// So instead I implemented my own way of handling CREATIVE_ONLY arrows
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
		if (!PersistentUtils.hasPersistentDataBoolean(event.getArrow(), NoArrowInfinity.TAG_IS_CREATIVE_ONLY)) { return; }

		// If player in creative allow them pickup arrows
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
			event.getArrow().setPickupStatus(PickupStatus.CREATIVE_ONLY);
		} else {
			// Otherwise cancel event
			event.setCancelled(true);
		}
	}
}
