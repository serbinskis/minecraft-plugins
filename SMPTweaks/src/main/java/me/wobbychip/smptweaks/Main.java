package me.wobbychip.smptweaks;

import me.wobbychip.smptweaks.commands.Commands;
import me.wobbychip.smptweaks.custom.allcraftingrecipes.AllCraftingRecipes;
import me.wobbychip.smptweaks.custom.anticreepergrief.AntiCreeperGrief;
import me.wobbychip.smptweaks.custom.antiendermangrief.AntiEndermanGrief;
import me.wobbychip.smptweaks.custom.autocraft.AutoCraft;
import me.wobbychip.smptweaks.custom.autotrade.AutoTrade;
import me.wobbychip.smptweaks.custom.betterlead.BetterLead;
import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.custom.custombreaking.CustomBreaking;
import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.customworld.CustomWorlds;
import me.wobbychip.smptweaks.custom.disableinvulnerability.DisableInvulnerability;
import me.wobbychip.smptweaks.custom.dropcursedpumpkin.DropCursedPumpkin;
import me.wobbychip.smptweaks.custom.entitylimit.EntityLimit;
import me.wobbychip.smptweaks.custom.essentials.Essentials;
import me.wobbychip.smptweaks.custom.fastcuring.FastCuring;
import me.wobbychip.smptweaks.custom.funnymessages.FunnyMessages;
import me.wobbychip.smptweaks.custom.globaltrading.GlobalTrading;
import me.wobbychip.smptweaks.custom.gravitycontrol.GravityControl;
import me.wobbychip.smptweaks.custom.headdrops.HeadDrops;
import me.wobbychip.smptweaks.custom.holograms.Holograms;
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
import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.library.placeholderapi.PlaceholderAPI;
import me.wobbychip.smptweaks.library.tinyprotocol.TinyProtocol;
import me.wobbychip.smptweaks.tweaks.TweakManager;
import me.wobbychip.smptweaks.utils.GameRules;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public static boolean DEBUG_MODE = System.getenv("computername").equalsIgnoreCase("WOBBYCHIP-PC");
	public static Sound DENY_SOUND_EFFECT = Sound.BLOCK_NOTE_BLOCK_HARP;
	public static char SYM_COLOR = '§';
	public static String MESSAGE_COLOR = SYM_COLOR + "9";
	public static String PREFIX = "SMPTweaks-";
	public static GameRules gameRules;
	public static TweakManager manager;
	public static Main plugin;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		Main.gameRules = new GameRules(Main.plugin).register();

		Utils.sendMessage("[SMPTweaks] Server Version: " + Bukkit.getBukkitVersion() + " (STARTUP)");
		Bukkit.getPluginManager().registerEvents(Main.plugin, Main.plugin);

		Main.manager = new TweakManager();
		Main.manager.addTweak(new AllCraftingRecipes());
		Main.manager.addTweak(new AntiCreeperGrief());
		Main.manager.addTweak(new AntiEndermanGrief());
		Main.manager.addTweak(new AutoCraft());
		Main.manager.addTweak(new AutoTrade());
		Main.manager.addTweak(new BetterLead());
		Main.manager.addTweak(new CustomBreaking());
		Main.manager.addTweak(new ChunkLoader());
		Main.manager.addTweak(new CustomPotions());
		Main.manager.addTweak(new CustomWorlds());
		Main.manager.addTweak(new CustomBlocks());
		Main.manager.addTweak(new DisableInvulnerability());
		Main.manager.addTweak(new DropCursedPumpkin());
		Main.manager.addTweak(new EntityLimit());
		Main.manager.addTweak(new Essentials());
		Main.manager.addTweak(new FastCuring());
		Main.manager.addTweak(new FunnyMessages());
		Main.manager.addTweak(new GlobalTrading());
		Main.manager.addTweak(new GravityControl());
		Main.manager.addTweak(new HeadDrops());
		Main.manager.addTweak(new Holograms());
		Main.manager.addTweak(new NoAdvancements());
		Main.manager.addTweak(new NoArrowInfinity());
		Main.manager.addTweak(new NoEndPortal());
		Main.manager.addTweak(new NoTooExpensive());
		Main.manager.addTweak(new PreventDropCentering());
		Main.manager.addTweak(new PvPDropInventory());
		Main.manager.addTweak(new RemoveDatapackItems());
		Main.manager.addTweak(new RepairWithXP());
		Main.manager.addTweak(new RespawnableDragonEgg());
		Main.manager.addTweak(new ServerPause());
		Main.manager.loadTweaks(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerLoadEvent(ServerLoadEvent event) {                                                    //Actually it is POSTWORLD -> MinecraftServer.java
		if (event.getType() != ServerLoadEvent.LoadType.STARTUP) { return; }                                  //this.server.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
		Utils.sendMessage("[SMPTweaks] Server Version: " + Bukkit.getBukkitVersion() + " (POSTWORLD)");  //this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
                                                                                                              //this.connection.acceptConnections();
		Main.manager.loadTweaks(false);
		Main.plugin.getCommand("smptweaks").setExecutor(new Commands());
		Main.plugin.getCommand("smptweaks").setTabCompleter(new Commands());
		PlaceholderAPI.register();
		CustomBlocks.start();
		TinyProtocol.start();
	}

	public ClassLoader getPluginClassLoader() {
		return this.getClassLoader();
	}

	@Override
	public void onDisable() {
		manager.disableAll();
	}
}