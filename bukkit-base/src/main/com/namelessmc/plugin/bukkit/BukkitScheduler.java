package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;

public class BukkitScheduler extends AbstractScheduler {

	private final @NonNull BukkitNamelessPlugin plugin;

	BukkitScheduler(final @NonNull BukkitNamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runAsync(final @NonNull Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
	}

	@Override
	public void runSync(final @NonNull Runnable runnable) {
		Bukkit.getScheduler().runTask(this.plugin, runnable);
	}

	@Override
	public BukkitScheduledTask runTimer(final @NonNull Runnable runnable,
																 final @NonNull Duration interval) {
		long ticks = interval.toMillis() / 50;
		final BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, ticks, ticks);
		return new BukkitScheduledTask(task);
	}

	@Override
	public BukkitScheduledTask runDelayed(final @NonNull Runnable runnable,
																   final @NonNull Duration delay) {
		long ticks = delay.toMillis() / 50;
		final BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, runnable, ticks);
		return new BukkitScheduledTask(task);
	}

	public static class BukkitScheduledTask extends AbstractScheduledTask {

		private final @NonNull BukkitTask task;

		private BukkitScheduledTask(final @NonNull BukkitTask task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}

	}

}
