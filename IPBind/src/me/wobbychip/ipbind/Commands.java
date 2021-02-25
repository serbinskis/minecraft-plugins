package me.wobbychip.ipbind;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    	if (!Utilities.CheckPermissions(sender, "ipbind.use")) { return true; }

    	//Bind IP
		if (command.getName().equalsIgnoreCase("ipbind")) {
			if (args.length == 0) {
				Utilities.IPBind(sender, ((Player) sender).getName());
			} else {
				if (!Utilities.CheckPermissions(sender, "ipbind.others")) { return true; }
				Utilities.IPBind(sender, args[0]);
			}
			
			return true;
		}

		//Unbind IP
		if (command.getName().equalsIgnoreCase("ipunbind")) {
			if (args.length == 0) {
				Utilities.IPUnbind(sender, ((Player) sender).getName());
			} else {
				if (!Utilities.CheckPermissions(sender, "ipbind.others")) { return true; }
				Utilities.IPUnbind(sender, args[0]);
			}
			
			return true;
		}

        return true;
    }
}
