package me.serbinskis.smptweaks.custom.serverpause;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.tweaks.TweakCommands;
import me.serbinskis.smptweaks.utils.ServerUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class Commands extends TweakCommands {
	public Commands(CustomTweak tweak, String command) {
		super(tweak, command, Arrays.asList("enable", "disable"));
	}

	@Override
	public boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.serverpause")) {
			Utils.sendMessage(sender, Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length == 0) {
			Utils.sendMessage(sender, this.getUsage() + " [" + String.join(" | ", this.getArguments()) + "]");
			return true;
		}

		if (args[0].equalsIgnoreCase("enable")) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				ServerPause.enabled = true;
				Utils.sendMessage(sender, "Server pause is now enabled.");
				if (ServerUtils.isPaused()) { Utils.sendMessage(sender, "Server is already paused."); }
				if (!ServerPause.canPause(false)) { Utils.sendMessage(sender, "Cannot pause server."); }
				boolean success = ServerUtils.pauseServer();
				if (success) { Utils.sendMessage(sender, "Server is now paused."); }
			}, 1L);
		}

		if (args[0].equalsIgnoreCase("disable")) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				ServerPause.enabled = false;
				Utils.sendMessage(sender, "Server pause is now disabled.");
				if (!ServerUtils.isPaused()) { Utils.sendMessage(sender, "Server is already resumed."); }
				boolean success = ServerUtils.resumeServer();
				if (success) { Utils.sendMessage(sender, "Server is now resumed."); }
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
