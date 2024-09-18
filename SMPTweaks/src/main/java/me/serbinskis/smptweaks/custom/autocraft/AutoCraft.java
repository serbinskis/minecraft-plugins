package me.serbinskis.smptweaks.custom.autocraft;

import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class AutoCraft extends CustomTweak {
	public static CustomTweak tweak;

	public AutoCraft() {
		super(AutoCraft.class, false, false);
		this.setGameRule("doAutoCraft", true, false);
		this.setDescription("Craft auto crafter with recipe specific in crafting table " +
							"Put a recipe inside the dispenser. " +
							"Input any container behind the dispenser, output in front.");
		AutoCraft.tweak = this;
	}

	public void onEnable() {
		CustomBlocks.registerBlock(new CrafterBlock());
	}
}
