package me.wobbychip.chunkloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Utilities {
	public static double Distance(double x1, double z1, double x2, double z2) {
		return Math.sqrt(Math.pow((x2-x1), 2) + Math.pow((z2-z1), 2));
	}

	public static void DebugInfo(String message) {
		if (Main.plugin.getConfig().getBoolean("debug")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	public static ItemStack ChunkLoaderItem() {
		ItemStack item = new ItemStack(Material.LODESTONE);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName("§aChunk Loader");
		meta.setLocalizedName("chunk_loader");
		meta.addEnchant(Enchantment.DURABILITY, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);

		return item;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack ActivatedCoreItem(int Area) {
		ItemStack item = new ItemStack(Material.NETHER_STAR, Area);
		ItemMeta meta = item.getItemMeta();

		List<String> loreList = new ArrayList<String>();
		loreList.add("Area: " + new Integer(Area).toString());
		loreList.add("Chunks: " + new Integer((Area+(Area-1))*(Area+(Area-1))).toString());

		meta.setDisplayName("§a§lActivated");
		meta.setLore(loreList);
		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack DeactivatedCoreItem() {
		ItemStack item = new ItemStack(Material.ITEM_FRAME);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName("§c§lDeactivated");
		meta.setLore(Collections.singletonList("Please put nether star inside!"));
		item.setItemMeta(meta);

		return item;
	}

	//Deactivate button is grayed out
	public static ItemStack ParticleItem0_0() {
		ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName("§7§lDeactivate border (Disabled)");
		meta.setLore(Collections.singletonList("§7This will hide small border around selected chunks."));

		item.setItemMeta(meta);
		return item;
	}

	//Deactivate button is red
	public static ItemStack ParticleItem0_1() {
		ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName("§c§lDeactivate border");
		meta.setLore(Collections.singletonList("This will hide small border around selected chunks."));

		item.setItemMeta(meta);
		return item;
	}

	//Activate button is grayed out
	public static ItemStack ParticleItem1_0() {
		ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName("§7§lActivate border (Disabled)");
		meta.setLore(Collections.singletonList("§7This will show small border around selected chunks."));

		item.setItemMeta(meta);
		return item;
	}

	//Activate button is green
	public static ItemStack ParticleItem1_1() {
		ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName("§a§lActivate border");
		meta.setLore(Collections.singletonList("This will show small border around selected chunks."));

		item.setItemMeta(meta);
		return item;
	}

	@SuppressWarnings("deprecation")
	static String LocationToString(Location location) {
		return location.getWorld().getName() + Main.Delimiter + new Integer((int)(location.getX())).toString() + Main.Delimiter + new Integer((int)(location.getY())).toString() + Main.Delimiter +  new Integer((int)(location.getZ())).toString();
	}

	public static Location StringToLocation(String locationString) {
		String[] splited = locationString.split(Main.Delimiter, 0);
		World world = Main.plugin.getServer().getWorld(splited[0]);
		Location location = new Location(world, Double.parseDouble(splited[1]), Double.parseDouble(splited[2]), Double.parseDouble(splited[3]));
		return location;
	}

	@SuppressWarnings("deprecation")
	public static String CoordsToString(int X, int Z) {
		return new Integer((int)(X)).toString() + Main.Delimiter + new Integer((int)(Z)).toString();
	}

	//Set outline to block
	static void CreateOutline(Location location, String CustomName) {
		location.setX(location.getX()+0.5);
		location.setZ(location.getZ()+0.5);

		Shulker shulker = location.getWorld().spawn(location, Shulker.class);
		shulker.setInvisible(true);
		shulker.setSilent(true);
		shulker.setInvulnerable(true);
		shulker.setAI(false);
		shulker.setGlowing(true);
		shulker.setCustomNameVisible(true);
		shulker.setCustomName(CustomName);
		
	}

	//Remove outline from block
	static void RemoveOutline(Location location) {
		location.setX(location.getX()+0.5);
		location.setZ(location.getZ()+0.5);
    	Collection<Entity> nearbyEntites = location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5);

        for (Entity entity : nearbyEntites) {
        	if ((entity.getType() == EntityType.SHULKER) && entity.isInvulnerable()) {
            	entity.remove();
            }
        }
	}

	//Drop item from player position
	public void DropItem(Player player, ItemStack item) {
		Location location = player.getLocation();
		location.setY(location.getY()+1.3);

		Vector vector = player.getLocation().getDirection();
		vector.multiply(0.32);

		Item itemDropped = player.getWorld().dropItem(location, item);
		itemDropped.setVelocity(vector);
		itemDropped.setPickupDelay(40);
	}

	public static void ForceLoadChunk(String world, int X, int Z) {
        Bukkit.getServer().getWorld(world).loadChunk(X, Z);
        Bukkit.getServer().getWorld(world).setChunkForceLoaded(X, Z, true);
        DebugInfo("&9[ChunkLoader] Loading chunk (" + X*16 + "," + Z*16 + ") in world '" + world + "'");
	}

	public static void ForceUnloadChunk(String world, int X, int Z) {
        Bukkit.getServer().getWorld(world).setChunkForceLoaded(X, Z, false);
        DebugInfo("&9[ChunkLoader] Unloading chunk (" + X*16 + "," + Z*16 + ") in world '" + world + "'");
	}

	public static boolean WorldExists(String world) {
		File worldFile = new File(Bukkit.getWorldContainer().toString() + "/" + world + "/level.dat");
		return worldFile.exists();
	}

	public static boolean WorldLoaded(String world) {
		return (Bukkit.getWorld(world) != null);
	}

	public static boolean LoadWorld(String world) {
		if (WorldLoaded(world)) {
			DebugInfo("&9[ChunkLoader] World '" + world + "' is already loaded");
			return false;
		}

		new WorldCreator(world).createWorld();
		DebugInfo("&9[ChunkLoader] Loaded world '" + world + "'");
		return true;
	}

	//Load world from chunks config
	public static void LoadChunks() {
		for (String world : Main.ChunksConfig.getConfig().getConfigurationSection("chunks").getKeys(false)) {
			if (!WorldExists(world)) {
				Main.ChunksConfig.getConfig().set("chunks." + world, null);
			} else {
				for (String chunk : Main.ChunksConfig.getConfig().getConfigurationSection("chunks." + world).getKeys(false)) {
					int X = Integer.parseInt(chunk.split(Main.Delimiter, 0)[0]);
					int Z = Integer.parseInt(chunk.split(Main.Delimiter, 0)[1]);
					ForceLoadChunk(world, X, Z);
				}
			}
		}

		Main.LoadersConfig.Save();
	}

	//Check for chunk loader existence
	public static void CheckChunkLoaders() {
		for (String section : Main.LoadersConfig.getConfig().getConfigurationSection("chunkloaders").getKeys(false)) {
			String[] splited = section.split(Main.Delimiter, 0);

			if (!WorldExists(splited[0])) {
				ChunkLoader chunkLoader = new ChunkLoader(section);
				ChunkManager chunkManager = new ChunkManager(section);
				chunkManager.SetArea(chunkLoader.getArea(), 0, chunkLoader.getActivated(), chunkLoader.getActivated());
				Main.LoadersConfig.getConfig().set("chunkloaders." + section, null);
				DebugInfo("&9[ChunkLoader] ChunkLoader in world `" + splited[0] + "` at (" + splited[1] + "," + splited[2] + "," + splited[3] + ") is missing and was removed!");
			} else {
				if (Utilities.StringToLocation(section).getBlock().getType() != Material.LODESTONE) {
					new ChunkLoader(section).Remove(false);
					DebugInfo("&9[ChunkLoader] ChunkLoader in world `" + splited[0] + "` at (" + splited[1] + "," + splited[2] + "," + splited[3] + ") is missing and was removed!");
				}
			}
		}

		Main.LoadersConfig.Save();
	}

	public static void LoadWorlds() {
		ArrayList<String> worlds = new ArrayList<String>();

		for (String section : Main.LoadersConfig.getConfig().getConfigurationSection("chunkloaders").getKeys(false)) {
			String world = section.split(Main.Delimiter, 0)[0];
			if (!worlds.contains(world)) { worlds.add(world); }
		}

	    for (String world : worlds) {
	        if (WorldExists(world)) {
	        	LoadWorld(world);
	        }
	    }
	}
}
