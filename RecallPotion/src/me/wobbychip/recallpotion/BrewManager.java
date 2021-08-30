package me.wobbychip.recallpotion;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
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

		int fuel = this.stand.getFuelLevel();
		this.stand.setFuelLevel(fuel-1);
		this.updatePotions();

		if (fuel > 0) {
			Main.brews.put(stand.getLocation(), this);
			runTaskTimer(plugin, 0, 1);
		}
	}

	//Destroying and replacing block causes duplication
	//Because block can be breaken between tick, which means no checking was done
	//The only solution is implement BlockBreakEvent and BlockExplodeEvent
	//And then remove them by location from HashMap
	@Override
	public void run() {
        if (cancelTask) {
        	Main.brews.remove(stand.getLocation());
        	this.cancel();
        	return;
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
            //Utilities.checkBrew(stand);
        }
	}

	public void stop() {
		this.cancelTask = true;
	}

	public boolean getDoUpdate() {
		return this.doUpdate;
	}

	public void setDoUpdate(Boolean arg0) {
		this.doUpdate = arg0;
	}

	public void updatePotions() {
		Inventory inv = this.stand.getInventory();
		org.bukkit.block.data.type.BrewingStand brewingData = (org.bukkit.block.data.type.BrewingStand) this.stand.getBlockData();

		for (int i = 0; i < 3; i++) {
			brewingData.setBottle(i, !Utilities.isEmpty(inv.getItem(i)));
		}

		this.stand.setBlockData(brewingData);
		this.stand.update();
	}

	public void updateInventory(Inventory inv) {
		if (inv == null || inv.getType() != InventoryType.BREWING) { return; }
		this.stand.getInventory().setContents(inv.getContents());
		this.stand.getSnapshotInventory().setContents(inv.getContents());
		this.updatePotions();
	}
}
