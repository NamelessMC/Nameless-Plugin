package com.namelessmc.plugin.common.event;

import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NamelessJoinEvent extends NamelessEvent {

	private final @NonNull NamelessPlayer player;

	public NamelessJoinEvent(final @NonNull NamelessPlayer player) {
		this.player = player;
	}

	public @NonNull NamelessPlayer player() {
		return this.player;
	}

}
