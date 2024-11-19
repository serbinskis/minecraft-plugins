package me.serbinskis.smptweaks.library.resourcepack;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.resourcepack.commands.Commands;
import me.serbinskis.smptweaks.library.resourcepack.events.PlayerEvents;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ResourcePacks extends CustomTweak {
	public static CustomTweak tweak;
	public static final String RESOURCE_PACK_PROMPT = "This resource pack is required for server.";
	public static HashMap<String, byte[]> resourcePacks = new HashMap<>();

	public ResourcePacks() {
		super(ResourcePacks.class, false, false, true);
		this.setCommand(new Commands(this, "rpacks"));
		this.setDescription("Library for downloading custom resource packs");
		this.setConfigs(List.of("resourcepacks.yml"));
		this.setReloadable(true);
		ResourcePacks.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new PlayerEvents(), Main.plugin);
		this.onReload();
	}

	@Override
	public void onReload() {
		for (byte[] hash : resourcePacks.values()) { Bukkit.getOnlinePlayers().forEach(e -> e.removeResourcePack(UUID.nameUUIDFromBytes(hash))); }
		resourcePacks.clear();

		List<String> resourcePacks = this.getConfig(0).getConfig().getStringList("resourcepacks");
		for (String resourcePack : resourcePacks) { addResourcePack(resourcePack); }
	}

	public static void addResourcePack(String url) {
		if (resourcePacks.containsKey(url)) { return; }
		resourcePacks.put(url, Utils.getFileHash(url));
		ResourcePacks.tweak.getConfig(0).getConfig().set("resourcepacks", resourcePacks.keySet().stream().toList());
		ResourcePacks.tweak.getConfig(0).save();

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.addResourcePack(UUID.nameUUIDFromBytes(resourcePacks.get(url)), url, resourcePacks.get(url), RESOURCE_PACK_PROMPT, true);
		}
	}

	public static void removeResourcePack(String url) {
		if (!resourcePacks.containsKey(url)) { return; }
		byte[] hash = resourcePacks.remove(url);
		ResourcePacks.tweak.getConfig(0).getConfig().set("resourcepacks", resourcePacks.keySet().stream().toList());
		ResourcePacks.tweak.getConfig(0).save();

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.removeResourcePack(UUID.nameUUIDFromBytes(hash));
		}
	}
}
