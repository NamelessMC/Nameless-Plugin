package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VelocityEventProxy {

	private final @NotNull NamelessPlugin plugin;

	VelocityEventProxy(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onJoin(final @NotNull ServerConnectedEvent event) {
		final NamelessPlayer player = Objects.requireNonNull(
				this.plugin.audiences().player(event.getPlayer().getUniqueId()));
		final ServerJoinEvent event2 = new ServerJoinEvent(player);
		this.plugin.events().post(event2);
	}

}
