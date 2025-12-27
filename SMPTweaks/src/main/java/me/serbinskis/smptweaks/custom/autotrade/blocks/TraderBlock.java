package me.serbinskis.smptweaks.custom.autotrade.blocks;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.autotrade.AutoTrade;
import me.serbinskis.smptweaks.custom.autotrade.Traders;
import me.serbinskis.smptweaks.custom.autotrade.inventory.CustomMerchant;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.utils.VillagerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TraderBlock extends CustomBlock {
    public static String TAG_AUTO_TRADE_XP = "AUTO_TRADE_XP";
    public static String TAG_AUTO_TRADE_INV = "AUTO_TRADE_INV";
    public static String TAG_AUTO_TRADE_RECIPE = "AUTO_TRADE_RECIPE";

    public TraderBlock() {
        super("trader_block", Material.DISPENSER);
        this.setCustomName(Main.SYM_COLOR + "rTrader");
        this.setCustomTitle("Trader");
        this.setTexture("trader_block.png");
        this.setDispensable(Dispensable.CUSTOM);
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.REDSTONE);
        recipe.shape("EEE", "EDE", "RNR");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('D', Material.DISPENSER);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('N', Material.NETHER_STAR);
        return recipe;
    }

    @Override
    public ItemStack prepareCraft(PrepareItemCraftEvent event, World world, ItemStack result) {
        if (!AutoTrade.tweak.getGameRuleBoolean(world)) { return null; }
        return super.prepareCraft(event, world, result);
    }

    @Override
    public boolean prepareDispense(Block block, Inventory inventory, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) {
        if (!AutoTrade.tweak.getGameRuleBoolean(block.getWorld())) { return false; }
        Traders.handleTrader(block).forEach(e -> dispense.put(e, Map.entry(new ItemStack(Material.AIR), -1)));
        return true;
    }

    @Override
    public boolean prepareInventory(InventoryOpenEvent event, Block block) {
        if (PersistentUtils.hasPersistentDataBoolean(block, TAG_AUTO_TRADE_INV)) { return true; }
        TaskUtils.scheduleSyncDelayedTask(() -> CustomMerchant.createInventory((Player) event.getPlayer(), block), 0L);
        return false;
    }

    @Override
    public void remove(Block block, boolean intentional) {
        if (intentional) { releaseXp(block, block.getLocation().clone().add(0.5, 0.5, 0.5)); }
        Collection<Entity> nearbyEntities = Utils.getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), EntityType.WANDERING_TRADER, 0.45, false);
        nearbyEntities.stream().map(WanderingTrader.class::cast).forEach(e -> e.getTrader().closeInventory()); //If it exists, then it is opened
    }

    public static void storeXp(Block block, int xp) {
        int amountXP = 0;

        if (PersistentUtils.hasPersistentDataInteger(block, TAG_AUTO_TRADE_XP)) {
            amountXP = PersistentUtils.getPersistentDataInteger(block, TAG_AUTO_TRADE_XP);
        }

        PersistentUtils.setPersistentDataInteger(block, TAG_AUTO_TRADE_XP, amountXP+xp);
    }

    public static int getXp(Block block) {
        if (!PersistentUtils.hasPersistentDataInteger(block, TAG_AUTO_TRADE_XP)) { return 0; }
        return PersistentUtils.getPersistentDataInteger(block, TAG_AUTO_TRADE_XP);
    }

    public static void releaseXp(Block block, Location location) {
        if (!PersistentUtils.hasPersistentDataInteger(block, TAG_AUTO_TRADE_XP)) { return; }
        int amountXP = PersistentUtils.getPersistentDataInteger(block, TAG_AUTO_TRADE_XP);
        PersistentUtils.setPersistentDataInteger(block, TAG_AUTO_TRADE_XP, 0);
        if (amountXP <= 0) { return; }

        ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
        orb.setExperience(amountXP);
    }

    public static void openInventory(Player player, Block block) {
        if (!(block.getState() instanceof Container container)) { return; }
        PersistentUtils.setPersistentDataBoolean(block, TAG_AUTO_TRADE_INV, true);
        player.closeInventory();
        player.openInventory(container.getInventory());
        PersistentUtils.removePersistentData(block, TAG_AUTO_TRADE_INV);
    }

    public static void setMerchantRecipe(Block block, MerchantRecipe recipe) {
        if (!(block.getState() instanceof Container container)) { return; }
        PersistentUtils.setPersistentDataByteArray(container, TAG_AUTO_TRADE_RECIPE, VillagerUtils.encodeMerchantRecipe(recipe));
    }

    public static @Nullable MerchantRecipe getMerchantRecipe(Block block) {
        if (!(block.getState() instanceof Container container)) { return null; }
        if (!PersistentUtils.hasPersistentDataByteArray(block, TAG_AUTO_TRADE_RECIPE)) { return null; }
        byte[] persistentDataByteArray = PersistentUtils.getPersistentDataByteArray(container, TAG_AUTO_TRADE_RECIPE);
        MerchantRecipe merchantRecipe = VillagerUtils.decodeMerchantRecipe(persistentDataByteArray);
        if (merchantRecipe == null) { PersistentUtils.removePersistentData(block, TAG_AUTO_TRADE_RECIPE); }
        if (merchantRecipe != null) { merchantRecipe.setUses(0); }
        return merchantRecipe;
    }
}
