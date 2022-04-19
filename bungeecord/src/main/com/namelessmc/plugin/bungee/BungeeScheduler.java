package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class BungeeScheduler extends AbstractScheduler {

	private final @NotNull BungeeNamelessPlugin plugin;

	BungeeScheduler(final @NotNull BungeeNamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runAsync(final @NotNull Runnable runnable) {
		ProxyServer.getInstance().getScheduler()
				.runAsync(this.plugin, runnable);
	}

	@Override
	public void runSync(final @NotNull Runnable runnable) {
		ProxyServer.getInstance().getScheduler()
				.schedule(this.plugin, runnable, 0, TimeUnit.NANOSECONDS);
	}

	@Override
	public @NotNull BungeeScheduledTask runTimer(@NotNull Runnable runnable, @NotNull Duration interval) {
		long l = interval.toNanos();
		final ScheduledTask task = ProxyServer.getInstance().getScheduler()
				.schedule(this.plugin, runnable, l, l, TimeUnit.NANOSECONDS);
		return new BungeeScheduledTask(task);
	}


	@Override
	public @NotNull BungeeScheduledTask runDelayed(@NotNull Runnable runnable, @NotNull Duration delay) {
		long l = delay.toNanos();
		final ScheduledTask task = ProxyServer.getInstance().getScheduler()
				.schedule(this.plugin, runnable, l, TimeUnit.NANOSECONDS);
		return new BungeeScheduledTask(task);
	}

	public static class BungeeScheduledTask extends AbstractScheduledTask {

		private final @NotNull ScheduledTask task;

		private BungeeScheduledTask(final @NotNull ScheduledTask task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}


	}
}
