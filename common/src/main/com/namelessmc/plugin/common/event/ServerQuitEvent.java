package com.namelessmc.plugin.common.event;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ServerQuitEvent extends AbstractEvent {

	private final @NotNull UUID uuid;

	public ServerQuitEvent(final @NotNull UUID uuid) {
		this.uuid = uuid;
	}

	public @NotNull UUID uuid() {
		return this.uuid;
	}

}
