package me.wobbychip.smptweaks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	private FileConfiguration customConfig;
	private File file;

	public Config(String configPath, String savePath) {
		file = new File(Main.plugin.getDataFolder() + savePath);

		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				InputStream inputStream = Main.plugin.getResource(configPath);
				Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		customConfig = new YamlConfiguration();

		try {
			customConfig.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig() {
		return this.customConfig;
	}

	public void Save() {
		try {
			customConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
