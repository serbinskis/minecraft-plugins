package me.wobbychip.smptweaks.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.server.MinecraftServer;

public class ServerUtils {
	public static Object levels = null; //Server worlds
	public static Object ticking = null; //Ticking functions, aka, datapacks
	public static Object postReload = null;

	//This actually doesn't pause your server,
	//it just removes worlds and functions from ticking
	//this will only work, if there are no players on the server,
	//but it maybe can also break something if other plugins try to access worlds
	@SuppressWarnings("deprecation")
	public static void pauseServer() {
		if ((levels != null) || (ticking != null) || (postReload != null)) { return; }

		try {
			levels = ReflectionUtils.MinecraftServer_levels.get(MinecraftServer.getServer());
			ReflectionUtils.MinecraftServer_levels.set(MinecraftServer.getServer(), Maps.newLinkedHashMap());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		try {
			Object functionManager = ReflectionUtils.MinecraftServer_functionManager.get(MinecraftServer.getServer());
			ticking = ReflectionUtils.CustomFunctionData_ticking.get(functionManager);
			postReload = ReflectionUtils.CustomFunctionData_postReload.get(functionManager);
			ReflectionUtils.CustomFunctionData_ticking.set(functionManager, ImmutableList.of());
			ReflectionUtils.CustomFunctionData_postReload.set(functionManager, false);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static void resumeServer() {
		if ((levels == null) || (ticking == null) || (postReload == null)) { return; }

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
		}
	}

	public static boolean isPaused() {
		return ((levels != null) || (ticking != null) || (postReload != null));
	}
}
