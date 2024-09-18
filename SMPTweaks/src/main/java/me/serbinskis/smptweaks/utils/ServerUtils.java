package me.serbinskis.smptweaks.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;

import java.util.concurrent.ConcurrentHashMap;

public class ServerUtils {
	public static Object levels = null; //Server worlds
	public static Object ticking = null; //Ticking functions, aka, datapacks
	public static Object postReload = null;
	public static Object ImageFrame_itemFrames = null; //Support for ImageFrame - https://www.spigotmc.org/resources/106031/
	private static boolean shutting = false;

	//This actually doesn't pause your server,
	//it just removes worlds and functions from ticking
	//this will only work, if there are no players on the server,
	//but it maybe can also break something if other plugins will try to access worlds
	public static boolean pauseServer() {
		if (ServerUtils.isPaused() || ServerUtils.shutting) { return false; }
		if (!Bukkit.getOnlinePlayers().isEmpty()) { return false; }

		try {
			levels = ReflectionUtils.getSetLevels(Maps.newLinkedHashMap());
			ticking = ReflectionUtils.getSetCustomFunctionDataTicking(ImmutableList.of());
			postReload = ReflectionUtils.getSetCustomFunctionPostReload(false);
			ImageFrame_itemFrames = ReflectionUtils.getSetImageFrame_itemFrames(new ConcurrentHashMap<>(), false);
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
			ReflectionUtils.getSetImageFrame_itemFrames(ImageFrame_itemFrames, true);
			levels = null; ticking = null; postReload = null; ImageFrame_itemFrames = null;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean isPaused() {
		return ((levels != null) || (ticking != null) || (postReload != null));
	}

	//This does not shut down the server, this indicates that the server is shutting down
	//which will make the server to resume, because it is required for proper shutdown
	public static void serverShutdown() {
		ServerUtils.resumeServer();
		ServerUtils.shutting = true;
	}

	public static long getTick() {
		return ReflectionUtils.getTickCount();
	}
}
