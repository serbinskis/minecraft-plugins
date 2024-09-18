package me.serbinskis.smptweaks.tweaks;

import me.serbinskis.smptweaks.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TweakCommands {
	public static String NO_CONSOLE = Main.MESSAGE_COLOR + "This command can only be executed by a player!";
	public static String NO_PERMISSIONS = "&cYou do not have permissions!";
	private final CustomTweak tweak;
	private final String command;
	private final List<String> arguments;
	private final String usage;

	public TweakCommands(CustomTweak tweak, String command, List<String> arguments) {
		this.tweak = tweak;
		this.command = command;
		this.arguments = arguments;
		this.usage = Main.MESSAGE_COLOR + "Usage /smptweaks execute " + command;
	}

	public CustomTweak getTweak() {
		return tweak;
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

	public boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		return true;
	}

	public List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}
