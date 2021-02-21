package me.wobbychip.chunkloader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChunkLoader {
	private Location loactionLoader;
	private String locationString;
	private UUID owner;
	private boolean activated;
	private int area;

	public ChunkLoader(String chunkloader) {
		locationString = chunkloader;
		loactionLoader = Utilities.StringToLocation(chunkloader);
		Load();
	}

	public ChunkLoader(Location location) {
		locationString = Utilities.LocationToString(location);
		loactionLoader = location;
		Load();
	}

	public ChunkLoader(Location location, Player player, boolean doSave) {
		locationString = Utilities.LocationToString(location);
		loactionLoader = location;
		owner = player.getUniqueId();
		activated = false;
		area = 1;

		Create();
		if (doSave) { Save(); }
	}

	public void Save() {
		Main.LoadersConfig.getConfig().set("chunkloaders." + locationString + ".owner", owner.toString());
		Main.LoadersConfig.getConfig().set("chunkloaders." + locationString + ".activated", activated);
		Main.LoadersConfig.getConfig().set("chunkloaders." + locationString + ".area", area);
		Main.LoadersConfig.Save();
	}

	public boolean Exists() {
		return Main.LoadersConfig.getConfig().contains("chunkloaders." + locationString);
	}

	public boolean isRemoved() {
		return Main.LoadersConfig.getConfig().contains("chunkloaders." + locationString + ".removed");
	}

	private void Load() {
		if (!Exists()) { return; }

		owner = UUID.fromString(Main.LoadersConfig.getConfig().getString("chunkloaders." + locationString + ".owner"));
		activated = Main.LoadersConfig.getConfig().getBoolean("chunkloaders." + locationString + ".activated");
		area = Main.LoadersConfig.getConfig().getInt("chunkloaders." + locationString + ".area");
	}

	private void Create() {
		Utilities.CreateOutline(loactionLoader, "§aChunk Loader");
	}

	public void Remove(boolean DropItems) {
		Main.LoadersConfig.getConfig().set("chunkloaders." + locationString + ".removed", true);
		Main.LoadersConfig.Save();

		ChunkManager chunkManager = new ChunkManager(loactionLoader);
		chunkManager.SetArea(area, 0, activated, activated);
		
		CloseInvenotry(); //After close inventory some settings may be updated
		Load(); //So reload settings

		Utilities.RemoveOutline(loactionLoader);
		loactionLoader.getBlock().setType(Material.AIR);
		
		if (DropItems) {
			loactionLoader.getWorld().dropItem(loactionLoader, Utilities.ChunkLoaderItem());
		}

		if (DropItems && activated) {
			loactionLoader.getWorld().dropItem(loactionLoader, new ItemStack(Material.NETHER_STAR, 1));
		}

		Main.LoadersConfig.getConfig().set("chunkloaders." + locationString, null);
		Main.LoadersConfig.Save();

		List<String> removeManagers = new ArrayList<String>();

		for (String key : Main.particleManagers.keySet()) {
		    if (key.contains(locationString)) {
		    	removeManagers.add(key);
		    }
		}

		for (String key : removeManagers) {
			ParticleManager particleManager = Main.particleManagers.get(key);
			particleManager.Stop();
		}
	}

	public void OpenInvenotry(Player player) {
		GUI gui = new GUI(player, locationString);
		player.openInventory(gui.getInventory());
	}

	public void CloseInvenotry() {
        for (Player player : Bukkit.getOnlinePlayers()) {
        	Inventory inv = player.getOpenInventory().getTopInventory();
        	if (inv.getHolder() instanceof GUI) {
        		GUI gui = (GUI) inv.getHolder();
        		if (gui.getChunkLoader().equals(locationString)) {
        			player.closeInventory();
        			break;
        		}
        	}
        }
	}

	public boolean isOpened() {
        for (Player player : Bukkit.getOnlinePlayers()) {
        	Inventory inv = player.getOpenInventory().getTopInventory();
        	if (inv.getHolder() instanceof GUI) {
        		GUI gui = (GUI) inv.getHolder();
        		if (gui.getChunkLoader().equals(locationString)) {
        			return true;
        		}
        	}
        }

        return false;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int Area, boolean doSave) {
		area = Area;
		if (doSave) { Save(); }
	}

	public boolean getActivated() {
		return activated;
	}

	public void setActivated(boolean Activated, boolean doSave) {
		activated = Activated;
		if (doSave) { Save(); }
	}

	public UUID getOwner() {
		return owner;
	}

	public Location getLocation() {
		return Utilities.StringToLocation(locationString);
	}
}
