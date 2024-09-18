package me.serbinskis.smptweaks;

import me.serbinskis.smptweaks.commands.Commands;
import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import me.serbinskis.smptweaks.library.placeholderapi.PlaceholderAPI;
import me.serbinskis.smptweaks.library.tinyprotocol.TinyProtocol;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.tweaks.TweakManager;
import me.serbinskis.smptweaks.utils.GameRules;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.Utils;
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
	public static String TWEAKS_PACKAGE = "me.serbinskis.smptweaks";
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
		Main.manager = new TweakManager();

		Utils.sendMessage("[SMPTweaks] Server Version: " + Bukkit.getBukkitVersion() + " (STARTUP)");
		Bukkit.getPluginManager().registerEvents(Main.plugin, Main.plugin);
		ReflectionUtils.getInstances(getPluginClassLoader(), TWEAKS_PACKAGE, CustomTweak.class, true, true, true).forEach(Main.manager::addTweak);
		Main.manager.loadTweaks(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerLoadEvent(ServerLoadEvent event) {                                                    //Actually it is POSTWORLD -> MinecraftServer.java
		if (event.getType() != ServerLoadEvent.LoadType.STARTUP) { return; }                                  //this.server.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
		Utils.sendMessage("[SMPTweaks] Server Version: " + Bukkit.getBukkitVersion() + " (POSTWORLD)");  //this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
		FakePlayer.start();																					  //this.connection.acceptConnections();

		Main.manager.loadTweaks(false);
		Main.plugin.getCommand("smptweaks").setExecutor(new Commands());
		Main.plugin.getCommand("smptweaks").setTabCompleter(new Commands());

		PlaceholderAPI.register();
		CustomBlocks.start();
		TinyProtocol.start();
	}

	@Override
	public void onDisable() {
		manager.disableAll();
		TinyProtocol.stop();
	}

	public ClassLoader getPluginClassLoader() {
		return this.getClassLoader();
	}
}