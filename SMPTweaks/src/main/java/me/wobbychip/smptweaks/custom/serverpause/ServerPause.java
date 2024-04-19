package me.wobbychip.smptweaks.custom.serverpause;

import io.netty.channel.Channel;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.events.ServerConnectionEvent;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.List;

public class ServerPause extends CustomTweak {
	public static CustomTweak tweak;
	public static int CONNECTION_MAX_TIME = 10;
	public static int STARTUP_DELAY = 60;
	public static boolean gamerule = true;
	public static boolean enabled = true;
	public static int delayTask = -1;

	public static int pauseDelay = 0;
	public static boolean quiteCommands = false;
	public static int cconnections = ReflectionUtils.getConnections().size();

	public ServerPause() {
		super(ServerPause.class, false, false);
		this.setCommand(new Commands(this, "spause"));
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doServerPause", true, true);
		this.setReloadable(true);
		this.setDescription("Pauses the server when there are no players online.");
		ServerPause.tweak = this;
	}

	public void onEnable() {
		this.onReload();

		//Delay event registration so that other plugins can do their thing
		//Tbh, I don't know if this actually is required, but just in case
		TaskUtils.scheduleSyncDelayedTask(() -> {
			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			if (!ServerPause.canPause(false)) { return; }
			boolean success = ServerUtils.pauseServer();
			if (success) { Utils.sendMessage("Server is now paused."); }
		}, 20L*STARTUP_DELAY);

		TaskUtils.scheduleSyncRepeatingTask(() -> {
			Collection<Channel> connections = ReflectionUtils.getConnections();
			if (connections.size() == cconnections) { return; } else { cconnections = connections.size(); }
			boolean online = ReflectionUtils.getConnections().stream().anyMatch(e -> ((e != null) && e.isOpen()));
			Bukkit.getPluginManager().callEvent(new ServerConnectionEvent(cconnections, online));
		}, 20L*STARTUP_DELAY, 1L);
	}

	public void onDisable() {
		ServerUtils.serverShutdown();
	}

	public void onReload() {
		ServerPause.pauseDelay = this.getConfig(0).getConfig().getInt("pauseDelay");
		ServerPause.quiteCommands = this.getConfig(0).getConfig().getBoolean("quiteCommands");
		if (ServerPause.pauseDelay < 0) { ServerPause.pauseDelay = 0; }

		if (delayTask < 0) { return; }
		delayTask = TaskUtils.rescheduleSyncDelayedTask(delayTask, pauseDelay);
		if (pauseDelay <= 0) { TaskUtils.finishTask(delayTask); }
	}

	public static boolean canPause(boolean bConnections) {
		if (!ServerPause.enabled) { return false; }
		gamerule = ServerUtils.isPaused() ? gamerule : ServerPause.tweak.getGameRuleBoolean(null);
		boolean online = (!ReflectionUtils.getConnections().isEmpty());
		return (Bukkit.getOnlinePlayers().isEmpty()) && (!online || !bConnections) && gamerule;
	}
}
