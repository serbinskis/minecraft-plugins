package me.wobbychip.recallpotion;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BrewManager extends BukkitRunnable {
	private BrewingStand stand;
	private boolean doUpdate = true;
	private boolean cancelTask = false;
	private int currentTime;
	private int brewTime;

	public BrewManager(Plugin plugin, BrewingStand stand, int time) {
		this.stand = stand;
		this.brewTime = time;
		this.currentTime = 0;

		Utilities.sendMessage("checkBrew add new");
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
        	this.cancel();
        }

        stand.setBrewingTime((int)(400*(1-(double)currentTime/(double)brewTime)));
        if (doUpdate) { stand.update(); } //This causes block to restore inventory

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

        	Main.brews.remove(stand.getLocation());
        	this.cancel();
        } else {
        	currentTime++;
            Utilities.checkBrew(stand);
        }
	}

	public void stop() {
		this.cancelTask = true;
	}

	public void stopUpdate(Boolean arg0) {
		this.doUpdate = !arg0;
	}
}
