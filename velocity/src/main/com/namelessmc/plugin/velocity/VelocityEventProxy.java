package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class VelocityEventProxy {

	private final @NonNull NamelessPlugin plugin;

	VelocityEventProxy(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onJoin(final @NonNull ServerConnectedEvent event) {
		final NamelessPlayer player = Objects.requireNonNull(
				this.plugin.audiences().player(event.getPlayer().getUniqueId()));
		final ServerJoinEvent event2 = new ServerJoinEvent(player);
		this.plugin.events().post(event2);
	}

}
