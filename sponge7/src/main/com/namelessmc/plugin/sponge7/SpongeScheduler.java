package com.namelessmc.plugin.sponge7;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SpongeScheduler extends AbstractScheduler {

	private final SpongeNamelessPlugin plugin;

	SpongeScheduler(final SpongeNamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runAsync(@NonNull Runnable runnable) {
		Task.builder()
				.execute(runnable)
				.async()
				.submit(this.plugin);
	}

	@Override
	public void runSync(@NonNull Runnable runnable) {
		Task.builder()
				.execute(runnable)
				.submit(this.plugin);
	}

	@Override
	public @NonNull SpongeScheduledTask runTimer(@NonNull Runnable runnable, @NonNull Duration interval) {
		final Task task = Task.builder()
				.execute(runnable)
				.delay(interval.toNanos(), TimeUnit.NANOSECONDS)
				.interval(interval.toNanos(), TimeUnit.NANOSECONDS)
				.submit(this.plugin);
		return new SpongeScheduledTask(task);
	}

	@Override
	public @NonNull SpongeScheduledTask runDelayed(@NonNull Runnable runnable, @NonNull Duration delay) {
		final Task task = Task.builder()
				.execute(runnable)
				.delay(delay.toNanos(), TimeUnit.NANOSECONDS)
				.submit(this.plugin);
		return new SpongeScheduledTask(task);
	}

	public static class SpongeScheduledTask extends AbstractScheduledTask {

		private final @NonNull Task task;

		private SpongeScheduledTask(final @NonNull Task task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}

	}

}
