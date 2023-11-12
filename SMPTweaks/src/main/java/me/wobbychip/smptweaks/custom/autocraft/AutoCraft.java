package me.wobbychip.smptweaks.custom.autocraft;

import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.TaskUtils;

import java.util.List;

public class AutoCraft extends CustomTweak {
	public static CustomTweak tweak;
	public static int task = -1;
	public static int craftCooldown = 8;
	public static String redstoneMode = "indirect";
	public static boolean allowBlockRecipeModification = true;
	public static Crafters crafters;

	public AutoCraft() {
		super(AutoCraft.class, false, false);
		AutoCraft.tweak = this;
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doAutoCraft", true, false);
		this.setReloadable(true);
		this.setDescription("Just search on internet about crafter in 1.21");
	}

	public void onEnable() {
		this.onReload();
		CustomBlocks.registerBlock(new CrafterBlock());
	}

	public void onReload() {
		AutoCraft.craftCooldown = this.getConfig(0).getConfig().getInt("craftCooldown");
		AutoCraft.redstoneMode = this.getConfig(0).getConfig().getString("redstoneMode");
		AutoCraft.allowBlockRecipeModification = this.getConfig(0).getConfig().getBoolean("allowBlockRecipeModification");
		AutoCraft.task = TaskUtils.rescheduleSyncRepeatingTask(AutoCraft.task, 0L, AutoCraft.craftCooldown);
	}
}
