package me.wobbychip.smptweaks.utils;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import me.wobbychip.smptweaks.Main;

public class TaskUtils implements Listener {
	private static HashMap<Integer, Runnable> runnables0 = new HashMap<Integer, Runnable>();
	private static HashMap<Runnable, Integer> runnables1 = new HashMap<Runnable, Integer>();

	public static int scheduleSyncDelayedTask(Runnable runnable, long ticks) {
		Runnable wrapper = new Runnable() { public void run() {
			Integer task = runnables1.remove(this);
			if (task != null) { runnables0.remove(task); }
			runnable.run();
		}};

		int task = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, wrapper, ticks);
		runnables0.put(task, wrapper);
		runnables1.put(wrapper, task);
		return task;
	}

	public static int rescheduleSyncDelayedTask(int task, long ticks) {
		if (!runnables0.containsKey(task)) { return -1; }
		Runnable runnable = runnables0.remove(task);
		if (runnable != null) { runnables1.remove(runnable); }
		Bukkit.getScheduler().cancelTask(task);

		task = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, runnable, ticks);
		runnables0.put(task, runnable);
		runnables1.put(runnable, task);
		return task;
	}

	public static void cancelSyncDelayedTask(int task) {
		Runnable runnable = runnables0.remove(task);
		if (runnable != null) { runnables1.remove(runnable); }
		Bukkit.getScheduler().cancelTask(task);
	}

	public static void finishSyncDelayedTask(int task) {
		if (!runnables0.containsKey(task)) { return; }
		runnables0.get(task).run();
		Bukkit.getScheduler().cancelTask(task);
	}
}
