package me.wobbychip.chunkloader.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.chunkloader.ChunkLoader;
import me.wobbychip.chunkloader.ChunkManager;
import me.wobbychip.chunkloader.GUI;
import me.wobbychip.chunkloader.Main;
import me.wobbychip.chunkloader.ParticleManager;
import me.wobbychip.chunkloader.Utilities;

public class InventoryEvents implements Listener {
	@EventHandler(priority=EventPriority.NORMAL)
	public void onCraftItem(CraftItemEvent event) {
		if (!event.getRecipe().getResult().getItemMeta().getLocalizedName().equals("chunk_loader")) { return; }
		Player player = (Player) event.getWhoClicked();

		if (!player.hasPermission("chunkloader.use")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.plugin.getConfig().getString("permissionMessage")));
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.NORMAL)
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getClickedInventory();
		if (inv == null) { return; }
		if (!(inv.getHolder() instanceof GUI)) { return; }
		event.setCancelled(true);
		
		//Check nether star place or take
		if (event.getSlot() == 4) {
			ItemStack item = inv.getItem(4);
			
			//Place nether star
			if (item.getType() == Material.ITEM_FRAME) {
				ItemStack cursor = event.getCursor();
				if (cursor.getType() != Material.NETHER_STAR) { return; }

				GUI gui = (GUI) inv.getHolder();
				gui.setActivated(true);
				inv.setItem(4, Utilities.ActivatedCoreItem(gui.getArea()));
				cursor.setAmount(cursor.getAmount()-1);

				if (Main.particleManagers.containsKey(gui.getManagerName())) {
					item = Utilities.ParticleItem1_0(); //Activate grayed out
					inv.setItem(7, item);

					item = Utilities.ParticleItem0_1(); //Deactivate button
					inv.setItem(8, item);
				} else {
					item = Utilities.ParticleItem1_1(); //Activate button
					inv.setItem(7, item);

					item = Utilities.ParticleItem0_0(); //Deactivate grayed out
					inv.setItem(8, item);	
				}

				return;
			}

			//Take nether star
			if (item.getType() == Material.NETHER_STAR) {
				ItemStack cursor = event.getCursor();
				if (cursor.getType() != Material.AIR) { return; }

				GUI gui = (GUI) inv.getHolder();
				gui.setActivated(false);
				inv.setItem(4, Utilities.DeactivatedCoreItem());
				event.setCursor(new ItemStack(Material.NETHER_STAR));

				item = Utilities.ParticleItem1_0(); //Activate grayed out
				inv.setItem(7, item);

				item = Utilities.ParticleItem0_0(); //Deactivate grayed out
				inv.setItem(8, item);
				return;
			}
		}

		//Decrease area
		if (event.getSlot() == 3) {
			ItemStack item = inv.getItem(4);
			if (item.getType() == Material.ITEM_FRAME) { return; }
			int minimumArea = Main.plugin.getConfig().getInt("minimumArea");

			GUI gui = (GUI) inv.getHolder();
			int newArea = gui.getArea() - Main.plugin.getConfig().getInt("arrowIncrement");
			if (newArea <= minimumArea) { newArea = minimumArea; }
			gui.setArea(newArea);
			inv.setItem(4, Utilities.ActivatedCoreItem(gui.getArea()));
		}

		//Increase area
		if (event.getSlot() == 5) {
			ItemStack item = inv.getItem(4);
			if (item.getType() == Material.ITEM_FRAME) { return; }
			int maximumArea = Main.plugin.getConfig().getInt("maximumArea");

			GUI gui = (GUI) inv.getHolder();
			int newArea = gui.getArea() + Main.plugin.getConfig().getInt("arrowIncrement");
			if (newArea >= maximumArea) { newArea = maximumArea; }
			gui.setArea(newArea);
			inv.setItem(4, Utilities.ActivatedCoreItem(gui.getArea()));
		}

		//Activate border
		if (event.getSlot() == 7) {
			ItemStack item = inv.getItem(7);
			if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) { return; }

			item = Utilities.ParticleItem1_0(); //Activate grayed out
			inv.setItem(7, item);

			item = Utilities.ParticleItem0_1(); //Deactivate button
			inv.setItem(8, item);
		}

		//Deactivate border
		if (event.getSlot() == 8) {
			ItemStack item = inv.getItem(8);
			if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) { return; }

			item = Utilities.ParticleItem0_0(); //Deactivate grayed out
			inv.setItem(8, item);

			item = Utilities.ParticleItem1_1(); //Activate button
			inv.setItem(7, item);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		if (inv == null) { return; }
		if (!(inv.getHolder() instanceof GUI)) { return; }

		GUI gui = (GUI) inv.getHolder();
		ChunkLoader chunkLoader = new ChunkLoader(gui.getChunkLoader());
		ParticleManager particleManager = new ParticleManager(((Player) event.getPlayer()), chunkLoader.getLocation(), gui.getArea());

		if (chunkLoader.isRemoved()) {
			chunkLoader.setActivated(gui.getActivated(), false);
			chunkLoader.Save();
		} else {
			ChunkManager chunkManager = new ChunkManager(Utilities.StringToLocation(gui.getChunkLoader()));
			chunkManager.SetArea(chunkLoader.getArea(), gui.getArea(), chunkLoader.getActivated(), gui.getActivated());
			
			chunkLoader.setActivated(gui.getActivated(), false);
			chunkLoader.setArea(gui.getArea(), false);
			chunkLoader.Save();

			if (inv.getItem(8).getType() == Material.RED_STAINED_GLASS_PANE) {
				particleManager.Start();
			} else {
				particleManager.Stop();
			}
		}
	}
}
