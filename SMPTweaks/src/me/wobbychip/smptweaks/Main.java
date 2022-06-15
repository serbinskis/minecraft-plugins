package me.wobbychip.smptweaks;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.smptweaks.custom.allcraftingrecipes.AllCraftingRecipes;
import me.wobbychip.smptweaks.custom.anticreepergrief.AntiCreeperGrief;
import me.wobbychip.smptweaks.custom.antiendermangrief.AntiEndermanGrief;
import me.wobbychip.smptweaks.custom.disableinvulnerability.DisableInvulnerability;
import me.wobbychip.smptweaks.custom.dropcursedpumpkin.DropCursedPumpkin;
import me.wobbychip.smptweaks.custom.entitylimit.EntityLimit;
import me.wobbychip.smptweaks.custom.expbottles.ExpBottles;
import me.wobbychip.smptweaks.custom.fastcuring.FastCuring;
import me.wobbychip.smptweaks.custom.funnymessages.FunnyMessages;
import me.wobbychip.smptweaks.custom.globaltrading.GlobalTrading;
import me.wobbychip.smptweaks.custom.preventdropcentering.PreventDropCentering;
import me.wobbychip.smptweaks.custom.pvpdropinventory.PvPDropInventory;
import me.wobbychip.smptweaks.custom.repairwithxp.RepairWithXP;
import me.wobbychip.smptweaks.custom.respawnabledragonegg.RespawnableDragonEgg;
import me.wobbychip.smptweaks.tweaks.TweakManager;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static TweakManager manager;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		manager = new TweakManager();
		manager.addTweak(new AllCraftingRecipes());
		manager.addTweak(new AntiEndermanGrief());
		manager.addTweak(new AntiCreeperGrief());
		manager.addTweak(new DisableInvulnerability());
		manager.addTweak(new DropCursedPumpkin());
		manager.addTweak(new EntityLimit());
		manager.addTweak(new ExpBottles());
		manager.addTweak(new FastCuring());
		manager.addTweak(new FunnyMessages());
		manager.addTweak(new GlobalTrading());
		manager.addTweak(new PreventDropCentering());
		manager.addTweak(new PvPDropInventory());
		manager.addTweak(new RepairWithXP());
		manager.addTweak(new RespawnableDragonEgg());

		Main.plugin.getCommand("smptweaks").setExecutor(new Commands());
	}

	@Override
	public void onDisable() {
		manager.disableAll();
	}
}