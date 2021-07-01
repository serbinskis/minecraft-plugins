package me.wobbychip.repairwithxp;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Utilities {
	//Get string from config
	static String getString(String arg0) {
		return Main.plugin.getConfig().getString(arg0);
	}

	//Send message to sender
	static void SendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	//Check if player has permissions
	static boolean CheckPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission(permission)) {
				SendMessage(sender, getString("permissionMessage"));
				return false;
			}
		}

		return true;
	}

    //Calculate total experience up to a level
    public static int getExpAtLevel(int level) {
        if (level <= 16) {
            return (int) (Math.pow(level,2) + 6*level);
        } else if (level <= 31) {
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }
 
    //Calculate players current EXP amount
    public static int getPlayerExp(Player player) {
        int level = player.getLevel();
        int exp = getExpAtLevel(level);
        exp += Math.round(player.getExpToLevel() * player.getExp());
        return exp;
    }

	//Check player
	@SuppressWarnings("deprecation")
	public static void checkPlayer(Player player) {
		//Check if player is sneaking
		if (!player.isSneaking()) {
			return;
		}

		//Check if player has enough exp
		if (getPlayerExp(player) < Main.perXP) {
			return;
		}

		//Get player off hand item
		ItemStack offHand = player.getInventory().getItemInOffHand();

		//Check if item is damaged
		if (offHand.getDurability() <= 0) {
			return;
		}

		//Check if item has mending
		if (offHand.getEnchantmentLevel(Enchantment.MENDING) <= 0) {
			return;
		}

		//Remove perXP amount from player
		player.giveExp(Main.perXP * -1);
		
		//Spawn XP orb with perXP amount
		ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
		orb.setExperience(Main.perXP);
	}
}
