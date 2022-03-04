package me.wobbychip.pvpdropinventory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class Utils {
	public static void sendMessage(String arg0) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', arg0));
	}

    public static int getExpAtLevel(int level) {
        if (level <= 16) {
            return (int) (Math.pow(level,2) + 6*level);
        } else if (level <= 31) {
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }

    public static int getPlayerExp(Player player) {
        int level = player.getLevel();
        int exp = getExpAtLevel(level);
        exp += Math.round(player.getExpToLevel() * player.getExp());
        return exp;
    }

	public static int getExperienceReward(Player player, boolean dropAllXp) {
        if (player.getGameMode() != GameMode.SPECTATOR) {
        	if (dropAllXp) {
        		return getPlayerExp(player);
        	} else {
                int i = player.getLevel() * 7;
                return i > 100 ? 100 : i;
        	}
        }

		return 0;
    }
}
