package me.wobbychip.smptweaks.custom.serverpause;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.tweaks.TweakCommands;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Commands extends TweakCommands {
	public Commands(CustomTweak tweak, String command) {
		super(tweak, command, Arrays.asList("enable", "disable"));
	}

	@Override
	public boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.serverpause")) {
			Utils.sendMessage(sender, me.wobbychip.smptweaks.commands.Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length == 0) {
			Utils.sendMessage(sender, this.getUsage() + " [" + String.join(" | ", this.getArguments()) + "]");
			return true;
		}

		if (args[0].toLowerCase().equalsIgnoreCase("enable")) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				public void run() {
					ServerPause.enabled = true;
					Utils.sendMessage(sender, "Server pause is now enabled.");
					if (ServerUtils.isPaused()) { Utils.sendMessage(sender, "Server is already paused."); }
					if (!ServerPause.canPause(false)) { Utils.sendMessage(sender, "Cannot pause server."); }
					boolean success = ServerUtils.pauseServer();
					if (success) { Utils.sendMessage(sender, "Server is now paused."); }
				}
			}, 1L);
		}

		if (args[0].toLowerCase().equalsIgnoreCase("disable")) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				public void run() {
					ServerPause.enabled = false;
					Utils.sendMessage(sender, "Server pause is now disabled.");
					if (!ServerUtils.isPaused()) { Utils.sendMessage(sender, "Server is already resumed."); }
					boolean success = ServerUtils.resumeServer();
					if (success) { Utils.sendMessage(sender, "Server is now resumed."); }
				}
			}, 1L);
		}

		return true;
	}

	@Override
	public List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.serverpause")) { return null; }
		if (args.length == 1) { return this.getArguments(); }
		return null;
	}
}
