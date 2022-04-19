package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class BukkitScheduler extends AbstractScheduler {

	private final @NotNull BukkitNamelessPlugin plugin;

	BukkitScheduler(final @NotNull BukkitNamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runAsync(@NotNull Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
	}

	@Override
	public void runSync(@NotNull Runnable runnable) {
		Bukkit.getScheduler().runTask(this.plugin, runnable);
	}

	@Override
	public @NotNull SpigotScheduledTask runTimer(@NotNull Runnable runnable, @NotNull Duration interval) {
		long ticks = interval.toMillis() / 50;
		final BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, ticks, ticks);
		return new SpigotScheduledTask(task);
	}

	@Override
	public @NotNull SpigotScheduledTask runDelayed(@NotNull Runnable runnable, @NotNull Duration delay) {
		long ticks = delay.toMillis() / 50;
		final BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, runnable, ticks);
		return new SpigotScheduledTask(task);
	}

	public static class SpigotScheduledTask extends AbstractScheduledTask {

		private final @NotNull BukkitTask task;

		private SpigotScheduledTask(final @NotNull BukkitTask task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}

	}

}
