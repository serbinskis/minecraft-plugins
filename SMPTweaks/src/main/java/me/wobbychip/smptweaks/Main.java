package me.wobbychip.smptweaks;

import me.wobbychip.smptweaks.commands.Commands;
import me.wobbychip.smptweaks.custom.allcraftingrecipes.AllCraftingRecipes;
import me.wobbychip.smptweaks.custom.anticreepergrief.AntiCreeperGrief;
import me.wobbychip.smptweaks.custom.antiendermangrief.AntiEndermanGrief;
import me.wobbychip.smptweaks.custom.autocraft.AutoCraft;
import me.wobbychip.smptweaks.custom.autotrade.AutoTrade;
import me.wobbychip.smptweaks.custom.betterlead.BetterLead;
import me.wobbychip.smptweaks.custom.breakablebedrock.BreakableBedrock;
import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.disableinvulnerability.DisableInvulnerability;
import me.wobbychip.smptweaks.custom.dropcursedpumpkin.DropCursedPumpkin;
import me.wobbychip.smptweaks.custom.entitylimit.EntityLimit;
import me.wobbychip.smptweaks.custom.essentials.Essentials;
import me.wobbychip.smptweaks.custom.expbottles.ExpBottles;
import me.wobbychip.smptweaks.custom.fastcuring.FastCuring;
import me.wobbychip.smptweaks.custom.funnymessages.FunnyMessages;
import me.wobbychip.smptweaks.custom.globaltrading.GlobalTrading;
import me.wobbychip.smptweaks.custom.gravitycontrol.GravityControl;
import me.wobbychip.smptweaks.custom.headdrops.HeadDrops;
import me.wobbychip.smptweaks.custom.holograms.Holograms;
import me.wobbychip.smptweaks.custom.ipprotect.IpProtect;
import me.wobbychip.smptweaks.custom.noadvancements.NoAdvancements;
import me.wobbychip.smptweaks.custom.noarrowinfinity.NoArrowInfinity;
import me.wobbychip.smptweaks.custom.noendportal.NoEndPortal;
import me.wobbychip.smptweaks.custom.notooexpensive.NoTooExpensive;
import me.wobbychip.smptweaks.custom.preventdropcentering.PreventDropCentering;
import me.wobbychip.smptweaks.custom.pvpdropinventory.PvPDropInventory;
import me.wobbychip.smptweaks.custom.removedatapackitems.RemoveDatapackItems;
import me.wobbychip.smptweaks.custom.repairwithxp.RepairWithXP;
import me.wobbychip.smptweaks.custom.respawnabledragonegg.RespawnableDragonEgg;
import me.wobbychip.smptweaks.custom.serverpause.ServerPause;
import me.wobbychip.smptweaks.custom.shriekercansummon.ShriekerCanSummon;
import me.wobbychip.smptweaks.custom.silktouchspawners.SilkTouchSpawners;
import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.tweaks.TweakManager;
import me.wobbychip.smptweaks.utils.GameRules;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static Main plugin;
	public static GameRules gameRules;
	public static TweakManager manager;
	public static String prefix = "SMPTweaks-";
	public static char sym_color = '§';
	public static String color = sym_color + "9";
	public static Sound DENY_SOUND_EFFECT = Sound.BLOCK_NOTE_BLOCK_HARP;
	public static boolean DEBUG_MODE = System.getenv("computername").equalsIgnoreCase("WOBBYCHIP-PC");

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		Main.gameRules = new GameRules(Main.plugin);

		Utils.sendMessage("[SMPTweaks] Server Version: " + ReflectionUtils.version);

		manager = new TweakManager();
		manager.addTweak(new AllCraftingRecipes());
		manager.addTweak(new AntiCreeperGrief());
		manager.addTweak(new AntiEndermanGrief());
		manager.addTweak(new AutoCraft());
		manager.addTweak(new AutoTrade());
		manager.addTweak(new BetterLead());
		manager.addTweak(new BreakableBedrock());
		manager.addTweak(new ChunkLoader());
		manager.addTweak(new CustomPotions());
		manager.addTweak(new DisableInvulnerability());
		manager.addTweak(new DropCursedPumpkin());
		manager.addTweak(new EntityLimit());
		manager.addTweak(new Essentials());
		manager.addTweak(new ExpBottles());
		manager.addTweak(new FastCuring());
		manager.addTweak(new FunnyMessages());
		manager.addTweak(new GlobalTrading());
		manager.addTweak(new GravityControl());
		manager.addTweak(new HeadDrops());
		manager.addTweak(new Holograms());
		manager.addTweak(new IpProtect());
		manager.addTweak(new NoAdvancements());
		manager.addTweak(new NoArrowInfinity());
		manager.addTweak(new NoEndPortal());
		manager.addTweak(new NoTooExpensive());
		manager.addTweak(new PreventDropCentering());
		manager.addTweak(new PvPDropInventory());
		manager.addTweak(new RemoveDatapackItems());
		manager.addTweak(new RepairWithXP());
		manager.addTweak(new RespawnableDragonEgg());
		manager.addTweak(new ServerPause());
		manager.addTweak(new ShriekerCanSummon());
		manager.addTweak(new SilkTouchSpawners());

		Main.plugin.getCommand("smptweaks").setExecutor(new Commands());
		Main.plugin.getCommand("smptweaks").setTabCompleter(new Commands());
		if (Main.DEBUG_MODE) { CustomBlocks.start(); }
	}

	public ClassLoader getPluginClassLoader() {
		return this.getClassLoader();
	}

	@Override
	public void onDisable() {
		manager.disableAll();
	}
}