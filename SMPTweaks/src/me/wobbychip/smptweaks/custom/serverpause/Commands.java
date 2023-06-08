package me.wobbychip.smptweaks.custom.serverpause;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.tweaks.TweakCommands;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Commands extends TweakCommands {
	public Commands(String command) {
		super(command, Arrays.asList("pause", "resume"));
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.serverpause")) {
			Utils.sendMessage(sender, me.wobbychip.smptweaks.commands.Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length == 0) {
			Utils.sendMessage(sender, this.getUsage());
			return true;
		}

		if (args[0].toLowerCase().equalsIgnoreCase("pause")) {
			if (ServerUtils.isPaused()) { Utils.sendMessage(sender, "Server is already paused."); return true; }
			if (!ServerPause.canPause(false)) { Utils.sendMessage(sender, "Cannot pause server."); return true; }
			boolean success = ServerUtils.pauseServer();
			if (success) { Utils.sendMessage(sender, "Server is now paused."); }
		}

		if (args[0].toLowerCase().equalsIgnoreCase("resume")) {
			if (!ServerUtils.isPaused()) { Utils.sendMessage(sender, "Server is already resumed."); return true; }
			boolean success = ServerUtils.resumeServer();
			if (success) { Utils.sendMessage(sender, "Server is now resumed."); }
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.serverpause")) { return null; }
		if (args.length == 1) { return this.getArguments(); }
		return null;
	}
}
