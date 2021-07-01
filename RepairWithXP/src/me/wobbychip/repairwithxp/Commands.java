package me.wobbychip.repairwithxp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    	//Return if no arguments
		if (args.length == 0) {
			Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			if (!Utilities.CheckPermissions(sender, "repairwithxp.reload")) { return true; }

			Main.plugin.reloadConfig();
			Main.StartPlugin();
			Utilities.SendMessage(sender, Utilities.getString("reloadMessage"));

			return true;
		}

		Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
        return true;
    }
}
