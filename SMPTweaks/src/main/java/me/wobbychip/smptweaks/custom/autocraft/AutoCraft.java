package me.wobbychip.smptweaks.custom.autocraft;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;

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
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doAutoCraft", true, false);
		this.setReloadable(true);
		this.setDescription("Put on a dispenser an item frame with a crafting table. " +
							"Put a recipe inside the dispenser. " +
							"Input any container behind the dispenser, output in front.");
		AutoCraft.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		AutoCraft.crafters = new Crafters();

		AutoCraft.task = TaskUtils.scheduleSyncRepeatingTask(() -> {
            if (!ServerUtils.isPaused()) { crafters.handleCrafters(); }
        }, 0L, AutoCraft.craftCooldown);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		AutoCraft.craftCooldown = this.getConfig(0).getConfig().getInt("craftCooldown");
		AutoCraft.redstoneMode = this.getConfig(0).getConfig().getString("redstoneMode");
		AutoCraft.allowBlockRecipeModification = this.getConfig(0).getConfig().getBoolean("allowBlockRecipeModification");
		AutoCraft.task = TaskUtils.rescheduleSyncRepeatingTask(AutoCraft.task, 0L, AutoCraft.craftCooldown);
	}
}
