package me.wobbychip.whitelist;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
    private FileConfiguration customConfig;
    private File file;

	public Config(String ConfigName) {
		file = new File(Main.plugin.getDataFolder(), ConfigName);

		if (!file.exists()) {
        	file.getParentFile().mkdirs();
        	Main.plugin.saveResource(ConfigName, false);
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
