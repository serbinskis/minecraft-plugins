package me.wobbychip.smptweaks.tweaks;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import me.wobbychip.smptweaks.Main;

public class TweakCommands implements CommandExecutor, TabCompleter {
	private String command;
	private List<String> arguments;
	private String usage;

	public TweakCommands(String command, List<String> arguments) {
		this.command = command;
		this.arguments = arguments;
		this.usage = Main.color + "Usage /smptweaks execute " + command;
		if (arguments != null) { this.usage += " [" + String.join(" | ", arguments) + "]"; }
	}

	public String getCommand() {
		return command;
	}

	public String getUsage() {
		return usage;
	}

	public List<String> getArguments() {
		return arguments;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}
