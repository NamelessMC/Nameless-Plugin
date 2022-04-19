package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class VelocityScheduler extends AbstractScheduler {

	private final @NotNull VelocityNamelessPlugin plugin;
	private final @NotNull Scheduler scheduler;

	VelocityScheduler(final @NotNull VelocityNamelessPlugin plugin,
					  final @NotNull Scheduler scheduler) {
		this.plugin = plugin;
		this.scheduler = scheduler;
	}

	@Override
	public void runAsync(final @NotNull Runnable runnable) {
		this.scheduler
				.buildTask(this.plugin, runnable)
				.schedule();
	}

	@Override
	public void runSync(final @NotNull Runnable runnable) {
		// Velocity has no "main thread", we can just run it in the current thread
		runnable.run();
	}

	@Override
	public @NotNull VelocityScheduledTask runTimer(@NotNull Runnable runnable, @NotNull Duration interval) {
		final ScheduledTask task = this.scheduler
				.buildTask(this.plugin, runnable)
				.delay(interval.toNanos(), TimeUnit.NANOSECONDS)
				.repeat(interval.toNanos(), TimeUnit.NANOSECONDS)
				.schedule();
		return new VelocityScheduledTask(task);
	}

	@Override
	public @NotNull VelocityScheduledTask runDelayed(@NotNull Runnable runnable, @NotNull Duration delay) {
		final ScheduledTask task = this.scheduler
				.buildTask(this.plugin, runnable)
				.delay(delay.toNanos(), TimeUnit.NANOSECONDS)
				.schedule();
		return new VelocityScheduledTask(task);
	}

	public static class VelocityScheduledTask extends AbstractScheduledTask {

		private final @NotNull ScheduledTask task;

		private VelocityScheduledTask(final @NotNull ScheduledTask task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}

	}


}
