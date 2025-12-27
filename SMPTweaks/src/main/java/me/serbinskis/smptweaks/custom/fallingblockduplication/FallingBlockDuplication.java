package me.serbinskis.smptweaks.custom.fallingblockduplication;

import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class FallingBlockDuplication extends CustomTweak {
	public static CustomTweak tweak;

	public FallingBlockDuplication() {
		super(FallingBlockDuplication.class, true, false);
		this.setDescription("Enable falling block duplication glitch with end portal on PaperMC servers.");
		this.setGameRule("falling_block_duplication", true, false);
		FallingBlockDuplication.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
