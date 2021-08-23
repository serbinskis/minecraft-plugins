package me.wobbychip.expbottles;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
	//Generate random range number
	public static int RandomRange(int min, int max) {
        return min + (int)(Math.random() * (max - min+1));
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

	//Drop item from player position
	public static void dropItem(Player player, ItemStack item) {
		Location location = player.getLocation();
		location.setY(location.getY()+1.3);

		Vector vector = player.getLocation().getDirection();
		vector.multiply(0.32);

		Item itemDropped = player.getWorld().dropItem(location, item);
		itemDropped.setVelocity(vector);
		itemDropped.setPickupDelay(40);
	}

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[ExpBottles] ExpBottles has loaded!"));
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENCHANTING_TABLE) { return; }
		if (event.getItem() == null || event.getItem().getType() != Material.GLASS_BOTTLE) { return; }

		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		if (!player.isSneaking() || ((getPlayerExp(player) < 11)) && (player.getGameMode() != GameMode.CREATIVE)) { return; }
		if (player.getGameMode() != GameMode.CREATIVE) { player.giveExp(RandomRange(4, 11) * -1); }
		if (player.getGameMode() != GameMode.CREATIVE) { item.setAmount(item.getAmount()-1); }
		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0F, 1.0F);

		ItemStack expBootle = new ItemStack(Material.EXPERIENCE_BOTTLE);
		HashMap<Integer, ItemStack> items = player.getInventory().addItem(expBootle);
		if (!items.isEmpty()) { dropItem(player, expBootle); }

		event.setUseInteractedBlock(Result.DENY);
	}
}