package me.serbinskis.smptweaks.custom.noarrowinfinity;

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
		// There is some something that happens before this event, that requires isntabuild: true
		// Otherwise we won't start drawing bow while looking at block

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
		// Or prevent dupe exploits while drawing a bow, because while drawing a bow we have instabuild on server side
		//if (NoArrowInfinity.DEBUG) { Utils.sendMessage(hasInfinityMainHand + " | " + (player.getItemInUse() != null) + " | " + event.getHand().equals(EquipmentSlot.OFF_HAND)); }
		if (hasInfinityMainHand && (player.getItemInUse() != null) && event.getHand().equals(EquipmentSlot.OFF_HAND)) {
			if (NoArrowInfinity.DEBUG) { Utils.sendMessage("STOP DUPE EVENT"); }
			event.setUseItemInHand(Event.Result.DENY);
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setCancelled(true);
			return;
		}

		// In case if we have bow with infinity in main hand, but trying to use offhand, prevent event
		if (hasInfinityMainHand && event.getHand().equals(EquipmentSlot.OFF_HAND)) { event.setUseItemInHand(Event.Result.DENY); return; }

		// If we are not trying to use infinity bow in this event, just return
		if (!hasInfinityEventHand) { return; }

		// If we are trying to use bow then start using it
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("ReflectionUtils.setInstantBuild"); }

		// Even tho we start using item, at the end of this event it for some reason reverts it
		// That reason is extra packet, but it doesn't matter since we still need instabuild
		// When releasing bow, otherwise it won't shoot bow, and we can't detect bow shoot
		// So we just have to hold instabuild while drawing bow the entire time
		//ReflectionUtils.startUsingItem(player, event.getHand());

		// Give instant build and remove it in onEntityShootBowEvent or if player stops using bow
		ReflectionUtils.setInstantBuild(player, true, false, true);
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("isCancelled: " + event.isCancelled()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useItemInHand: " + event.useItemInHand()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useInteractedBlock: " + event.useInteractedBlock()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("======================================="); }

		// This prevents ghost item modification because of extra packet
		if (!NoArrowInfinity.USE_GHOST_PATCH) { player.updateInventory(); }

		// In case if player don't shoot make a timer and do checks
		int[] task = { 0 };
		task[0] = TaskUtils.scheduleSyncRepeatingTask(() -> {
			if (player.getItemInUse() == null) {
				ReflectionUtils.setInstantBuild(player, false, false, true);
				TaskUtils.cancelTask(task[0]);
			}
		}, 1L, 1L);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!EntityType.SULFUR_CUBE.equals(event.getRightClicked().getType())) { return; }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEntityEvent -> SULFUR_CUBE"); }
		if (!NoArrowInfinity.hasInstaBuildTag(event.getPlayer())) { return; }
		event.setCancelled(true); // Prevent item duplication via sulfur cubes
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPacketEvent(PacketEvent event) {
		// When in creative mode client only sends 2 packets: USE_ITEM_ON (MAINHAND) -> USE_ITEM
		// But if in survival we receive 3 packets: USE_ITEM_ON (MAINHAND) -> USE_ITEM -> USE_ITEM_ON (OFFHAND)
		// The third packet breaks the logic and doesn't allow drawing bow while looking at the ground
		// So the only way I found was to remove this packet if player is holding infinity bow
		// BUG: THERE IS NOW A GHOST PLACEMENT WHEN HOLDING BLOCK IN OFFHAND, SINCE SERVER DOESN'T SYNC (KINDA FIXED)

		if (!List.of(PacketType.USE_ITEM_ON, PacketType.USE_ITEM).contains(event.getPacketType())) { return; }
		boolean hasInstaBuildTag = NoArrowInfinity.hasInstaBuildTag(event.getPlayer());
		boolean isOffhand = EquipmentSlot.OFF_HAND.equals(ReflectionUtils.getUseItemOnEventHand(event.getPacket()));

		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("Packet: " + event.getPacketType() + " | offhand: " + isOffhand + " | instabuild: " + hasInstaBuildTag); }
		if (hasInstaBuildTag && isOffhand) { event.setCancelled(true); }

		// This fixes shield and swing bug, and also prediction ghost blocks
		if (!NoArrowInfinity.USE_GHOST_PATCH) { ReflectionUtils.syncPlayer(event.getPlayer(), true, event.getPacket()); }
	}
}
