package me.wobbychip.smptweaks.utils;

import me.wobbychip.smptweaks.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class TaskUtils implements Listener {
	private static final HashMap<Integer, Runnable> runnables0 = new HashMap<>();
	private static final HashMap<Runnable, Integer> runnables1 = new HashMap<>();

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
		Bukkit.getScheduler().cancelTask(task);
		Runnable runnable = runnables0.remove(task);
		runnables1.remove(runnable);

		task = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, runnable, ticks);
		runnables0.put(task, runnable);
		runnables1.put(runnable, task);
		return task;
	}

	public static int scheduleSyncRepeatingTask(Runnable runnable, long first, long interval) {
		int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, runnable, first, interval);
		runnables0.put(task, runnable);
		return task;
	}

	public static int rescheduleSyncRepeatingTask(int task, long first, long interval) {
		if (!runnables0.containsKey(task)) { return -1; }
		Bukkit.getScheduler().cancelTask(task);
		Runnable runnable = runnables0.remove(task);
		return scheduleSyncRepeatingTask(runnable, first, interval);
	}

	public static void finishTask(int task) {
		if (!runnables0.containsKey(task)) { return; }
		Bukkit.getScheduler().cancelTask(task);
		runnables0.remove(task).run(); //Don't care, because inside wrapper we also use runnables0.remove()
	}

	public static void cancelTask(int task) {
		if (!runnables0.containsKey(task)) { return; }
		Bukkit.getScheduler().cancelTask(task);
		runnables1.remove(runnables0.remove(task)); //I do not care if runnables1.remove() will return null
	}
}
