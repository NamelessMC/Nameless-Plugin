package com.namelessmc.plugin.common.command;

public abstract class AbstractScheduler {

	public abstract void runAsync(Runnable runnable);

	public abstract void runSync(Runnable runnable);

}
