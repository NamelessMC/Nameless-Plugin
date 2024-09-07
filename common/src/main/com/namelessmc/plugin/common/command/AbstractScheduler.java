package com.namelessmc.plugin.common.command;

import java.time.Duration;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractScheduler {

	public abstract void runAsync(final @NonNull Runnable runnable);

	public abstract void runSync(final @NonNull Runnable runnable);

	public abstract @Nullable AbstractScheduledTask runTimer(final @NonNull Runnable runnable, final @NonNull Duration interval);

	public abstract @Nullable AbstractScheduledTask runDelayed(final @NonNull Runnable runnable, final @NonNull Duration delay);

}
