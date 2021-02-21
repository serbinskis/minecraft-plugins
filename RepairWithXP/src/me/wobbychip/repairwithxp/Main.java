package me.wobbychip.repairwithxp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	int TaskID;

	public Boolean pluginEnabled = false;
	public Integer perTicks = 5;
	public Integer perXP = 5;

	public String enableMessage = new String();
	public String permissionMessage = new String();
	public String incorrectArgument = new String();
	public String usageMessage = new String();
	
	public String reloadMessage = new String();
	public String onMessage = new String();
	public String alreadyOnMessage = new String();
	public String offMessage = new String();
	public String alreadyOffMessage = new String();

	public String setPerTicksMessage = new String();
	public String setPerXPMessage = new String();
	
	public void loadConfig() {
		pluginEnabled = this.getConfig().getBoolean("Enabled");
		perTicks = this.getConfig().getInt("perTicks");
		perXP = this.getConfig().getInt("perXP");

		enableMessage = this.getConfig().getString("enableMessage");
		permissionMessage = this.getConfig().getString("permissionMessage");
		incorrectArgument = this.getConfig().getString("incorrectArgument");
		usageMessage = this.getConfig().getString("usageMessage");
		
		reloadMessage = this.getConfig().getString("reloadMessage");
		onMessage = this.getConfig().getString("onMessage");
		alreadyOnMessage = this.getConfig().getString("alreadyOnMessage");
		offMessage = this.getConfig().getString("offMessage");
		alreadyOffMessage = this.getConfig().getString("alreadyOffMessage");

		setPerTicksMessage = this.getConfig().getString("setPerTicksMessage");
		setPerXPMessage = this.getConfig().getString("setPerXPMessage");
	}

//==============================================================================================================================================
//==============================================================================================================================================
//==============================================================================================================================================
	
	//Check if player has permissions
	public boolean CheckPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission(permission)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', permissionMessage));
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
	public void CheckPlayer(Player player) {
		//Check if player is sneaking
		if (!player.isSneaking()) {
			return;
		}

		//Check if player has enough exp
		if (getPlayerExp(player) < perXP) {
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
		player.giveExp(perXP * -1);
		
		//Spawn XP orb with perXP amount
		ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
		orb.setExperience(perXP);
	}

	//RepairWithXP commands list
	@SuppressWarnings("deprecation")
	public void RepairCommands(CommandSender sender,  String[] args) {
		//Check if args is empty
		if (args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMessage));
			return;
		}

		//Turn on RepairWithXP
		if (args[0].equalsIgnoreCase("on")) {
			if (!CheckPermissions(sender, "repairwithxp.toggle")) { return; }

			if (pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOnMessage));
				return;
			}

			pluginEnabled = true;
			StartPlugin();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onMessage));
			return;
		}

		//Turn off RepairWithXP
		if (args[0].equalsIgnoreCase("off")) {
			if (!CheckPermissions(sender, "repairwithxp.toggle")) { return; }

			if (!pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOffMessage));
				return;
			}

			pluginEnabled = false;
			StopPlugin();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', offMessage));
			return;
		}

		//Reload plugin command
		if (args[0].equalsIgnoreCase("reload")) {
			if (!CheckPermissions(sender, "repairwithxp.reload")) { return; }

			this.reloadConfig();
			loadConfig();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMessage));

			ReloadPlugin();
			return;
		}

		//Change tick speed command
		if (args[0].equalsIgnoreCase("perTicks")) {
			if (!CheckPermissions(sender, "repairwithxp.perTicks")) { return; }

			if ((args.length < 2) || !args[1].matches("-?\\d+") || (args[1].length() > 4)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', incorrectArgument));
				return;
			}

			perTicks = Integer.parseInt(args[1]);
			if (perTicks <= 0) { perTicks = 1; }

			String replacedMessage = setPerTicksMessage.replace("%value%", new Integer(perTicks).toString());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));

			ReloadPlugin();
			return;
		}

		//Change cost per every repair
		if (args[0].equalsIgnoreCase("perXP")) {
			if (!CheckPermissions(sender, "repairwithxp.perXP")) { return; }

			if ((args.length < 2) || !args[1].matches("-?\\d+") || (args[1].length() > 4)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', incorrectArgument));
				return;
			}

			perXP = Integer.parseInt(args[1]);
			if (perXP <= 0) { perXP = 1; }

			String replacedMessage = setPerXPMessage.replace("%value%", new Integer(perXP).toString());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));

			ReloadPlugin();
			return;
		}

		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMessage));
		return;
	}

//==============================================================================================================================================
//==============================================================================================================================================
//==============================================================================================================================================

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
		loadConfig();
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', enableMessage));
		StartPlugin();
	}

	@Override
    public void onDisable() {
		this.getConfig().set("perTicks", perTicks);
		this.getConfig().set("perXP", perXP);
		this.getConfig().set("Enabled", pluginEnabled);
		this.saveConfig();
    }

	public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
		if (str.equalsIgnoreCase("repairwithxp")) {
			RepairCommands(sender, args);
			return true;
		}

		return false;
	}
	
	public void StartPlugin() {
		if (!pluginEnabled) {
			return;
		}
		
		TaskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                	CheckPlayer(player);
                }
            }
        }, 0L, perTicks);
	}

	public void StopPlugin() {
		Bukkit.getServer().getScheduler().cancelTask(TaskID);
	}

	public void ReloadPlugin() {
		if (!pluginEnabled) {
			return;
		}

		StopPlugin();
		StartPlugin();
	}
}