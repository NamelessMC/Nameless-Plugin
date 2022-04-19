package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.namelessmc.plugin.common.event.ServerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Objects;

public class SpongeEventProxy {

	private final @NotNull NamelessPlugin plugin;

	SpongeEventProxy(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		final NamelessPlayer player = Objects.requireNonNull(
				plugin.audiences().player(event.getTargetEntity().getUniqueId()));
		final ServerJoinEvent event2 = new ServerJoinEvent(player);
		this.plugin.events().post(event2);
	}

	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect event) {
		final ServerQuitEvent event2 = new ServerQuitEvent(event.getTargetEntity().getUniqueId());
		this.plugin.events().post(event2);
	}

}
