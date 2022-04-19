package com.namelessmc.plugin.common.command;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public abstract class AbstractScheduler {

	public abstract void runAsync(final @NotNull Runnable runnable);

	public abstract void runSync(final @NotNull Runnable runnable);

	public abstract @NotNull AbstractScheduledTask runTimer(final @NotNull Runnable runnable, final @NotNull Duration interval);

	public abstract @NotNull AbstractScheduledTask runDelayed(final @NotNull Runnable runnable, final @NotNull Duration delay);

}
