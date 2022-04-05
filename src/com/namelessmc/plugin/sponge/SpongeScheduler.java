package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SpongeScheduler extends AbstractScheduler {

	private final @NotNull NamelessPluginSponge plugin;

	SpongeScheduler(final @NotNull NamelessPluginSponge plugin) {
		this.plugin = plugin;
	}

	@Override
	public void runAsync(@NotNull Runnable runnable) {
		Task.builder()
				.execute(runnable)
				.async()
				.submit(this.plugin);
	}

	@Override
	public void runSync(@NotNull Runnable runnable) {
		Task.builder()
				.execute(runnable)
				.submit(this.plugin);
	}

	@Override
	public @NotNull SpongeScheduledTask runTimer(@NotNull Runnable runnable, @NotNull Duration interval) {
		final Task task = Task.builder()
				.execute(runnable)
				.delay(interval.toNanos(), TimeUnit.NANOSECONDS)
				.interval(interval.toNanos(), TimeUnit.NANOSECONDS)
				.submit(this.plugin);
		return new SpongeScheduledTask(task);
	}

	@Override
	public @NotNull SpongeScheduledTask runDelayed(@NotNull Runnable runnable, @NotNull Duration delay) {
		final Task task = Task.builder()
				.execute(runnable)
				.delay(delay.toNanos(), TimeUnit.NANOSECONDS)
				.submit(this.plugin);
		return new SpongeScheduledTask(task);
	}

	public static class SpongeScheduledTask extends AbstractScheduledTask {

		private final @NotNull Task task;

		private SpongeScheduledTask(final @NotNull Task task) {
			this.task = task;
		}

		@Override
		public void cancel() {
			this.task.cancel();
		}

	}

}
