package me.serbinskis.smptweaks.custom.noarrowinfinity;

import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Events implements Listener {
	/*@EventHandler(priority = EventPriority.LOWEST)
	public void onPacketEvent(PacketEvent event) {
		// When in creative mode client only sends 2 packets: USE_ITEM_ON (MAINHAND) -> USE_ITEM
		// But if in survival we receive 3 packets: USE_ITEM_ON (MAINHAND) -> USE_ITEM -> USE_ITEM_ON (OFFHAND)
		// The third packet breaks the visuals making it use offhand item while also drawing bow
		// The only way I found right now is to just cancel the third packet and do manual sync

		// We only are interested in preventing third packet for offhand interaction
		if (!List.of(PacketType.USE_ITEM_ON, PacketType.USE_ITEM).contains(event.getPacketType())) { return; }

		// Check if player has infinity bow in main hand and also has no arrows
		@NotNull ItemStack mainHandItem = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);
		boolean hasInfinityMainHand = NoArrowInfinity.isInfinityBow(mainHandItem) && !NoArrowInfinity.hasArrow(event.getPlayer());

		// In that case we check if this packet is for offhand
		boolean isOffhand = EquipmentSlot.OFF_HAND.equals(ReflectionUtils.getUseItemOnEventHand(event.getPacket()));

		// If so this packet the third offhand packet
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("Packet: " + event.getPacketType() + " | offhand: " + isOffhand + " | hasInfinityMainHand: " + hasInfinityMainHand); }
		if (hasInfinityMainHand && isOffhand) { event.setCancelled(true); }

		// This fixes shield and swing bug, and also prediction ghost blocks
		if (NoArrowInfinity.DEBUG && event.isCancelled()) { Utils.sendMessage("PacketEvent -> setCancelled(true)"); }
		ReflectionUtils.syncPlayer(event.getPlayer(), true, event.getPacket());
	}*/

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		// NOTE: There is still no way to prevent ghost swings, even with fully blocked OFF_HAND it still swings it

		Player player = event.getPlayer();
		if ((player.getGameMode() == GameMode.CREATIVE) || (event.getHand() == null)) { return; }

		// Ignore left click, we don't care about block breaking
		if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) { return; }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEvent -> " + event.getAction() + " | " + event.getHand()); }

		@NotNull ItemStack mainHandItem = player.getInventory().getItem(EquipmentSlot.HAND);
		boolean hasInfinityMainHand = NoArrowInfinity.isInfinityBow(mainHandItem) && !NoArrowInfinity.hasArrow(player);
		ItemStack eventHandItem = player.getInventory().getItem(event.getHand());
		boolean hasInfinityEventHand = NoArrowInfinity.isInfinityBow(eventHandItem) && !NoArrowInfinity.hasArrow(player);

		// In case if we have bow with infinity in main hand, but trying to use offhand, prevent event
		if (hasInfinityMainHand && EquipmentSlot.OFF_HAND.equals(event.getHand())) {
			if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEvent -> catch third packet"); }
			event.getPlayer().startUsingItem(EquipmentSlot.HAND);
			event.setCancelled(true); // This also sets both block and hand interaction to deny
			return;
		}

		// If we are not trying to use infinity bow in this event, just return
		if (!hasInfinityEventHand) { return; }

		// If we are trying to use bow then start using it
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEvent -> startUsingItem()"); }

		// Sync visual to prevent client from visually using offhand instead of main hand (IDK HOW THIS WORKS)
		ReflectionUtils.syncPlayer(event.getPlayer(), false, null);

		// Start using item, aka the bow
		event.getPlayer().startUsingItem(event.getHand());

		// NOTE: Since we are manually starting using item, the block interactions like right
		// NOTE: clicking bell will not prevent bow from drawing like in standard creative mode

		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("isCancelled: " + event.isCancelled()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useItemInHand: " + event.useItemInHand()); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("useInteractedBlock: " + event.useInteractedBlock()); }
	}

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		// Check if we are currently using infinity bow
		if (!NoArrowInfinity.isInfinityBow(event.getPlayer().getItemInUse())) { return; }

		// Stop using infinity bow when opening inventory
		event.getPlayer().clearActiveItem();
	}

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		// First check if we are trying to use offhand
		if (!EquipmentSlot.OFF_HAND.equals(event.getHand())) { return; }

		// Then check if we are currently using infinity bow
		if (!NoArrowInfinity.isInfinityBow(event.getPlayer().getItemInUse())) { return; }

		// And if we are using infinity bow then using offhand on entities is not allowed
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerInteractEntityEvent -> setCancelled(true)"); }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerStopUsingItemEvent(PlayerStopUsingItemEvent event) {
		Player player = event.getPlayer();

		// We don't care if player is already in creative
		if (player.getGameMode() == GameMode.CREATIVE) { return; }

		// If player stopped using infinity bow continue, means we about to try to shoot an arrow
		if (!NoArrowInfinity.isInfinityBow(event.getItem())) { return; }

		// Also check if player has arrow, if he does then we don't actually care to do workaround
		if (NoArrowInfinity.hasArrow(player)) { return; }

		// We need to set instabuild true for server to allow to use bow without any arrows inside the inventory
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("PlayerStopUsingItemEvent -> setInstantBuild"); }
		if (NoArrowInfinity.DEBUG) { Utils.sendMessage("======================================="); }

		// Set instabuild and revert this change in the next tick or inside EntityShootBowEvent
		NoArrowInfinity.doInstantBuild(event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		// Check if player is shooting a bow without any consumable item stack
		if (!(event.getEntity() instanceof Player player)) { return; }
		if (!(event.getProjectile() instanceof Arrow) || (event.getConsumable() == null)) { return; }

		// Fix infinite bow durability & tipped arrow bug, aka, just remove instabuild after shooting bow
		if (player.getGameMode() != GameMode.CREATIVE) { NoArrowInfinity.doInstantBuild(player, false); }
	}
}
