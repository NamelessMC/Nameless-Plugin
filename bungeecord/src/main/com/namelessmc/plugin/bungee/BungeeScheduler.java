package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class BungeeScheduler extends AbstractScheduler {

	private final @NonNull BungeeNamelessPlugin plugin;

	BungeeScheduler(final @NonNull BungeeNamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runAsync(final @NonNull Runnable runnable) {
		ProxyServer.getInstance().getScheduler()
				.runAsync(this.plugin, runnable);
	}

	@Override
	public void runSync(final @NonNull Runnable runnable) {
		ProxyServer.getInstance().getScheduler()
				.schedule(this.plugin, runnable, 0, TimeUnit.NANOSECONDS);
	}

	@Override
	public @NonNull BungeeScheduledTask runTimer(@NonNull Runnable runnable, @NonNull Duration interval) {
		long l = interval.toNanos();
		final ScheduledTask task = ProxyServer.getInstance().getScheduler()
				.schedule(this.plugin, runnable, l, l, TimeUnit.NANOSECONDS);
		return new BungeeScheduledTask(task);
	}


	@Override
	public @NonNull BungeeScheduledTask runDelayed(@NonNull Runnable runnable, @NonNull Duration delay) {
		long l = delay.toNanos();
		final ScheduledTask task = ProxyServer.getInstance().getScheduler()
				.schedule(this.plugin, runnable, l, TimeUnit.NANOSECONDS);
		return new BungeeScheduledTask(task);
	}

	public static class BungeeScheduledTask extends AbstractScheduledTask {

		private final @NonNull ScheduledTask task;

		private BungeeScheduledTask(final @NonNull ScheduledTask task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}


	}
}
