package me.wobbychip.smptweaks.custom.custompotions.events;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class VillagerEvents implements Listener {
	List<Material> potionTypes = Arrays.asList(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
	public List<String> vanilla = Arrays.asList(
			"night_vision",
			"invisibility",
			"leaping",
			"fire_resistance",
			"swiftness",
			"slowness",
			"water_breathing",
			"healing",
			"harming",
			"poison",
			"regeneration",
			"strength",
			"weakness",
			"turtle_master",
			"slow_falling"
		);

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onVillagerAcquireTradeEvent(VillagerAcquireTradeEvent event) {
		if (!(event.getEntity() instanceof Villager)) { return; }
		if (((Villager) event.getEntity()).getProfession() == Profession.FLETCHER) { villagerAcquireTradeFletcher(event); }
		if (((Villager) event.getEntity()).getProfession() == Profession.CLERIC) { villagerAcquireTradeCleric(event); }
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTradeSelectEvent(TradeSelectEvent event) {
		MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(recipe.getResult());
		if (customPotion == null) { return; }

		boolean canBuy = customPotion.isEnabled() && customPotion.getAllowVillagerTrades();
		if (Utils.isTippedArrow(recipe.getResult())) { canBuy = canBuy && customPotion.getAllowTippedArrow(); }
		if (canBuy) { return; }

		Utils.sendActionMessage(null, (Player) event.getWhoClicked(), "Potion is disabled.");
		event.setResult(Result.DENY);
	}

	public void villagerAcquireTradeFletcher(VillagerAcquireTradeEvent event) {
		if (event.getRecipe().getResult().getType() != Material.TIPPED_ARROW) { return; }
		boolean isCustom = !event.getRecipe().getResult().hasItemMeta();
		boolean isChance = !(new Random().nextInt(100)+1 >= CustomPotions.tradingArrowChance);
		if (!isChance && !isCustom) { return; }

		List<CustomPotion> potions = CustomPotions.manager.getPotions(false);
		Iterator<CustomPotion> iterator = potions.iterator();
		ItemStack arrow = new ItemStack(Material.ARROW, 5);

		while (iterator.hasNext()) {
			CustomPotion potion = iterator.next();
			if (!potion.getAllowTippedArrow() || !potion.getAllowVillagerTrades()) { iterator.remove(); };
		}

		if ((potions.size() > 0) && isChance) {
			CustomPotion customPotion = potions.get(new Random().nextInt(potions.size()));
			arrow = customPotion.getTippedArrow(true, event.getRecipe().getResult().getAmount());
		} else {
			String vanillaPotion = vanilla.get(new Random().nextInt(vanilla.size()));
			arrow = ReflectionUtils.setPotionTag(event.getRecipe().getResult(), "minecraft:" + vanillaPotion);
		}

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

	public void villagerAcquireTradeCleric(VillagerAcquireTradeEvent event) {
		if (event.getRecipe().getResult().getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (new Random().nextInt(100)+1 >= CustomPotions.tradingPotionChance) { return; }

		List<CustomPotion> potions = CustomPotions.manager.getPotions(false);
		Iterator<CustomPotion> iterator = potions.iterator();

		while (iterator.hasNext()) {
			if (!iterator.next().getAllowVillagerTrades()) { iterator.remove(); };
		}

		if (potions.size() == 0) { return; }
		CustomPotion customPotion = potions.get(new Random().nextInt(potions.size()));
		ItemStack potion = customPotion.setProperties(new ItemStack(potionTypes.get(new Random().nextInt(potionTypes.size()))));

		int uses = event.getRecipe().getUses();
		int maxUses = event.getRecipe().getMaxUses();
		boolean experienceReward = event.getRecipe().hasExperienceReward();
		int villagerExperience = event.getRecipe().getVillagerExperience();
		float priceMultiplier = event.getRecipe().getPriceMultiplier();
		int demand = event.getRecipe().getDemand();
		int specialPrice = event.getRecipe().getSpecialPrice();

		MerchantRecipe recipe = new MerchantRecipe(potion, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);
		recipe.setIngredients(event.getRecipe().getIngredients());
		event.setRecipe(recipe);
	}
}
