package com.namelessmc.plugin.sponge8;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.event.NamelessJoinEvent;
import com.namelessmc.plugin.sponge8.audiences.SpongeNamelessPlayer;
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
		final NamelessPlayer player = new SpongeNamelessPlayer(event.player());
		this.plugin.events().post(new NamelessJoinEvent(player));
	}

	// TODO quit event

}
