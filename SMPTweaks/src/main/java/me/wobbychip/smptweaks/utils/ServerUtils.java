package me.wobbychip.smptweaks.utils;

import org.bukkit.Bukkit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class ServerUtils {
	public static Object levels = null; //Server worlds
	public static Object ticking = null; //Ticking functions, aka, datapacks
	public static Object postReload = null;
	private static boolean shutting = false;

	//This actually doesn't pause your server,
	//it just removes worlds and functions from ticking
	//this will only work, if there are no players on the server,
	//but it maybe can also break something if other plugins that try to access worlds
	public static boolean pauseServer() {
		if (ServerUtils.isPaused() || ServerUtils.shutting) { return false; }
		if (!Bukkit.getOnlinePlayers().isEmpty()) { return false; }

		try {
			levels = ReflectionUtils.getSetLevels(Maps.newLinkedHashMap());
			ticking = ReflectionUtils.getSetCustomFunctionDataTicking(ImmutableList.of());
			postReload = ReflectionUtils.getSetCustomFunctionPostReload(false);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean resumeServer() {
		if (!ServerUtils.isPaused()) { return false; }

		try {
			ReflectionUtils.getSetLevels(levels);
			ReflectionUtils.getSetCustomFunctionDataTicking(ticking);
			ReflectionUtils.getSetCustomFunctionPostReload(postReload);
			levels = null; ticking = null; postReload = null;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean isPaused() {
		return ((levels != null) || (ticking != null) || (postReload != null));
	}

	//This does not shutdown the server
	//this indicates that the server is shutting down
	//which will make the server to resume
	//because it is required for propper shutdown
	public static void serverShutdown() {
		ServerUtils.resumeServer();
		ServerUtils.shutting = true;
	}
}
