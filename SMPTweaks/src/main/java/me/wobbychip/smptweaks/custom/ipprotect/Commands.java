package me.wobbychip.smptweaks.custom.ipprotect;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.tweaks.TweakCommands;
import me.wobbychip.smptweaks.utils.Utils;

public class Commands extends TweakCommands {
	public Commands(CustomTweak tweak, String command) {
		super(tweak, command, Arrays.asList("enable", "disable"));
	}

	@Override
	public boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.ipprotect")) {
			Utils.sendMessage(sender, me.wobbychip.smptweaks.commands.Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length < 2) {
			Utils.sendMessage(sender, this.getUsage() + " [" + String.join(" | ", this.getArguments()) + "] <username>");
			return true;
		}

		if (args[0].toLowerCase().equalsIgnoreCase("enable")) {
			Player player = Bukkit.getPlayer(args[1]);
			if (player == null) { Utils.sendMessage(sender, args[1] + " is not online."); return true; }
			this.getTweak().getConfig(0).getConfig().set("players." + args[1].toLowerCase(), player.getAddress().getHostName());
			this.getTweak().getConfig(0).Save();
			Utils.sendMessage(sender, args[1] + " is now ip protected (" + player.getAddress().getHostName() + ").");
		}

		if (args[0].toLowerCase().equalsIgnoreCase("disable")) {
			this.getTweak().getConfig(0).getConfig().set("players." + args[1].toLowerCase(), null);
			this.getTweak().getConfig(0).Save();
			Utils.sendMessage(sender, args[1] + " is now not ip protected.");
		}

		return true;
	}

	@Override
	public List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.ipprotect")) { return null; }
		if (args.length == 1) { return this.getArguments(); }
		if (args.length == 2) { return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()); }
		return null;
	}
}
