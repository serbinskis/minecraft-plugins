package me.wobbychip.smptweaks.utils;

import org.bukkit.Bukkit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.server.MinecraftServer;

public class ServerUtils {
	public static Object levels = null; //Server worlds
	public static Object ticking = null; //Ticking functions, aka, datapacks
	public static Object postReload = null;
	private static boolean shutting = false;

	//This actually doesn't pause your server,
	//it just removes worlds and functions from ticking
	//this will only work, if there are no players on the server,
	//but it maybe can also break something if other plugins that try to access worlds
	@SuppressWarnings("deprecation")
	public static boolean pauseServer() {
		if (ServerUtils.isPaused() || ServerUtils.shutting) { return false; }
		if (Bukkit.getOnlinePlayers().size() > 0) { return false; }

		try {
			levels = ReflectionUtils.MinecraftServer_levels.get(MinecraftServer.getServer());
			ReflectionUtils.MinecraftServer_levels.set(MinecraftServer.getServer(), Maps.newLinkedHashMap());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

		try {
			Object functionManager = ReflectionUtils.MinecraftServer_functionManager.get(MinecraftServer.getServer());
			ticking = ReflectionUtils.CustomFunctionData_ticking.get(functionManager);
			postReload = ReflectionUtils.CustomFunctionData_postReload.get(functionManager);
			ReflectionUtils.CustomFunctionData_ticking.set(functionManager, ImmutableList.of());
			ReflectionUtils.CustomFunctionData_postReload.set(functionManager, false);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	public static boolean resumeServer() {
		if (!ServerUtils.isPaused()) { return false; }

		try {
			Object functionManager = ReflectionUtils.MinecraftServer_functionManager.get(MinecraftServer.getServer());
			ReflectionUtils.CustomFunctionData_ticking.set(functionManager, ticking);
			ReflectionUtils.CustomFunctionData_postReload.set(functionManager, postReload);
			ReflectionUtils.MinecraftServer_levels.set(MinecraftServer.getServer(), levels);
			ticking = null;
			postReload = null;
			levels = null;
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
