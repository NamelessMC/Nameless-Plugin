package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SpongeScheduler extends AbstractScheduler {

	private final @NotNull NamelessPluginSponge plugin;
	private final @NotNull PluginContainer spongePlugin;

	SpongeScheduler(final @NotNull NamelessPluginSponge plugin,
					final @NotNull PluginContainer spongePlugin) {
		this.plugin = plugin;
		this.spongePlugin = spongePlugin;
	}

	@Override
	public void runAsync(@NotNull Runnable runnable) {
		final Task task = Task.builder()
				.plugin(spongePlugin)
				.execute(runnable)
				.async()
				.submit(this.plugin);
	}

	@Override
	public void runSync(@NotNull Runnable runnable) {
		final Task task = Task.builder()
				.plugin(spongePlugin)
				.execute(runnable)
				.build();
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
