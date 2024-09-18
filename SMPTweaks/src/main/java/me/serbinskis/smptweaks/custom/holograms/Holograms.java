package me.serbinskis.smptweaks.custom.holograms;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;

public class Holograms extends CustomTweak {
	public Holograms() {
		super(Holograms.class, false, false);
		this.setDescription("Admin tool to make holograms with display text entity. " +
							"Crouch and right click a block with a book to create and hit to remove. " +
							"Stick - Right click: toggle following | Left click:  toggle see through. " +
							"Compass - Left click: cycle rotation. Blaze Rod - Left click: cycle alignment. " +
							"Special book - Right click block: teleport hologram." +
							"Permissions: smptweaks.holograms.*");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
