package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.event.NamelessJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class SpongeEventProxy {

	private final @NotNull NamelessPlugin plugin;

	SpongeEventProxy(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Listener
	public void onJoin(ServerSideConnectionEvent.Join event) {
		final NamelessPlayer player = new NamelessPlayer(event.player(), event.player().uniqueId(), event.player().name());
		this.plugin.events().post(new NamelessJoinEvent(player));
	}

	// TODO quit event

}
