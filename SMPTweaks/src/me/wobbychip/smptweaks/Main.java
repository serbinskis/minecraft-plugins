package me.wobbychip.smptweaks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.smptweaks.custom.allcraftingrecipes.AllCraftingRecipes;
import me.wobbychip.smptweaks.custom.anticreepergrief.AntiCreeperGrief;
import me.wobbychip.smptweaks.custom.antiendermangrief.AntiEndermanGrief;
import me.wobbychip.smptweaks.custom.autocraft.AutoCraft;
import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.disableinvulnerability.DisableInvulnerability;
import me.wobbychip.smptweaks.custom.dropcursedpumpkin.DropCursedPumpkin;
import me.wobbychip.smptweaks.custom.entitylimit.EntityLimit;
import me.wobbychip.smptweaks.custom.expbottles.ExpBottles;
import me.wobbychip.smptweaks.custom.fastcuring.FastCuring;
import me.wobbychip.smptweaks.custom.funnymessages.FunnyMessages;
import me.wobbychip.smptweaks.custom.globaltrading.GlobalTrading;
import me.wobbychip.smptweaks.custom.gravitycontrol.GravityControl;
import me.wobbychip.smptweaks.custom.headdrops.HeadDrops;
import me.wobbychip.smptweaks.custom.noarrowinfinity.NoArrowInfinity;
import me.wobbychip.smptweaks.custom.notooexpensive.NoTooExpensive;
import me.wobbychip.smptweaks.custom.preventdropcentering.PreventDropCentering;
import me.wobbychip.smptweaks.custom.pvpdropinventory.PvPDropInventory;
import me.wobbychip.smptweaks.custom.repairwithxp.RepairWithXP;
import me.wobbychip.smptweaks.custom.respawnabledragonegg.RespawnableDragonEgg;
import me.wobbychip.smptweaks.custom.shriekercansummon.ShriekerCanSummon;
import me.wobbychip.smptweaks.tweaks.TweakManager;
import me.wobbychip.smptweaks.utils.Utils;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static TweakManager manager;
	public static ClassLoader classLoader;
	public static String prefix = "SMPTweaks-";

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		Main.classLoader = Main.plugin.getClassLoader();

		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		Utils.sendMessage("&9[SMPTweaks] Server Version: " + version);

		manager = new TweakManager();
		manager.addTweak(new AllCraftingRecipes());
		manager.addTweak(new AntiCreeperGrief());
		manager.addTweak(new AntiEndermanGrief());
		manager.addTweak(new AutoCraft());
		manager.addTweak(new ChunkLoader());
		manager.addTweak(new DisableInvulnerability());
		manager.addTweak(new DropCursedPumpkin());
		manager.addTweak(new EntityLimit());
		manager.addTweak(new ExpBottles());
		manager.addTweak(new FastCuring());
		manager.addTweak(new FunnyMessages());
		manager.addTweak(new GlobalTrading());
		manager.addTweak(new GravityControl());
		manager.addTweak(new HeadDrops());
		manager.addTweak(new NoArrowInfinity());
		manager.addTweak(new NoTooExpensive());
		manager.addTweak(new PreventDropCentering());
		manager.addTweak(new PvPDropInventory());
		manager.addTweak(new RepairWithXP());
		manager.addTweak(new RespawnableDragonEgg());
		manager.addTweak(new ShriekerCanSummon());
		manager.addTweak(new CustomPotions());

		Main.plugin.getCommand("smptweaks").setExecutor(new Commands());
	}

	@Override
	public void onDisable() {
		manager.disableAll();
	}
}