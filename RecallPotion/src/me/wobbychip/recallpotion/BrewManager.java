package me.wobbychip.recallpotion;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.network.protocol.game.PacketPlayOutWindowData;
import net.minecraft.server.level.EntityPlayer;

public class BrewManager extends BukkitRunnable {
	private BrewingStand stand;
	private boolean cancelTask = false;
	private int currentTime;
	private int brewTime;

	public BrewManager(Plugin plugin, BrewingStand stand, int time) {
		this.stand = stand;
		this.brewTime = time;
		this.currentTime = 0;

		int fuel = this.stand.getFuelLevel();
		this.stand.setFuelLevel(fuel-1);
		this.stand.update();

		if (fuel > 0) {
			Main.brews.put(stand.getLocation(), this);
			runTaskTimer(plugin, 0, 1);
		}
	}

	@Override
	public void run() {
        if (cancelTask) {
        	Main.brews.remove(stand.getLocation());
        	updateBrewingProgress(0, brewTime);
        	this.cancel();
        	return;
        }

		//Check if brew is done and call event
        if (currentTime >= brewTime) {
        	BrewerInventory bInv = (BrewerInventory) stand.getInventory();
        	BrewEvent brewEvent = new BrewEvent(stand.getBlock(), bInv, stand.getFuelLevel());
        	Bukkit.getPluginManager().callEvent(brewEvent);
        	stand.getLocation().getWorld().playSound(stand.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);

        	//Remove ingredient
        	if (!brewEvent.isCancelled() && !Utilities.isEmpty(bInv.getIngredient())) {
        		bInv.getIngredient().setAmount(bInv.getIngredient().getAmount()-1);
        	}

        	cancelTask = true;
        } else {
        	updateBrewingProgress(currentTime, brewTime);
        	currentTime++;
        }
	}

	public void stop() {
		this.cancelTask = true;
	}

	public void updateBrewingProgress(int current, int total) {
        for (HumanEntity humanEntity : stand.getInventory().getViewers()) {
        	this.sendBrewingProgress((Player) humanEntity, (int)(400*(1-(double)current/(double)total)));
        	if (current == 0) { ((Player) humanEntity).updateInventory(); }
        }
	}

	public void sendBrewingProgress(Player player, int brewTime) {
		try {
	    	if (player == null) { return; }
	    	Object craftPlayer = Main.CraftPlayer.cast(player);
	    	EntityPlayer entityPlayer = (EntityPlayer) player.getClass().getDeclaredMethod("getHandle").invoke(craftPlayer);

	    	int windowID = entityPlayer.bV.j; //Current window ID
	    	PacketPlayOutWindowData brewingTime = new PacketPlayOutWindowData(windowID, 0, brewTime);
	    	entityPlayer.b.sendPacket(brewingTime);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
