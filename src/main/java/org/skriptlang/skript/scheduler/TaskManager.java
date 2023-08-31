package org.skriptlang.skript.scheduler;

import java.util.concurrent.TimeUnit;

import org.skriptlang.skript.scheduler.platforms.FoliaScheduler;
import org.skriptlang.skript.scheduler.platforms.SpigotScheduler;

import ch.njol.skript.Skript;

/**
 * Class to handle the abstract versioning and to be loaded from the Skript main class.
 */
public class TaskManager {

	private static PlatformScheduler scheduler;
	private static Skript instance;

	/**
	 * For the main Skript class to call once.
	 * @throws IllegalAccessException if the TaskManager has already been initalized.
	 */
	public TaskManager(Skript instance) throws IllegalAccessException {
		if (TaskManager.instance != null)
			throw new IllegalAccessException("The TaskManager has already been initalized!");
		TaskManager.instance = instance;
		if (Skript.classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
			TaskManager.scheduler = new FoliaScheduler();
		} else {
			TaskManager.scheduler = new SpigotScheduler();
		}
	}

	public PlatformScheduler getScheduler() {
		return scheduler;
	}

	public static void run(Task task, long delayInTicks) {
		scheduler.run(task, delayInTicks);
	}

	public static boolean cancel(Task task) {
		return scheduler.cancel(task);
	}

	public static boolean isAlive(Task task) {
		return scheduler.isAlive(task);
	}

	public static void run(AsyncTask task, long delay, TimeUnit unit) {
		scheduler.run(task, delay, unit);
	}

	public static boolean cancel(AsyncTask task) {
		return scheduler.cancel(task);
	}

	public static boolean isAlive(AsyncTask task) {
		return scheduler.isAlive(task);
	}

}
