package me.wobbychip.chunkloader;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUI implements InventoryHolder {
	private Inventory inv;
	private String locationString;
	private String managerName;
	private String owner;
	private boolean activated;
	private int area;

	public GUI(Player player, String chunkloader) {
		locationString = chunkloader;

		ChunkLoader chunkLoader = new ChunkLoader(locationString);
		activated = chunkLoader.getActivated();
		area = chunkLoader.getArea();
		owner = Bukkit.getOfflinePlayer(chunkLoader.getOwner()).getName();
		managerName = player.getUniqueId().toString() + Main.Delimiter + locationString;

		inv = Bukkit.createInventory(this, 9, "§aChunk Loader - " + owner);
		initInventory();
	}

	private void initInventory() {
		ItemStack item;

		//Center
		if (activated) {
			item = Utilities.ActivatedCoreItem(area);
			inv.setItem(4, item);

			if (Main.particleManagers.containsKey(managerName)) {
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
		} else {
			item = Utilities.DeactivatedCoreItem();
			inv.setItem(4, item);

			item = Utilities.ParticleItem1_0(); //Activate grayed out
			inv.setItem(7, item);

			item = Utilities.ParticleItem0_0(); //Deactivate grayed out
			inv.setItem(8, item);
		}

		item = CreateItem("§f§lIncrease area", 1, Material.BLACK_STAINED_GLASS_PANE);
		inv.setItem(3, item);

		item = CreateItem("§f§lDecrease area", 1, Material.BLACK_STAINED_GLASS_PANE);
		inv.setItem(5, item);

		item = CreateItem(" ", 1, Material.WHITE_STAINED_GLASS_PANE);

		//Left
		for (int i = 0; i < 3; i++) {
			inv.setItem(i, item);
		}

		//Right
		for (int i = 6; i < 7; i++) {
			inv.setItem(i, item);
		}
	}

	private ItemStack CreateItem(String name, int amount, Material material) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int Area) {
		area = Area;
	}

	public boolean getActivated() {
		return activated;
	}

	public void setActivated(boolean Activated) {
		activated = Activated;
	}

	public String getChunkLoader() {
		return locationString;
	}

	public String getManagerName() {
		return managerName;
	}
}
