package com.namelessmc.plugin.common.event;

import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NamelessPlayerEvent extends NamelessOfflinePlayerEvent {

	private final @NonNull NamelessPlayer player;

	public NamelessPlayerEvent(final @NonNull NamelessPlayer player) {
		super(player.uuid());
		this.player = player;
	}

	public @NonNull NamelessPlayer player() {
		return this.player;
	}

}
