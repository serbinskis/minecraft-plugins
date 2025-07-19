package me.serbinskis.smptweaks.custom.autotrade;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.autotrade.blocks.TraderBlock;
import me.serbinskis.smptweaks.custom.autotrade.events.InventoryEvents;
import me.serbinskis.smptweaks.custom.autotrade.inventory.CustomMerchant;
import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.library.customitems.CustomItems;
import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AutoTrade extends CustomTweak {
	public static int TRADE_DISTANCE = 2;
	public static CustomTweak tweak;
	public static Player fakePlayer;

	public AutoTrade() {
		super(AutoTrade.class, true, false);
		this.setGameRule("doAutoTrade", true, false);
		this.setDescription("Allows to automatically trade with villagers. Crafted inside crafting table.");
		AutoTrade.tweak = this;
	}

	public void onEnable() {
		Location location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		AutoTrade.fakePlayer = FakePlayer.addFakePlayer(location, new UUID(0, 0), false, false, false, true, false);

		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new CustomMerchant(), Main.plugin);

		CustomBlocks.registerBlock(new TraderBlock());
		CustomItems.registerItem(new CustomItem("trader_get_experience", Material.KNOWLEDGE_BOOK).setTexture("trader_get_experience.png").setCustomName("§r§6§lGet Experience"));
		CustomItems.registerItem(new CustomItem("trader_open_storage", Material.KNOWLEDGE_BOOK).setTexture("trader_open_storage.png").setCustomName("§r§d§lOpen Storage"));
	}
}
