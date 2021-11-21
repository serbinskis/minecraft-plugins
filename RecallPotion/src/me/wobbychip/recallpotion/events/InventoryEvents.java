package me.wobbychip.recallpotion.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

import me.wobbychip.recallpotion.Main;
import me.wobbychip.recallpotion.Utils;

public class InventoryEvents implements Listener {
	//This event is kinda bugged
	//Changing item causes brew stand to brew potion again if recipe is valid
	//So one solution is just run event delayed
	@EventHandler(priority=EventPriority.MONITOR)
    public void onBrew(BrewEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getContents().getIngredient() == null) { return; }

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				for (int i = 0; i < 3; i++) {
					ItemStack item = event.getContents().getItem(i);

					if (Utils.isPotion(item) && (item.getItemMeta() != null) && (item.getItemMeta().getLocalizedName() != null)) {
						String name = item.getItemMeta().getLocalizedName();
						ItemStack ingredient = event.getContents().getIngredient();
						PotionData potionData = ((PotionMeta) item.getItemMeta()).getBasePotionData();
						//If item not potion this causes error ^ - fixed

						//Brewing potion -> base + custom ingredient
						if ((potionData.getType() == Main.potionBase) && (ingredient.getType() == Main.potionIngredient)) {
							switch (item.getType()) {
								case POTION: {
									Utils.sendMessage("case POTION");
									event.getContents().setItem(i, Main.potionItem);
									break;
								}
								case SPLASH_POTION: {
									Utils.sendMessage("case SPLASH_POTION");
									event.getContents().setItem(i, Main.splashPotionItem);
									break;
								}
								case LINGERING_POTION: {
									Utils.sendMessage("case LINGERING_POTION");
									event.getContents().setItem(i, Main.lingeringPotionItem);
									break;
								}
								default: break;
							}
						}

						//Brewing splash potion -> custom potion + gunpower
						if (name.equals(Main.potionItem.getItemMeta().getLocalizedName()) && (ingredient.getType() == Material.GUNPOWDER)) {
							Utils.sendMessage("delay splashPotionItem");
							event.getContents().setItem(i, Main.splashPotionItem);
						}

						//Brewing lingering potion -> custom potion + dragon breath
						if (name.equals(Main.splashPotionItem.getItemMeta().getLocalizedName()) && (ingredient.getType() == Material.DRAGON_BREATH)) {
							Utils.sendMessage("delay lingeringPotionItem");
							event.getContents().setItem(i, Main.lingeringPotionItem);
						}
					}
				}
			}
		}, 1L);
    }
}