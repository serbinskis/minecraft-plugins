package me.serbinskis.smptweaks.custom.cycletrades;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customitems.CustomItems;
import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.Collections;

public class CycleTrades extends CustomTweak {
	public static String CYCLE_ITEM_TAG = "isCycleItemTag";
	public static CustomTweak tweak;

	public CycleTrades() {
		super(CycleTrades.class, true, false);
		this.setGameRule("doTradeCycle", true, false);
		this.setDescription("Allows cycling trades inside villager.");
		CycleTrades.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
		CustomItems.registerItem(new CustomItem("cycle_trade", Material.KNOWLEDGE_BOOK).setTexture("cycle_trade.png").setCustomName("§r§d§lCycle Trades"));
		CustomItems.registerItem(new CustomItem("cycle_trade_disabled", Material.KNOWLEDGE_BOOK).setTexture("cycle_trade_disabled.png").setCustomName("§r§d§lCycle Trades"));
	}

	public static MerchantRecipe getMerchantRecipe(boolean disabled) {
		ItemStack itemStack = CustomItems.getItemStack("cycle_trade" + (disabled ? "_disabled" : ""));
		MerchantRecipe merchantRecipe = new MerchantRecipe(PersistentUtils.setPersistentDataBoolean(itemStack, CYCLE_ITEM_TAG, true), 1);
		merchantRecipe.setIngredients(Collections.nCopies(2, merchantRecipe.getResult()));
		return merchantRecipe;
	}
}
