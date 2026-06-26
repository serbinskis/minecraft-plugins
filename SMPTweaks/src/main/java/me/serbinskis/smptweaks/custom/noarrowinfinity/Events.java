package me.serbinskis.smptweaks.custom.noarrowinfinity;

import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import me.serbinskis.smptweaks.library.tinyprotocol.PacketEvent;
import me.serbinskis.smptweaks.library.tinyprotocol.PacketType;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
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

import java.util.List;

public class Events implements Listener {
	// BUG LIST
	// [+] Picking up tipped arrow while aiming will shoot it but not consume

	// CONCERT LIST
	// [-] While aiming player has instant build, this potentially can be exploited with modified clients

	// If arrow is CREATIVE_ONLY then PlayerPickupArrowEvent will not fire (fuck you bukkit)
	// So instead I implemented my own way of handling CREATIVE_ONLY arrows

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

		// Fix infinite bow durability & tipped arrow bug
		if (player.getGameMode() != GameMode.CREATIVE) {
			ReflectionUtils.setInstantBuild(((Player) event.getEntity()), false, false, true);
		}

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

		// When player interacts with block while holding infinity bow the order is HAND -> startUsingItem -> OFF_AND
		// So we need prevent second OFF_HAND interaction to keep using the bow
		//if (NoArrowInfinity.DEBUG) { Utils.sendMessage(hasInfinityMainHand + " | " + (player.getItemInUse() != null) + " | " + event.getHand().equals(EquipmentSlot.OFF_HAND)); }
		if (hasInfinityMainHand && (player.getItemInUse() != null) && event.getHand().equals(EquipmentSlot.OFF_HAND)) {
			if (NoArrowInfinity.DEBUG) { Utils.sendMessage("STOP DUPE EVENT"); }
			event.setUseItemInHand(Event.Result.DENY);
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setCancelled(true);
			return;
		}

		// In case if we have bow with infinity in main hand, but trying to use offhand, prevent event
		if (hasInfinityMainHand && EquipmentSlot.OFF_HAND.equals(event.getHand())) {
			event.setUseItemInHand(Event.Result.DENY);
			event.setUseItemInHand(Event.Result.DENY);
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setCancelled(true);
			return;
		}

		// If we are not trying to use infinity bow in this event, just return
		if (!hasInfinityEventHand) { return; }

		// If we are trying to use bow then start using it
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("getPlayer().startUsingItem()"); }

		// Start using item, aka the bow
		event.getPlayer().startUsingItem(event.getHand());

		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("isCancelled: " + event.isCancelled()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useItemInHand: " + event.useItemInHand()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useInteractedBlock: " + event.useInteractedBlock()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("======================================="); }
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
		ReflectionUtils.setInstantBuild(player, true, false, true);

		// Revert this change in the next tick
		TaskUtils.scheduleSyncDelayedTask(() -> ReflectionUtils.setInstantBuild(player, false, false, true), 0L);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPacketEvent(PacketEvent event) {
		// When in creative mode client only sends 2 packets: USE_ITEM_ON (MAINHAND) -> USE_ITEM
		// But if in survival we receive 3 packets: USE_ITEM_ON (MAINHAND) -> USE_ITEM -> USE_ITEM_ON (OFFHAND)
		// The third packet breaks the logic and doesn't allow drawing bow while looking at the ground
		// So the only way I found was to remove this packet if player is holding infinity bow
		// BUG: THERE IS NOW A GHOST PLACEMENT WHEN HOLDING BLOCK IN OFFHAND, SINCE SERVER DOESN'T SYNC (KINDA FIXED)
		// NOTE: INTERACT -> Event for entities (used to prevent sulfur cube interaction)

		if (!List.of(PacketType.USE_ITEM_ON, PacketType.USE_ITEM, PacketType.INTERACT).contains(event.getPacketType())) { return; }
		boolean hasInstaBuildTag = NoArrowInfinity.hasPlayerInfinityTag(event.getPlayer());
		boolean isOffhand = EquipmentSlot.OFF_HAND.equals(ReflectionUtils.getUseItemOnEventHand(event.getPacket()));

		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("Packet: " + event.getPacketType() + " | offhand: " + isOffhand + " | hasPlayerInfinityTag: " + hasInstaBuildTag); }
		if (hasInstaBuildTag && isOffhand) { event.setCancelled(true); }

		// This fixes shield and swing bug, and also prediction ghost blocks
		ReflectionUtils.syncPlayer(event.getPlayer(), true, event.getPacket());
	}
}
