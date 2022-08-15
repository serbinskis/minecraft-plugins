package me.wobbychip.smptweaks.custom.funnymessages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class FunnyMessages extends CustomTweak {
	public static List<String> messages = new ArrayList<>();

	public FunnyMessages() {
		super(FunnyMessages.class.getSimpleName(), false, false);
	}

	public void onEnable() {
		loadConfig();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public static void loadConfig() {
		try {
			List<String> list = Arrays.asList(FunnyMessages.class.getCanonicalName().split("\\."));
			String configPath = String.join("/", list.subList(0, list.size()-1)) + "/messages.txt";
			File file = Utils.saveResource(configPath, "/tweaks/FunnyMessages/messages.txt");

			for (String line : Files.readString(file.toPath()).split("\\r?\\n")) {
				if (line.contains("<player>")) { messages.add(line); }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
