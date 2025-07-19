package me.serbinskis.smptweaks.custom.custompotions.events;

import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.PotionManager;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VillagerEvents implements Listener {
	List<Material> potionTypes = Arrays.asList(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onVillagerAcquireTradeEvent(VillagerAcquireTradeEvent event) {
		if (!(event.getEntity() instanceof Villager)) { return; }
		if (((Villager) event.getEntity()).getProfession() == Profession.FLETCHER) { villagerAcquireTradeFletcher(event); }
		if (((Villager) event.getEntity()).getProfession() == Profession.CLERIC) { villagerAcquireTradeCleric(event); }
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) { return; }
		if (!(event.getRightClicked() instanceof Villager villager)) { return; }
		if (villager.getProfession() == Profession.NONE) { return; }
		ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
		if (item.getType() != Material.DEBUG_STICK) { return; }
		if (!Utils.hasPermissions(event.getPlayer(), "cpotions.get") || event.getPlayer().getGameMode() != GameMode.CREATIVE) { return; }
		villager.setRecipes(List.of());

		for (int i = 1; i <= 5; i++) {
			villager.setVillagerLevel(i);
			villager.addTrades(2);
		}

		villager.setVillagerExperience(999);
		Utils.playSound(event.getPlayer(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
		event.getPlayer().openMerchant(villager, true);
		event.setCancelled(true);
	}

	//If potion is disabled, but we still have villager that have it, prevent trade selection
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTradeSelectEvent(TradeSelectEvent event) {
		MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());
		CustomPotion customPotion = PotionManager.getCustomPotion(recipe.getResult());
		if (customPotion == null) { return; }

		boolean canBuy = customPotion.isEnabled() && customPotion.getAllowVillagerTrades();
		if (Utils.isTippedArrow(recipe.getResult())) { canBuy = canBuy && customPotion.getAllowTippedArrow(); }
		if (canBuy) { return; }

		Utils.sendActionMessage((Player) event.getWhoClicked(), "Potion is disabled.");
		event.setResult(Result.DENY);
	}

	//In case if new trade tipped arrow, then with random cache we replace it with custom potion arrow
	public void villagerAcquireTradeFletcher(VillagerAcquireTradeEvent event) {
		if (event.getRecipe().getResult().getType() != Material.TIPPED_ARROW) { return; }
		if (new Random().nextInt(100)+1 >= CustomPotions.tradingArrowChance) { return; }

		List<CustomPotion> potions = PotionManager.getPotions(false);
		potions.removeIf(potion -> !potion.getAllowTippedArrow() || !potion.getAllowVillagerTrades());
		if (potions.isEmpty()) { return; }

		int uses = event.getRecipe().getUses();
		int maxUses = event.getRecipe().getMaxUses();
		boolean experienceReward = event.getRecipe().hasExperienceReward();
		int villagerExperience = event.getRecipe().getVillagerExperience();
		float priceMultiplier = event.getRecipe().getPriceMultiplier();
		int demand = event.getRecipe().getDemand();
		int specialPrice = event.getRecipe().getSpecialPrice();

		CustomPotion customPotion = potions.get(new Random().nextInt(potions.size()));
		ItemStack arrow = customPotion.getTippedArrow(true, event.getRecipe().getResult().getAmount());
		MerchantRecipe recipe = new MerchantRecipe(arrow, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);
		recipe.setIngredients(event.getRecipe().getIngredients());
		event.setRecipe(recipe);
	}

	//In case if new trade contains experience bottle, then with random cache we replace it with custom potion
	public void villagerAcquireTradeCleric(VillagerAcquireTradeEvent event) {
		if (event.getRecipe().getResult().getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (new Random().nextInt(100)+1 >= CustomPotions.tradingPotionChance) { return; }

		List<CustomPotion> potions = PotionManager.getPotions(false);
		potions.removeIf(potion -> !potion.getAllowVillagerTrades());
		if (potions.isEmpty()) { return; }

		int uses = event.getRecipe().getUses();
		int maxUses = event.getRecipe().getMaxUses();
		boolean experienceReward = event.getRecipe().hasExperienceReward();
		int villagerExperience = event.getRecipe().getVillagerExperience();
		float priceMultiplier = event.getRecipe().getPriceMultiplier();
		int demand = event.getRecipe().getDemand();
		int specialPrice = event.getRecipe().getSpecialPrice();

		CustomPotion customPotion = potions.get(new Random().nextInt(potions.size()));
		ItemStack potion = customPotion.setProperties(new ItemStack(potionTypes.get(new Random().nextInt(potionTypes.size()))));
		MerchantRecipe recipe = new MerchantRecipe(potion, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);
		recipe.setIngredients(event.getRecipe().getIngredients());
		event.setRecipe(recipe);
	}
}
