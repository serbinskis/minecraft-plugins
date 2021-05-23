package me.wobbychip.workbenchinventory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class Utilities {
	public static void DebugInfo(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static byte[] inventoryToByteArray(Inventory inventory) throws IllegalStateException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			for (int i = 0; i < inventory.getSize(); i++) {
				dataOutput.writeObject(inventory.getItem(i));
			}

			dataOutput.close();
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
    }

	public static void inventoryFromByteArray(Inventory inv, byte[] data) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

			for (int i = 0; i < inv.getSize(); i++) {
				inv.setItem(i, (ItemStack) dataInput.readObject());
			}

			dataInput.close();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public static void writeFile(File file, byte[] content) {
		if (!file.exists()) {
        	file.getParentFile().mkdirs();
        	try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

		try {
			Files.write(Paths.get(file.getAbsolutePath()), content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] readFile(File file) {
		if (file.exists()) {
			try {
				return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new byte[0];
	}
}
