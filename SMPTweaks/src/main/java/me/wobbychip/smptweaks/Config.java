package me.wobbychip.smptweaks;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Config {
	private final FileConfiguration customConfig;
	private final File file;

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
		if (configPath.endsWith(".txt")) { return; }

		try {
			customConfig.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig() {
		return this.customConfig;
	}

	public File getFile()  {
		return file;
	}

	public Path getPath()  {
		return file.toPath();
	}

	public String getContent() {
		try {
			return Files.readString(getPath());
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public void save() {
		try {
			customConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
