package com.namelessmc.plugin.common.command;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;

public abstract class AbstractScheduler {

	public abstract void runAsync(final @NonNull Runnable runnable);

	public abstract void runSync(final @NonNull Runnable runnable);

	public abstract @NonNull AbstractScheduledTask runTimer(final @NonNull Runnable runnable, final @NonNull Duration interval);

	public abstract @NonNull AbstractScheduledTask runDelayed(final @NonNull Runnable runnable, final @NonNull Duration delay);

}
