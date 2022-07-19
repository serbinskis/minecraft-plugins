package me.wobbychip.custompotions.events;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import me.wobbychip.custompotions.Main;
import me.wobbychip.custompotions.potions.CustomPotion;

public class VillagerEvents implements Listener {	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onVillagerAcquireTradeEvent(VillagerAcquireTradeEvent event) {
		if (!(event.getEntity() instanceof Villager)) { return; }
		if (((Villager) event.getEntity()).getProfession() != Profession.FLETCHER) { return; }
		if (event.getRecipe().getResult().getType() != Material.TIPPED_ARROW) { return; }
		if (event.getRecipe().getResult().hasItemMeta()) { return; }

		List<CustomPotion> potions = Main.manager.getPotions(false);
		CustomPotion customPotion = potions.get(new Random().nextInt(potions.size()));

		ItemStack arrow = customPotion.getTippedArrow(true, event.getRecipe().getResult().getAmount());
		int uses = event.getRecipe().getUses();
		int maxUses = event.getRecipe().getMaxUses();
		boolean experienceReward = event.getRecipe().hasExperienceReward();
		int villagerExperience = event.getRecipe().getVillagerExperience();
		float priceMultiplier = event.getRecipe().getPriceMultiplier();
		int demand = event.getRecipe().getDemand();
		int specialPrice = event.getRecipe().getSpecialPrice();

		MerchantRecipe recipe = new MerchantRecipe(arrow, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);
		recipe.setIngredients(event.getRecipe().getIngredients());
		event.setRecipe(recipe);
	}
}
