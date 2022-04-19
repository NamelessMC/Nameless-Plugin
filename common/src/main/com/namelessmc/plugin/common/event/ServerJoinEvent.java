package com.namelessmc.plugin.common.event;

import com.namelessmc.plugin.common.NamelessPlayer;
import org.jetbrains.annotations.NotNull;

public class ServerJoinEvent extends AbstractEvent {

	private final @NotNull NamelessPlayer player;

	public ServerJoinEvent(final @NotNull NamelessPlayer player) {
		this.player = player;
	}

	public @NotNull NamelessPlayer getPlayer() {
		return this.player;
	}

}
