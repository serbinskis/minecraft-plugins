package me.serbinskis.smptweaks.custom.cycletrades;

import io.papermc.paper.event.player.PlayerTradeEvent;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.VillagerUtils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;
import java.util.stream.Stream;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTradeSelectEvent(TradeSelectEvent event) {
		MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());
		if (!PersistentUtils.hasPersistentDataBoolean(recipe.getResult(), CycleTrades.CYCLE_ITEM_TAG)) { return; }
		pressCycleTradesButton((Villager) event.getMerchant(), (Player) event.getWhoClicked());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTradeEvent(PlayerTradeEvent event) {
		if (PersistentUtils.hasPersistentDataBoolean(event.getTrade().getResult(), CycleTrades.CYCLE_ITEM_TAG)) { return; }
		if (!((event.getVillager() instanceof Villager villager))) { return; }
		if (FakePlayer.isFakePlayer(event.getPlayer())) { return; }

		if (villager.getVillagerLevel() > 1) { return; }
		if (villager.getVillagerExperience() > 0) { return; }

		//Remove first item, which should be cycle_trade, and replace it with cycle_trade_disabled
		//When closing inventory it should be removed automatically

		TaskUtils.scheduleSyncDelayedTask(() -> {
			if (villager.getTrader() == null) { return; }
			Stream<MerchantRecipe> recipeStream = villager.getRecipes().stream().filter(recipe -> !PersistentUtils.hasPersistentDataBoolean(recipe.getResult(), CycleTrades.CYCLE_ITEM_TAG));
			villager.setRecipes(Stream.concat(Stream.of(CycleTrades.getMerchantRecipe(true)), recipeStream).toList());
			ReflectionUtils.sendMerchantOffers(event.getPlayer(), villager);
		}, 0L);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		if (event.getInventory().getType() != InventoryType.MERCHANT) { return; }
		if (!(event.getInventory().getHolder() instanceof Villager villager)) { return; }
		if (!CycleTrades.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (FakePlayer.isFakePlayer((Player) event.getPlayer())) { return; }
		addCycleTradesButton(villager);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		if (event.getInventory().getType() != InventoryType.MERCHANT) { return; }
		if (!(event.getInventory().getHolder() instanceof Villager villager)) { return; }
		removeCycleTradesButton(villager, null);
	}

	public static void addCycleTradesButton(Villager villager) {
		if (villager.getProfession() == Villager.Profession.NONE) { return; }
		if (villager.getVillagerLevel() > 1) { return; }
		if (villager.getVillagerExperience() > 0) { return; }

		removeCycleTradesButton(villager, null);
		List<MerchantRecipe> recipes = Stream.concat(Stream.of(CycleTrades.getMerchantRecipe(false)), villager.getRecipes().stream()).toList();
		villager.setRecipes(recipes);
	}

	public static void removeCycleTradesButton(Villager villager, Player player) {
		if (villager.getProfession() == Villager.Profession.NONE) { return; }
		if (villager.getVillagerLevel() > 1) { return; }

		villager.setRecipes(villager.getRecipes().stream().filter(merchantRecipe -> {
			return !PersistentUtils.hasPersistentDataBoolean(merchantRecipe.getResult(), CycleTrades.CYCLE_ITEM_TAG);
		}).toList());

		if (player != null) { player.openMerchant(villager, true); }
	}

	public static void pressCycleTradesButton(Villager villager, Player player) {
		if (villager.getProfession() == Villager.Profession.NONE) { return; }
		if (villager.getVillagerLevel() > 1) { return; }
		if (villager.getVillagerExperience() > 0) { return; }

		villager.setRecipes(List.of());
		villager.addTrades(2);
		VillagerUtils.updateSpecialPrices(player, villager);
		addCycleTradesButton(villager);
		ReflectionUtils.sendMerchantOffers(player, villager);
	}
}