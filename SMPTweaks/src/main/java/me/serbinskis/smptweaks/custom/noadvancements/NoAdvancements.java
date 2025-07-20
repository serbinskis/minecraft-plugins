package me.serbinskis.smptweaks.custom.noadvancements;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;

public class NoAdvancements extends CustomTweak {
	public static CustomTweak tweak;

	public NoAdvancements() {
		super(NoAdvancements.class, false, false);
		this.setGameRule("doAdvancements", true, false);
		this.setDescription("Disable advancements with custom gamerule.");
		NoAdvancements.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}

	public void onDisable() {
		Events.scheduled.removeIf(item -> { TaskUtils.finishTask(item); return true; });
	}
}
