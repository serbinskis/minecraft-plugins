package me.serbinskis.smptweaks.custom.autotrade.inventory;

import io.papermc.paper.event.player.PlayerTradeEvent;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.autotrade.AutoTrade;
import me.serbinskis.smptweaks.custom.autotrade.blocks.TraderBlock;
import me.serbinskis.smptweaks.library.customitems.CustomItems;
import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import me.serbinskis.smptweaks.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomMerchant implements Listener {
    public static String TAG_CUSTOM_MERCHANT = "AUTO_TRADE_IS_CUSTOM_MERCHANT";

    public static void createInventory(Player player, Block block) {
        WanderingTrader merchant = player.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), WanderingTrader.class, wanderingTrader -> {
            wanderingTrader.setPersistent(false);
            wanderingTrader.setAI(false);
            wanderingTrader.setInvisible(true);
            wanderingTrader.setCollidable(false);
            wanderingTrader.setGravity(false);
            wanderingTrader.setInvulnerable(true);
            wanderingTrader.setDespawnDelay(5);
            wanderingTrader.setSilent(true);
            wanderingTrader.setCanDrinkMilk(false);
            wanderingTrader.setCanDrinkPotion(false);
            wanderingTrader.getAttribute(Attribute.SCALE).setBaseValue(0);
            wanderingTrader.setCustomNameVisible(false);
            wanderingTrader.setCustomName(Main.SYM_COLOR + "rTrader");
            PersistentUtils.setPersistentDataBoolean(wanderingTrader, TAG_CUSTOM_MERCHANT, true);
        });

        Location location = block.getLocation().clone().add(0.5, 0.5, 0.5);
        Collection<Entity> villagers = Utils.getNearbyEntities(location, EntityType.VILLAGER, AutoTrade.TRADE_DISTANCE + 0.5, false);
        List<Villager> villagerList = villagers.stream().map(Villager.class::cast).filter(villager -> villager.getProfession() != Villager.Profession.NONE).toList();

        villagerList.forEach(villager -> Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(AutoTrade.fakePlayer, villager))); //Support for global trading tweak
        villagerList.forEach(villager -> VillagerUtils.updateSpecialPrices(AutoTrade.fakePlayer, villager));
        List<MerchantRecipe> recipeList = villagerList.stream().map(Merchant::getRecipes).flatMap(List::stream).map(VillagerUtils::cloneMerchantRecipe).collect(Collectors.toList());
        villagerList.forEach(VillagerUtils::resetSpecialPrices);

        MerchantRecipe merchantRecipe0 = TraderBlock.getMerchantRecipe(block);
        if (merchantRecipe0 != null) { recipeList.addFirst(merchantRecipe0); }
        recipeList = Utils.removeDupes(recipeList, CustomMerchant::tradeEquals);

        /*if (TraderBlock.hasMerchantRecipe(block)) { //If recipe is out of stock, it will fail to load result slot in the menu
            MerchantRecipe merchantRecipe0 = TraderBlock.getMerchantRecipe(block);
            Stream<MerchantRecipe> recipeStream = recipeList.stream().filter(e -> CustomMerchant.tradeEquals(e, merchantRecipe0));
            Boolean outOfStock = recipeStream.filter(e -> e.getMaxUses() < e.getUses()).map(e -> true).findFirst().orElse(false);
            if (outOfStock) { merchantRecipe0.setUses(merchantRecipe0.getMaxUses()); }
            recipeList.addFirst(merchantRecipe0);
        }*/

        if (TraderBlock.getXp(block) > 0) {
            MerchantRecipe merchantRecipe1 = new MerchantRecipe(CustomItems.getItemStack("trader_get_experience", "", " (" + TraderBlock.getXp(block) + " EXP)"), 1);
            merchantRecipe1.setIngredients(Collections.nCopies(2, merchantRecipe1.getResult()));
            recipeList.addFirst(merchantRecipe1);
        }

        MerchantRecipe merchantRecipe2 = new MerchantRecipe(CustomItems.getItemStack("trader_open_storage"), 1);
        merchantRecipe2.setIngredients(Collections.nCopies(2, merchantRecipe2.getResult()));
        recipeList.addFirst(merchantRecipe2);

        merchant.setRecipes(recipeList);
        InventoryView inventoryView = player.openMerchant(merchant, true);
        MerchantInventory topInventory = (MerchantInventory) inventoryView.getTopInventory();

        if (merchantRecipe0 != null) { loadMerchantRecipe(topInventory, merchantRecipe0); }
    }

    public static boolean tradeEquals(MerchantRecipe r1, MerchantRecipe r2) {
        if (!r1.getResult().equals(r2.getResult())) { return false; }
        if (!r1.getIngredients().equals(r2.getIngredients())) { return false; }
        return Objects.equals(r1.getAdjustedIngredient1(), r2.getAdjustedIngredient1());
    }

    public static void loadMerchantRecipe(MerchantInventory inventory, MerchantRecipe recipe) {
        if (recipe == null) { return; }
        inventory.setItem(0, Objects.requireNonNullElseGet(recipe.getAdjustedIngredient1(), () -> new ItemStack(Material.AIR)));
        inventory.setItem(1, (recipe.getIngredients().size() > 1) ? recipe.getIngredients().get(1) : new ItemStack(Material.AIR));
        inventory.setItem(2, Objects.requireNonNullElseGet(recipe.getResult(), () -> new ItemStack(Material.AIR)));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof WanderingTrader wanderingTrader)) { return; }
        if (!PersistentUtils.hasPersistentDataBoolean(wanderingTrader, TAG_CUSTOM_MERCHANT)) { return; }

        event.getInventory().clear();
        TaskUtils.scheduleSyncDelayedTask(wanderingTrader::remove, 0L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof WanderingTrader wanderingTrader)) { return; }
        if (!PersistentUtils.hasPersistentDataBoolean(wanderingTrader, TAG_CUSTOM_MERCHANT)) { return; }
        if (event.getClickedInventory() == null) { return; }
        if (event.getClickedInventory().getType() != InventoryType.MERCHANT) { return; }

        Utils.playSound((Player) event.getWhoClicked(), Main.DENY_SOUND_EFFECT);
        ReflectionUtils.sendMerchantOffers((Player) event.getWhoClicked(), wanderingTrader); //Required to sync client and server side
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTradeEvent(PlayerTradeEvent event) {
        if (!PersistentUtils.hasPersistentDataBoolean(event.getVillager(), TAG_CUSTOM_MERCHANT)) { return; }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTradeSelectEvent(TradeSelectEvent event) {
        if (!PersistentUtils.hasPersistentDataBoolean((Entity) event.getMerchant(), TAG_CUSTOM_MERCHANT)) { return; }
        CustomItem customItem = CustomItems.getCustomItem(event.getMerchant().getRecipe(event.getIndex()).getResult());
        Block block = ((Entity) event.getMerchant()).getLocation().getBlock();
        event.setCancelled(true);

        if ((customItem != null) && customItem.getId().equals("trader_open_storage")) {
            TraderBlock.openInventory((Player) event.getWhoClicked(), block);
            return;
        }

        if ((customItem != null) && customItem.getId().equals("trader_get_experience")) {
            if (TraderBlock.getXp(block) == 0) { Utils.playSound((Player) event.getWhoClicked(), Main.DENY_SOUND_EFFECT); }
            TraderBlock.releaseXp(block, event.getWhoClicked().getLocation());
            event.getWhoClicked().closeInventory();
            return;
        }

        MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());
        TraderBlock.setMerchantRecipe(block, recipe);
        loadMerchantRecipe(event.getInventory(), recipe);
    }
}
