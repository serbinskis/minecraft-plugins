package me.serbinskis.smptweaks.custom.cycletrades;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customitems.CustomItems;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import org.bukkit.Bukkit;
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
		CustomItems.registerItem(new CycleItem());
	}

	public static MerchantRecipe getMerchantRecipe() {
		ItemStack itemStack = CustomItems.getItemStack("cycle_trade");
		MerchantRecipe merchantRecipe = new MerchantRecipe(PersistentUtils.setPersistentDataBoolean(itemStack, CYCLE_ITEM_TAG, true), 1);
		merchantRecipe.setIngredients(Collections.nCopies(2, CustomItems.getItemStack("cycle_trade")));
		return merchantRecipe;
	}
}
