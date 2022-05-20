package com.namelessmc.plugin.sponge8;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SpongeScheduler extends AbstractScheduler {

	private final PluginContainer spongePlugin;
	private final ExecutorService executor = Executors.newCachedThreadPool();

	SpongeScheduler(final PluginContainer spongePlugin) {
		this.spongePlugin = spongePlugin;
	}

	@Override
	public void runAsync(final @NonNull Runnable runnable) {
		// Sponge probably provides an API for async tasks, but I couldn't find it. Use Java ExecutorService instead.
		executor.submit(runnable);
	}

	@Override
	public void runSync(final @NonNull Runnable runnable) {
		final Task task = Task.builder()
				.plugin(spongePlugin)
				.execute(runnable)
				.build();
		Sponge.server().scheduler().submit(task);
	}

	@Override
	public @NonNull SpongeScheduledTask runTimer(@NonNull Runnable runnable, @NonNull Duration interval) {
		final Task task = Task.builder()
				.plugin(spongePlugin)
				.execute(runnable)
				.delay(interval.toNanos(), TimeUnit.NANOSECONDS)
				.interval(interval.toNanos(), TimeUnit.NANOSECONDS)
				.build();
		return new SpongeScheduledTask(Sponge.server().scheduler().submit(task));
	}

	@Override
	public @NonNull SpongeScheduledTask runDelayed(@NonNull Runnable runnable, @NonNull Duration delay) {
		final Task task = Task.builder()
				.plugin(spongePlugin)
				.execute(runnable)
				.delay(delay.toNanos(), TimeUnit.NANOSECONDS)
				.build();
		return new SpongeScheduledTask(Sponge.server().scheduler().submit(task));
	}

	public static class SpongeScheduledTask extends AbstractScheduledTask {

		private final ScheduledTask task;

		private SpongeScheduledTask(final ScheduledTask task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}

	}

}
