package me.wobbychip.autocraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class Utilities {
	public static void DebugInfo(String message) {
		if (Main.plugin.getConfig().getBoolean("debug")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	@SuppressWarnings("deprecation")
	public static String LocationToString(Location location) {
		return location.getWorld().getName() + Main.Delimiter + new Integer((int)(location.getX())).toString() + Main.Delimiter + new Integer((int)(location.getY())).toString() + Main.Delimiter +  new Integer((int)(location.getZ())).toString();
	}

	public static Location StringToLocation(String locationString) {
		String[] splited = locationString.split(Main.Delimiter, 0);
		World world = Main.plugin.getServer().getWorld(splited[0]);
		Location location = new Location(world, Double.parseDouble(splited[1]), Double.parseDouble(splited[2]), Double.parseDouble(splited[3]));
		return location;
	}

	public static InventoryManager getInventoryManager(Location location) {
		String locationString = LocationToString(location);

		for (String key : Main.inventoryManagers.keySet()) {
		    if (key.contains(locationString)) {
		    	return Main.inventoryManagers.get(key);
		    }
		}

		return new InventoryManager(locationString);
	}

	public static byte[] inventoryToByteArray(Inventory inventory) throws IllegalStateException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			// Save every element in the list
			for (int i = 0; i < inventory.getSize(); i++) {
				dataOutput.writeObject(inventory.getItem(i));
			}
            
			// Serialize that array
			dataOutput.close();
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
    }

	public static void inventoryFromByteArray(Inventory inv, byte[] data) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

			// Read the serialized inventory
			for (int i = 0; i < inv.getSize(); i++) {
				inv.setItem(i, (ItemStack) dataInput.readObject());
			}

			dataInput.close();
		} catch (ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}

	public static void writeFile(File file, byte[] content) {
		try {
			Files.write(Paths.get(file.getAbsolutePath()), content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] readFile(File file) {
		try {
			return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new byte[0];
	}
}
