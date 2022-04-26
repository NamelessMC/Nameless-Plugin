package com.namelessmc.plugin.common.event;

import com.namelessmc.plugin.common.NamelessPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ServerJoinEvent extends AbstractEvent {

	private final @NonNull NamelessPlayer player;

	public ServerJoinEvent(final @NonNull NamelessPlayer player) {
		this.player = player;
	}

	public @NonNull NamelessPlayer player() {
		return this.player;
	}

}
